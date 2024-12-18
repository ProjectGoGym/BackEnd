package com.gogym.gympay.service.strategy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.gogym.gympay.dto.PaymentResult;
import com.gogym.gympay.entity.GymPay;
import com.gogym.gympay.repository.GymPayRepository;
import com.gogym.gympay.service.PaymentResultTestUtil;
import com.gogym.member.entity.Member;
import com.gogym.member.entity.Role;
import com.gogym.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@SpringBootTest(properties = "JASYPT_ENCRYPTOR_PASSWORD=Rhwla123!")
class PaymentPaidStrategyTest {

  @Autowired
  private PaymentPaidStrategy paymentPaidStrategy;

  @Autowired
  private MemberRepository memberRepository;

  private Member member;
  private GymPay gymPay;
  private PaymentResult paymentResult;
  @Autowired
  private GymPayRepository gymPayRepository;

  @BeforeEach
  void setUp() {
    member = Member.builder()
        .name("조하얀")
        .nickname("텧스트")
        .phone("010-1111-2222")
        .email("test@example.com")
        .password("encodedPassword")
        .role(Role.USER)
        .verifiedAt(LocalDateTime.now())
        .build();
    memberRepository.save(member);
    gymPay = new GymPay(0, member);
    gymPayRepository.save(gymPay);

    paymentResult = PaymentResultTestUtil.createDefaultPaymentResult();
  }

  @Test
  @Transactional
  void 동시에_10명이_충전을_요청하면_락을_획득하는_순서대로_성공한다() throws InterruptedException {
    int threadCount = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch countDownLatch = new CountDownLatch(threadCount);;

    for (int i = 0; i < threadCount; i++) {
      executorService.execute(() -> {
        try {
          paymentPaidStrategy.process(
              paymentResult,
              Map.of("amount", "10000", "member-email", "test@example.com"),
              member
          );
          log.info("🔒 락을 획득하여 실행 중 - 스레드: {}", Thread.currentThread().getName());
        } catch (Exception e) {
          log.error("예외 발생: {}", e.getMessage());
        } finally {
          countDownLatch.countDown();
        }
      });
    }

    countDownLatch.await();
    GymPay gymPayAfter = gymPayRepository.findById(gymPay.getId()).orElseThrow();
    assertThat(gymPayAfter.getBalance()).isEqualTo(paymentResult.amount().total() * threadCount);
  }
}