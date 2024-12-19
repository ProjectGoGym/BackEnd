package com.gogym.gympay.service;

import com.gogym.chat.entity.ChatRoom;
import com.gogym.gympay.entity.Transaction;
import com.gogym.gympay.repository.TransactionRepository;
import com.gogym.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

  private final TransactionRepository transactionRepository;

  @Transactional
  public void save(ChatRoom chatRoom, Member seller, Member buyer) {
    Transaction transaction = new Transaction(chatRoom, seller, buyer);
    transactionRepository.save(transaction);
  }
}
