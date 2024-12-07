package com.gogym.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;

public class JsonUtil {
  
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  
  static {
    OBJECT_MAPPER.findAndRegisterModules();
    OBJECT_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
  }
  
  /**
   * 객체를 JSON 문자열로 직렬화합니다.
   *
   * @param object 직렬화할 객체
   * @return 직렬화된 JSON 문자열
   */
  public static String serialize(Object object) {
      try {
          return OBJECT_MAPPER.writeValueAsString(object);
      } catch (JsonProcessingException e) {
          throw new CustomException(ErrorCode.JSON_MAPPING_FAILURE);
      }
  }
  
  /**
   * JSON 문자열을 특정 클래스 타입의 객체로 역직렬화합니다.
   *
   * @param <T> 대상 클래스 타입
   * @param json 역직렬화할 JSON 문자열
   * @param valueType 역직렬화 대상 클래스
   * @return 역직렬화된 객체
   */
  public static <T> T deserialize(String json, Class<T> valueType) {
      try {
          return OBJECT_MAPPER.readValue(json, valueType);
      } catch (JsonProcessingException e) {
          throw new CustomException(ErrorCode.JSON_MAPPING_FAILURE);
      }
  }
  
}
