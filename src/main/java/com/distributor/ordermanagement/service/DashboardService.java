package com.distributor.ordermanagement.service;

import com.distributor.ordermanagement.dto.DashboardResponse;
import com.distributor.ordermanagement.entity.OrderStatus;
import com.distributor.ordermanagement.repository.OrderRepository;
import com.distributor.ordermanagement.repository.PaymentRepository;
import com.distributor.ordermanagement.repository.ProductRepository;
import com.distributor.ordermanagement.repository.RetailerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardService {

    private final ProductRepository productRepository;
    private final RetailerRepository retailerRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public DashboardService(
            ProductRepository productRepository,
            RetailerRepository retailerRepository,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository) {

        this.productRepository = productRepository;
        this.retailerRepository = retailerRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    public DashboardResponse getDashboard() {

        DashboardResponse response = new DashboardResponse();

        response.setTotalProducts(productRepository.count());
        response.setTotalRetailers(retailerRepository.count());
        response.setTotalOrders(orderRepository.count());

        long pendingOrders = orderRepository.findAll()
                .stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .count();

        long deliveredOrders = orderRepository.findAll()
                .stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .count();

        long lowStockProducts = productRepository.findAll()
                .stream()
                .filter(product ->
                        product.getStockQuantity() <= product.getReorderLevel()
                )
                .count();

        BigDecimal totalSales = orderRepository.findAll()
                .stream()
                .map(order -> order.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaidAmount = paymentRepository.findAll()
                .stream()
                .map(payment -> payment.getPaidAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutstanding = paymentRepository.findAll()
                .stream()
                .map(payment -> payment.getOutstandingAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setPendingOrders(pendingOrders);
        response.setDeliveredOrders(deliveredOrders);
        response.setLowStockProducts(lowStockProducts);
        response.setTotalSales(totalSales);
        response.setTotalPaidAmount(totalPaidAmount);
        response.setTotalOutstanding(totalOutstanding);

        return response;
    }
}