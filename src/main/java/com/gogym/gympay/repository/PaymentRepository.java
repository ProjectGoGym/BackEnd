package com.gogym.gympay.repository;

import com.gogym.gympay.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  Optional<Payment> findByMerchantId(String merchantId);
}
