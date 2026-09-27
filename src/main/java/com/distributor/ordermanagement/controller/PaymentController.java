package com.distributor.ordermanagement.controller;

import com.distributor.ordermanagement.dto.PaymentRequest;
import com.distributor.ordermanagement.dto.PaymentResponse;
import com.distributor.ordermanagement.entity.Payment;
import com.distributor.ordermanagement.service.PaymentService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasAnyRole('ADMIN', 'DISTRIBUTOR', 'RETAILER')")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders/{orderId}")
    public PaymentResponse makePayment(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequest request) {

        Payment payment = paymentService.makePayment(orderId, request);

        return paymentService.convertToResponse(payment);
    }

    @GetMapping("/orders/{orderId}")
    public PaymentResponse getPaymentByOrderId(
            @PathVariable Long orderId) {

        Payment payment = paymentService.getPaymentByOrderId(orderId);

        return paymentService.convertToResponse(payment);
    }

    @GetMapping
    public List<PaymentResponse> getAllPayments() {

        return paymentService.getAllPayments()
                .stream()
                .map(paymentService::convertToResponse)
                .toList();
    }
}