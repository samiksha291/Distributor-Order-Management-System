package com.distributor.ordermanagement.service;

import com.distributor.ordermanagement.dto.PaymentRequest;
import com.distributor.ordermanagement.dto.PaymentResponse;
import com.distributor.ordermanagement.entity.Order;
import com.distributor.ordermanagement.entity.Payment;
import com.distributor.ordermanagement.entity.PaymentStatus;
import com.distributor.ordermanagement.repository.OrderRepository;
import com.distributor.ordermanagement.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository) {

        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Payment makePayment(Long orderId, PaymentRequest request) {

        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException("Payment amount must be greater than 0");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElse(null);

        if (payment == null) {
            payment = new Payment();
            payment.setOrder(order);
            payment.setTotalAmount(order.getTotalAmount());
            payment.setPaidAmount(BigDecimal.ZERO);
            payment.setOutstandingAmount(order.getTotalAmount());
            payment.setStatus(PaymentStatus.PENDING);
        }

        BigDecimal newPaidAmount =
                payment.getPaidAmount().add(request.getAmount());

        if (newPaidAmount.compareTo(payment.getTotalAmount()) > 0) {
            throw new RuntimeException(
                    "Payment cannot be greater than order total. Remaining outstanding: ₹" +
                            payment.getOutstandingAmount()
            );
        }

        BigDecimal outstandingAmount =
                payment.getTotalAmount().subtract(newPaidAmount);

        payment.setPaidAmount(newPaidAmount);
        payment.setOutstandingAmount(outstandingAmount);
        payment.setPaymentDate(LocalDateTime.now());

        if (outstandingAmount.compareTo(BigDecimal.ZERO) == 0) {
            payment.setStatus(PaymentStatus.PAID);
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            payment.setStatus(PaymentStatus.PARTIAL);
        } else {
            payment.setStatus(PaymentStatus.PENDING);
        }

        return paymentRepository.save(payment);
    }

    public Payment getPaymentByOrderId(Long orderId) {

        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException(
                        "Payment record not found for this order"
                ));
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByRetailer(Long retailerId) {
        return paymentRepository.findByOrderRetailerId(retailerId);
    }

    public List<Payment> getPaymentsByDistributor(Long distributorId) {
        return paymentRepository.findByOrderDistributorId(distributorId);
    }

    public List<Payment> getPaymentsByRetailerIds(List<Long> retailerIds) {
        if (retailerIds.isEmpty()) return List.of();
        return paymentRepository.findByOrderRetailerIdIn(retailerIds);
    }

    public PaymentResponse convertToResponse(Payment payment) {

        PaymentResponse response = new PaymentResponse();

        response.setId(payment.getId());
        response.setOrderId(payment.getOrder().getId());
        response.setTotalAmount(payment.getTotalAmount());
        response.setPaidAmount(payment.getPaidAmount());
        response.setOutstandingAmount(payment.getOutstandingAmount());
        response.setStatus(payment.getStatus());
        response.setPaymentDate(payment.getPaymentDate());

        return response;
    }
}