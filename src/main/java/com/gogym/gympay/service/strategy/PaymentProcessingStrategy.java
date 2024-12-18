package com.gogym.gympay.service.strategy;

import com.gogym.gympay.dto.PaymentResult;
import com.gogym.member.entity.Member;
import java.util.Map;

public interface PaymentProcessingStrategy {

  void process(PaymentResult result, Map<Object, Object> preRegisteredData, Member member);
}
