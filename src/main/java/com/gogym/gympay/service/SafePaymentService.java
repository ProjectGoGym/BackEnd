package com.gogym.gympay.service;

import com.gogym.chat.entity.ChatRoom;
import com.gogym.chat.service.ChatRoomService;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.gympay.dto.request.SafePaymentRequest;
import com.gogym.gympay.entity.SafePayment;
import com.gogym.gympay.entity.constant.RequesterRole;
import com.gogym.gympay.entity.constant.SafePaymentStatus;
import com.gogym.gympay.repository.SafePaymentRepository;
import com.gogym.member.entity.Member;
import com.gogym.post.type.PostStatus;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SafePaymentService {

  private final ChatRoomService chatRoomService;
  private final GymPayService gymPayService;

  private final SafePaymentRepository safePaymentRepository;

  @Transactional
  public Long save(Long requesterId, Long chatRoomId, SafePaymentRequest request) {
    ChatRoom chatRoom = chatRoomService.getById(chatRoomId);

    validateTransactionStatus(chatRoom);

    Member seller = chatRoom.getTransaction().getSeller();
    Member buyer = chatRoom.getTransaction().getBuyer();
    RequesterRole requesterRole = determineRequesterRole(requesterId, seller);

    Member requester = (requesterRole == RequesterRole.SELLER) ? seller : buyer;
    Member responder = (requesterRole == RequesterRole.SELLER) ? buyer : seller;

    SafePayment safePayment = request.toEntity(requester, responder, chatRoom.getTransaction(), requesterRole);

    safePaymentRepository.save(safePayment);

    return safePayment.getId();
  }

  private RequesterRole determineRequesterRole(Long requesterId, Member seller) {
    if (seller.getId().equals(requesterId)) {
      return RequesterRole.SELLER;
    } else {
      return RequesterRole.BUYER;
    }
  }

  @Transactional
  public void approve(Long safePaymentId, Long requesterId) {
    SafePayment safePayment = getById(safePaymentId);

    if (!Objects.equals(safePayment.getResponder().getId(), requesterId)) {
      throw new CustomException(ErrorCode.FORBIDDEN, "요청을 받은 사람이 아닙니다.");
    }

    safePayment.approve();
    gymPayService.withdraw(safePayment.getBuyer().getGymPay(),
        safePayment.getAmount(), safePayment.getSeller().getId());
  }

  @Transactional
  public void reject(Long safePaymentId, Long requesterId) {
    SafePayment safePayment = getById(safePaymentId);

    if (!Objects.equals(safePayment.getResponder().getId(), requesterId)) {
      throw new CustomException(ErrorCode.FORBIDDEN, "요청을 받은 사람이 아닙니다.");
    }

    safePayment.reject();
  }

  @Transactional
  public void complete(Long safePaymentId, Long requesterId) {
    SafePayment safePayment = getById(safePaymentId);

    if (!Objects.equals(safePayment.getBuyer().getId(), requesterId)) {
      throw new CustomException(ErrorCode.FORBIDDEN, "구매자만 거래 확정이 가능합니다.");
    }

    safePayment.complete();
    gymPayService.deposit(safePayment.getSeller().getGymPay(),
        safePayment.getAmount(), safePayment.getBuyer().getId());
  }

  @Transactional
  public void cancel(Long safePaymentId, Long requesterId) {
    SafePayment safePayment = getById(safePaymentId);

    if (!(Objects.equals(safePayment.getBuyer().getId(), requesterId)
        || Objects.equals(safePayment.getSeller().getId(), requesterId))) {
      throw new CustomException(ErrorCode.FORBIDDEN, "해당 채팅방에 참여 중이 아닙니다.");
    }

    if (safePayment.getStatus().equals(SafePaymentStatus.IN_PROGRESS)) {
      gymPayService.deposit(safePayment.getBuyer().getGymPay(),
          safePayment.getAmount(), safePayment.getSeller().getId());
    }

    safePayment.cancel();
  }

  private void validateTransactionStatus(ChatRoom chatRoom) {
    if (!chatRoom.getPost().getStatus().equals(PostStatus.POSTING)) {
      throw new CustomException(ErrorCode.NOT_IN_PROGRESS);
    }
    if (chatRoom.getTransaction() == null) {
      throw new CustomException(ErrorCode.NOT_IN_PROGRESS);
    }
  }

  private SafePayment getById(Long safePaymentId) {
    return safePaymentRepository.findById(safePaymentId)
        .orElseThrow(() -> new CustomException(ErrorCode.SAFE_PAYMENT_NOT_FOUND));
  }
}
