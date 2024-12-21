package com.gogym.gympay.service;

import com.gogym.chat.type.MessageType;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.gympay.entity.SafePayment;
import com.gogym.gympay.entity.Transaction;
import com.gogym.gympay.entity.constant.RequesterRole;
import com.gogym.gympay.entity.constant.SafePaymentStatus;
import com.gogym.gympay.entity.constant.TransactionStatus;
import com.gogym.gympay.event.SendMessageEvent;
import com.gogym.gympay.repository.SafePaymentRepository;
import com.gogym.member.entity.Member;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SafePaymentService {

  private final ApplicationEventPublisher eventPublisher;

  private final GymPayService gymPayService;
  private final TransactionService transactionService;

  private final SafePaymentRepository safePaymentRepository;

  @Transactional
  public Long save(Long requesterId, Long transactionId, int amount) {
    Transaction transaction = transactionService.getById(transactionId);
    validateTransactionStatus(transaction);

    Member seller = transaction.getSeller();
    Member buyer = transaction.getBuyer();
    RequesterRole requesterRole = determineRequesterRole(requesterId, seller);

    Member requester = (requesterRole == RequesterRole.SELLER) ? seller : buyer;
    Member responder = (requesterRole == RequesterRole.SELLER) ? buyer : seller;

    SafePayment safePayment = SafePayment.of(transaction, requester, responder, amount,
        requesterRole);

    safePaymentRepository.save(safePayment);

    String message = String.format("안전결제를 요청했습니다. \n 금액 : %d원", amount);
    eventPublisher.publishEvent(new SendMessageEvent(transaction.getChatRoom().getId(), requesterId, message,
        MessageType.SYSTEM_SAFE_PAYMENT_REQUEST));

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
  public void approve(Long postId, Long safePaymentId, Long requesterId) {
    SafePayment safePayment = getById(safePaymentId);

    if (!Objects.equals(safePayment.getResponder().getId(), requesterId)) {
      throw new CustomException(ErrorCode.FORBIDDEN, "요청을 받은 사람이 아닙니다.");
    }

    safePayment.approve();
    gymPayService.withdraw(safePayment.getBuyer().getGymPay(),
        safePayment.getAmount(), safePayment.getSeller().getId(), postId);

    String message = "안전결제를 수락했습니다.";
    eventPublisher.publishEvent(
        new SendMessageEvent(safePayment.getTransaction().getChatRoom().getId(), requesterId,
            message,
            MessageType.SYSTEM_SAFE_PAYMENT_APPROVAL));
  }

  @Transactional
  public void reject(Long safePaymentId, Long requesterId) {
    SafePayment safePayment = getById(safePaymentId);

    if (!Objects.equals(safePayment.getResponder().getId(), requesterId)) {
      throw new CustomException(ErrorCode.FORBIDDEN, "요청을 받은 사람이 아닙니다.");
    }

    safePayment.reject();

    String message = "안전결제를 거절했습니다.";
    eventPublisher.publishEvent(
        new SendMessageEvent(safePayment.getTransaction().getChatRoom().getId(), requesterId,
            message,
            MessageType.SYSTEM_SAFE_PAYMENT_REJECTION));
  }

  @Transactional
  public void complete(Long postId, Long safePaymentId, Long requesterId) {
    SafePayment safePayment = getById(safePaymentId);

    if (!Objects.equals(safePayment.getBuyer().getId(), requesterId)) {
      throw new CustomException(ErrorCode.FORBIDDEN, "구매자만 거래 확정이 가능합니다.");
    }

    safePayment.complete();
    gymPayService.deposit(safePayment.getSeller().getGymPay(),
        safePayment.getAmount(), safePayment.getBuyer().getId(), postId);

    String message = String.format("안전결제가 완료되었습니다. \n 금액 : %d원", safePayment.getAmount());
    eventPublisher.publishEvent(
        new SendMessageEvent(safePayment.getTransaction().getChatRoom().getId(), requesterId,
            message,
            MessageType.SYSTEM_SAFE_PAYMENT_COMPLETE));
  }

  @Transactional
  public void cancel(Long postId, Long safePaymentId, Long requesterId) {
    SafePayment safePayment = getById(safePaymentId);

    if (!(Objects.equals(safePayment.getBuyer().getId(), requesterId)
        || Objects.equals(safePayment.getSeller().getId(), requesterId))) {
      throw new CustomException(ErrorCode.FORBIDDEN, "해당 채팅방에 참여 중이 아닙니다.");
    }

    if (safePayment.getStatus().equals(SafePaymentStatus.IN_PROGRESS)) {
      gymPayService.deposit(safePayment.getBuyer().getGymPay(),
          safePayment.getAmount(), safePayment.getSeller().getId(), postId);
    }

    safePayment.cancel();

    String message = "안전결제가 취소되었습니다.";
    eventPublisher.publishEvent(
        new SendMessageEvent(safePayment.getTransaction().getChatRoom().getId(), requesterId,
            message,
            MessageType.SYSTEM_SAFE_PAYMENT_CANCEL));
  }

  private void validateTransactionStatus(Transaction transaction) {
    if (transaction == null
        || transaction.getStatus() != TransactionStatus.STARTED) {
      throw new CustomException(ErrorCode.NOT_IN_PROGRESS);
    }

    boolean canStart = transaction.getSafePayments().stream()
        .noneMatch(safePayment -> SafePaymentStatus.COMPLETED.equals(safePayment.getStatus())
            || SafePaymentStatus.IN_PROGRESS.equals(safePayment.getStatus()));

    if (canStart) {
      throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
          "안전결제가 진행 중이거나 완료되면 안전거래를 요청할 수 없습니다.");
    }
  }

  private SafePayment getById(Long safePaymentId) {
    return safePaymentRepository.findById(safePaymentId)
        .orElseThrow(() -> new CustomException(ErrorCode.SAFE_PAYMENT_NOT_FOUND));
  }
}
