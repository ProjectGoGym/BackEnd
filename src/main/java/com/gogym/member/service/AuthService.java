package com.gogym.member.service;

import com.gogym.common.response.ErrorCode;
import com.gogym.exception.CustomException;
import com.gogym.member.dto.AuthRequest;
import com.gogym.member.dto.AuthResponse;
import com.gogym.member.entity.EmailVerificationToken;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.EmailVerificationTokenRepository;
import com.gogym.member.repository.MemberRepository;
import com.gogym.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ROLE_USER = "ROLE_USER";
    private static final long TOKEN_EXPIRATION_TIME = 60 * 60 * 1000; // 1시간 (밀리초)
    private static final String EMAIL_VERIFICATION_PREFIX = "emailVerification:";

    private final MemberRepository memberRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    // 회원가입 처리
    @Transactional
    public AuthResponse signUp(AuthRequest request) {
        // 이메일 중복 확인
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 닉네임 중복 확인
        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 회원 정보를 엔티티로 생성
        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .profileImageUrl(request.getProfileImageUrl())
                .interestArea1(request.getInterestArea1())
                .interestArea2(request.getInterestArea2())
                .build();

        // 회원 데이터 저장
        memberRepository.save(member);
       
        
        // 응답 객체 생성 및 반환
        return AuthResponse.builder()
                .userId(member.getId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                
                .build();
    }

    // 로그인 처리
    public AuthResponse login(AuthRequest request) {
        // 이메일로 사용자 조회
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        //토큰
        String token = jwtTokenProvider.createToken(member.getEmail(), List.of(member.getRole().name()));
        
        // 응답 객체 생성 및 반환
        return AuthResponse.builder()
                .userId(member.getId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .token(token)
                .build();
    }

    // JWT 토큰 생성
    public String generateToken(String email, List<String> roles) {
        return jwtTokenProvider.createToken(email, roles);
    }

    // 로그아웃 처리
    public void logout(String token) {
        // Redis에 토큰 저장 및 만료 시간 설정
        redisTemplate.opsForValue().set(token, "logout", TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
    }

    // 비밀번호 재설정 처리
    public void resetPassword(AuthRequest request) {
        // 이메일로 사용자 찾기
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

        // 새 비밀번호 암호화 후 저장
        member.setPassword(passwordEncoder.encode(request.getNewPassword()));
        memberRepository.save(member);
    }

    // 이메일 중복 확인
    @Transactional(readOnly = true)
    public void checkEmail(String email) {
        boolean exists = memberRepository.existsByEmail(email);
        if (exists) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    // 닉네임 중복 확인
    @Transactional(readOnly = true)
    public void checkNickname(String nickname) {
        boolean exists = memberRepository.existsByNickname(nickname);
        if (exists) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    // 이메일 인증 요청
    public void sendVerificationEmail(String email) {
        // 토큰 생성 및 저장
        EmailVerificationToken token = new EmailVerificationToken(email);
        tokenRepository.save(token);

        // 인증 URL 생성
        String verificationUrl = "http://localhost:8080/api/auth/verify-email?token=" + token.getToken();

        // 이메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("이메일 인증 요청");
        message.setText("다음 링크를 클릭하여 이메일을 인증하세요: " + verificationUrl);

        mailSender.send(message);
    }

    // 이메일 인증 확인
    @Transactional
    public void verifyEmailToken(String token) {
        // Redis에서 토큰 검증
        String email = redisTemplate.opsForValue().get(EMAIL_VERIFICATION_PREFIX + token);
        
        // Redis에 저장된 이메일이 없는 경우 - 유효하지 않은 토큰
        if (email == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN); // 토큰이 유효하지 않음
        }

        // 토큰이 유효하면 회원 정보 업데이트
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 이메일 인증 상태 업데이트
        member.setEmailVerified(true);
        memberRepository.save(member);

        // 사용한 토큰 삭제 
        redisTemplate.delete(EMAIL_VERIFICATION_PREFIX + token);
    }
}
