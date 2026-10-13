package com.distributor.ordermanagement.controller;

import com.distributor.ordermanagement.dto.PaymentRequest;
import com.distributor.ordermanagement.dto.PaymentResponse;
import com.distributor.ordermanagement.entity.Order;
import com.distributor.ordermanagement.entity.Payment;
import com.distributor.ordermanagement.entity.Retailer;
import com.distributor.ordermanagement.service.OrderService;
import com.distributor.ordermanagement.service.PaymentService;
import com.distributor.ordermanagement.service.RetailerService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasAnyRole('DISTRIBUTOR', 'RETAILER')")
public class PaymentController {

    private final PaymentService paymentService;
    private final RetailerService retailerService;
    private final OrderService orderService;

    public PaymentController(
            PaymentService paymentService,
            RetailerService retailerService,
            OrderService orderService) {
        this.paymentService = paymentService;
        this.retailerService = retailerService;
        this.orderService = orderService;
    }

    private boolean isRetailer(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RETAILER"));
    }

    private boolean isDistributor(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DISTRIBUTOR"));
    }

    @PostMapping("/orders/{orderId}")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR', 'RETAILER')")
    public PaymentResponse makePayment(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Order order = orderService.getOrderById(orderId);

        if (isRetailer(auth)) {
            Retailer currentRetailer = retailerService.getRetailerForUsername(auth.getName());
            if (order.getRetailer() == null || !order.getRetailer().getId().equals(currentRetailer.getId())) {
                throw new AccessDeniedException("Access denied: You can only submit payments for your own orders");
            }
        } else if (isDistributor(auth)) {
            com.distributor.ordermanagement.entity.Distributor currentDistributor = retailerService.getDistributorForUsername(auth.getName());
            if (order.getDistributor() == null || !order.getDistributor().getId().equals(currentDistributor.getId())) {
                throw new AccessDeniedException("Access denied: You can only record payments for your own orders");
            }
        }

        Payment payment = paymentService.makePayment(orderId, request);
        return paymentService.convertToResponse(payment);
    }

    @GetMapping("/orders/{orderId}")
    public PaymentResponse getPaymentByOrderId(@PathVariable Long orderId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Payment payment = paymentService.getPaymentByOrderId(orderId);

        if (isRetailer(auth)) {
            Retailer currentRetailer = retailerService.getRetailerForUsername(auth.getName());
            if (payment.getOrder().getRetailer() == null || !payment.getOrder().getRetailer().getId().equals(currentRetailer.getId())) {
                throw new AccessDeniedException("Access denied: You can only view payments for your own orders");
            }
        } else if (isDistributor(auth)) {
            com.distributor.ordermanagement.entity.Distributor currentDistributor = retailerService.getDistributorForUsername(auth.getName());
            if (payment.getOrder().getDistributor() == null || !payment.getOrder().getDistributor().getId().equals(currentDistributor.getId())) {
                throw new AccessDeniedException("Access denied: You can only view payments for your own orders");
            }
        }

        return paymentService.convertToResponse(payment);
    }

    @GetMapping
    public List<PaymentResponse> getAllPayments() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<Payment> payments;

        if (isRetailer(auth)) {
            Retailer currentRetailer = retailerService.getRetailerForUsername(auth.getName());
            payments = paymentService.getPaymentsByRetailer(currentRetailer.getId());
        } else if (isDistributor(auth)) {
            com.distributor.ordermanagement.entity.Distributor currentDistributor = retailerService.getDistributorForUsername(auth.getName());
            payments = paymentService.getPaymentsByDistributor(currentDistributor.getId());
        } else {
            payments = paymentService.getAllPayments();
        }

        return payments.stream()
                .map(paymentService::convertToResponse)
                .toList();
    }
}