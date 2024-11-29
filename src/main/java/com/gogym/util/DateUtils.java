package com.gogym.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
  
  // ISO 8601 형식으로 현재 날짜와 시간을 반환
  public static String getCurrentDateTime() {
    LocalDateTime now = LocalDateTime.now();
    return now.format(DateTimeFormatter.ISO_DATE_TIME);
    
  }
  
  //추가하실 것 추가하시면 됩니다.
  
}
