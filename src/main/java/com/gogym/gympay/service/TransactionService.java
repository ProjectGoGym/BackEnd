package com.gogym.gympay.service;

import com.gogym.chat.entity.ChatRoom;
import com.gogym.chat.service.ChatRoomQueryService;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.gympay.dto.request.UpdateDateRequest;
import com.gogym.gympay.entity.SafePayment;
import com.gogym.gympay.entity.Transaction;
import com.gogym.gympay.entity.constant.SafePaymentStatus;
import com.gogym.gympay.repository.TransactionRepository;
import com.gogym.member.entity.Member;
import com.gogym.post.dto.PostPageResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

  private final ChatRoomQueryService chatRoomQueryService;

  private final TransactionRepository transactionRepository;
  
  private final ChatRoomQueryService chatRoomQueryService;

  @Transactional
  public void start(ChatRoom chatRoom, Member seller, Member buyer) {
    Transaction transaction = new Transaction(chatRoom, seller, buyer);
    transactionRepository.save(transaction);

    chatRoom.setTransactionId(transaction.getId());
  }

  @Transactional
  public void restart(Transaction transaction) {
    transaction.start();
  }

  @Transactional
  public void cancel(Transaction transaction) {
    canCancel(transaction);

    transaction.cancel();
  }

  @Transactional
  public void complete(Transaction transaction) {
    transaction.complete();
  }

  @Transactional
  public void patchDate(Long memberId, Long chatRoomId, UpdateDateRequest request) {
    ChatRoom chatRoom = chatRoomQueryService.getChatRoomById(chatRoomId);
    chatRoomQueryService.isMemberInChatRoom(chatRoomId, memberId);

    Transaction transaction = getById(chatRoom.getTransactionId());
    transaction.setMeetingAt(request.dateTime());
    ChatRoom chatRoom = chatRoomQueryService.getChatRoomById(chatRoomId);
  }

  private void canCancel(Transaction transaction) {
    List<SafePayment> safePayments = transaction.getSafePayments();
    boolean canCancel = safePayments.stream()
        .noneMatch(safePayment -> SafePaymentStatus.COMPLETED.equals(safePayment.getStatus())
            || SafePaymentStatus.IN_PROGRESS.equals(safePayment.getStatus()));

    if (!canCancel) {
      throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
          "안전결제가 진행 중이거나 완료되면 거래를 취소할 수 없습니다.");
    }
  }

  public Transaction getById(Long id) {
    return transactionRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));
  }

  public Page<PostPageResponseDto> getMyTransactions(Long memberId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable, String type) {
    return transactionRepository.getMyTransactions(memberId, startDate, endDate, pageable, type);
  }
}
