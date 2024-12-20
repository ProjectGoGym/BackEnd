package com.gogym.chat.type;

public enum MessageType {
  
  /**
   * 일반 메시지
   */
  TEXT_ONLY, // 텍스트 메시지만 포함된 일반 메시지.
  
  /**
   * 안전결제 관련
   */
  SYSTEM_SAFE_PAYMENT_CANCEL, // 안전결제 취소 메시지.
  SYSTEM_SAFE_PAYMENT_REQUEST, // 안전결제 요청 메시지.
  SYSTEM_SAFE_PAYMENT_APPROVAL, // 안전결제 승인 메시지.
  SYSTEM_SAFE_PAYMENT_COMPLETE, // 안전결제 성공 메시지.
  SYSTEM_SAFE_PAYMENT_REJECTION, // 안전결제 거절 메시지.
  
  /**
   * 거래 날짜 관련
   */
  SYSTEM_TRANSACTION_DATE_CONFIRMED, // 거래 날짜 확정 메시지.
  SYSTEM_TRANSACTION_DATE_CHANGED, // 거래 날짜 변경 메시지.
  
  /**
   * 거래 관련
   */
  SYSTEM_TRANSACTION_CANCEL
  
}
