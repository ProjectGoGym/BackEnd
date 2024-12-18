package com.gogym.gympay.repository;

import com.gogym.gympay.entity.SafePayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafePaymentRepository extends JpaRepository<SafePayment, Long> {

}
