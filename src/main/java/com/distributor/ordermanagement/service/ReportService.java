package com.distributor.ordermanagement.service;

import com.distributor.ordermanagement.dto.ReportResponse;
import com.distributor.ordermanagement.entity.Order;
import com.distributor.ordermanagement.entity.OrderStatus;
import com.distributor.ordermanagement.entity.Payment;
import com.distributor.ordermanagement.entity.PaymentStatus;
import com.distributor.ordermanagement.entity.Product;
import com.distributor.ordermanagement.repository.OrderRepository;
import com.distributor.ordermanagement.repository.PaymentRepository;
import com.distributor.ordermanagement.repository.ProductRepository;
import com.distributor.ordermanagement.repository.RetailerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ReportService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final RetailerRepository retailerRepository;
    private final RetailerService retailerService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public ReportService(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            ProductRepository productRepository,
            RetailerRepository retailerRepository,
            RetailerService retailerService,
            OrderService orderService,
            PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.retailerRepository = retailerRepository;
        this.retailerService = retailerService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    public ReportResponse generateReport(String username, boolean isDistributor) {
        ReportResponse report = new ReportResponse();

        List<Order> orders;
        List<Payment> payments;
        List<Product> products;
        long totalRetailers;

        if (isDistributor) {
            com.distributor.ordermanagement.entity.Distributor distributor = retailerService.getDistributorForUsername(username);
            orders = orderService.getOrdersByDistributor(distributor.getId());
            payments = paymentService.getPaymentsByDistributor(distributor.getId());
            totalRetailers = distributor.getRetailPartners().size();
            products = productRepository.findByDistributorId(distributor.getId());
        } else {
            orders = orderRepository.findAll();
            payments = paymentRepository.findAll();
            totalRetailers = retailerRepository.count();
            products = productRepository.findAll();
        }

        // 1. Orders & Sales
        long totalOrders = orders.size();
        BigDecimal totalSales = orders.stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (totalOrders > 0 && totalSales.compareTo(BigDecimal.ZERO) > 0) {
            averageOrderValue = totalSales.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        }

        long pendingOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long confirmedOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.CONFIRMED).count();
        long packedOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.PACKED).count();
        long shippedOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count();
        long deliveredOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();

        // 2. Payments
        BigDecimal totalPaid = payments.stream()
                .map(p -> p.getPaidAmount() != null ? p.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOutstanding = payments.stream()
                .map(p -> p.getOutstandingAmount() != null ? p.getOutstandingAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingPayments = payments.stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).count();
        long partialPayments = payments.stream().filter(p -> p.getStatus() == PaymentStatus.PARTIAL).count();
        long paidPayments = payments.stream().filter(p -> p.getStatus() == PaymentStatus.PAID).count();

        // 3. Inventory
        long totalProducts = products.size();
        long lowStockProducts = products.stream()
                .filter(p -> p.getStockQuantity() != null && p.getReorderLevel() != null && p.getStockQuantity() <= p.getReorderLevel())
                .count();
        long totalStock = products.stream()
                .mapToLong(p -> p.getStockQuantity() != null ? p.getStockQuantity() : 0L)
                .sum();

        // Populate Report DTO
        report.setTotalSales(totalSales);
        report.setTotalOrders(totalOrders);
        report.setAverageOrderValue(averageOrderValue);

        report.setPendingOrders(pendingOrders);
        report.setConfirmedOrders(confirmedOrders);
        report.setPackedOrders(packedOrders);
        report.setShippedOrders(shippedOrders);
        report.setDeliveredOrders(deliveredOrders);

        report.setTotalPaid(totalPaid);
        report.setTotalOutstanding(totalOutstanding);
        report.setPendingPayments(pendingPayments);
        report.setPartialPayments(partialPayments);
        report.setPaidPayments(paidPayments);

        report.setTotalProducts(totalProducts);
        report.setLowStockProducts(lowStockProducts);
        report.setTotalStock(totalStock);

        report.setTotalRetailers(totalRetailers);

        return report;
    }
}
