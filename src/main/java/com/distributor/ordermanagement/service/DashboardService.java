package com.distributor.ordermanagement.service;

import com.distributor.ordermanagement.dto.DashboardResponse;
import com.distributor.ordermanagement.entity.Order;
import com.distributor.ordermanagement.entity.OrderStatus;
import com.distributor.ordermanagement.entity.Payment;
import com.distributor.ordermanagement.repository.OrderRepository;
import com.distributor.ordermanagement.repository.PaymentRepository;
import com.distributor.ordermanagement.repository.ProductRepository;
import com.distributor.ordermanagement.repository.RetailerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private final ProductRepository productRepository;
    private final RetailerRepository retailerRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RetailerService retailerService;

    public DashboardService(
            ProductRepository productRepository,
            RetailerRepository retailerRepository,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            RetailerService retailerService) {

        this.productRepository = productRepository;
        this.retailerRepository = retailerRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.retailerService = retailerService;
    }

    public DashboardResponse getDashboardForDistributor(String username) {
        DashboardResponse response = new DashboardResponse();
        
        com.distributor.ordermanagement.entity.Distributor distributor = retailerService.getDistributorForUsername(username);
        List<com.distributor.ordermanagement.entity.Product> myProducts = productRepository.findByDistributorId(distributor.getId());

        response.setTotalProducts(myProducts.size());
        response.setTotalRetailers(distributor.getRetailPartners().size());
        
        List<Order> orders = orderRepository.findByDistributorId(distributor.getId());
        List<Payment> payments = paymentRepository.findByOrderDistributorId(distributor.getId());

        response.setTotalOrders(orders.size());

        long pendingOrders = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .count();

        long deliveredOrders = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .count();

        long lowStockProducts = myProducts.stream()
                .filter(product ->
                        product.getStockQuantity() <= product.getReorderLevel()
                )
                .count();

        BigDecimal totalSales = orders.stream()
                .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaidAmount = payments.stream()
                .map(payment -> payment.getPaidAmount() != null ? payment.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutstanding = payments.stream()
                .map(payment -> payment.getOutstandingAmount() != null ? payment.getOutstandingAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setPendingOrders(pendingOrders);
        response.setDeliveredOrders(deliveredOrders);
        response.setLowStockProducts(lowStockProducts);
        response.setTotalSales(totalSales);
        response.setTotalPaidAmount(totalPaidAmount);
        response.setTotalOutstanding(totalOutstanding);

        return response;
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
                .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaidAmount = paymentRepository.findAll()
                .stream()
                .map(payment -> payment.getPaidAmount() != null ? payment.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutstanding = paymentRepository.findAll()
                .stream()
                .map(payment -> payment.getOutstandingAmount() != null ? payment.getOutstandingAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setPendingOrders(pendingOrders);
        response.setDeliveredOrders(deliveredOrders);
        response.setLowStockProducts(lowStockProducts);
        response.setTotalSales(totalSales);
        response.setTotalPaidAmount(totalPaidAmount);
        response.setTotalOutstanding(totalOutstanding);

        return response;
    }

    public DashboardResponse getDashboardForRetailer(Long retailerId) {

        DashboardResponse response = new DashboardResponse();

        response.setTotalProducts(productRepository.count());
        response.setTotalRetailers(1);

        List<Order> retailerOrders = orderRepository.findByRetailerId(retailerId);
        response.setTotalOrders(retailerOrders.size());

        long pendingOrders = retailerOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .count();

        long deliveredOrders = retailerOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .count();

        response.setLowStockProducts(0);

        BigDecimal totalSales = retailerOrders.stream()
                .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Payment> retailerPayments = paymentRepository.findByOrderRetailerId(retailerId);

        BigDecimal totalPaidAmount = retailerPayments.stream()
                .map(payment -> payment.getPaidAmount() != null ? payment.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutstanding = retailerPayments.stream()
                .map(payment -> payment.getOutstandingAmount() != null ? payment.getOutstandingAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setPendingOrders(pendingOrders);
        response.setDeliveredOrders(deliveredOrders);
        response.setTotalSales(totalSales);
        response.setTotalPaidAmount(totalPaidAmount);
        response.setTotalOutstanding(totalOutstanding);

        return response;
    }
}