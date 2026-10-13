package com.distributor.ordermanagement.repository;

import com.distributor.ordermanagement.entity.Payment;
import com.distributor.ordermanagement.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByOrderRetailerId(Long retailerId);

    List<Payment> findByOrderDistributorId(Long distributorId);

    List<Payment> findByOrderRetailerIdIn(List<Long> retailerIds);

    List<Payment> findByStatus(PaymentStatus status);
}