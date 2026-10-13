package com.distributor.ordermanagement.service;

import com.distributor.ordermanagement.dto.CreateOrderRequest;
import com.distributor.ordermanagement.dto.OrderItemRequest;
import com.distributor.ordermanagement.dto.OrderItemResponse;
import com.distributor.ordermanagement.dto.OrderResponse;
import com.distributor.ordermanagement.entity.*;
import com.distributor.ordermanagement.repository.OrderRepository;
import com.distributor.ordermanagement.repository.PaymentRepository;
import com.distributor.ordermanagement.repository.ProductRepository;
import com.distributor.ordermanagement.repository.RetailerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final RetailerRepository retailerRepository;
    private final PaymentRepository paymentRepository;
    private final com.distributor.ordermanagement.repository.DistributorRepository distributorRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            RetailerRepository retailerRepository,
            PaymentRepository paymentRepository,
            com.distributor.ordermanagement.repository.DistributorRepository distributorRepository) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.retailerRepository = retailerRepository;
        this.paymentRepository = paymentRepository;
        this.distributorRepository = distributorRepository;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        if (request.getRetailerId() == null) {
            throw new RuntimeException("Retailer ID is required");
        }

        if (request.getDistributorId() == null) {
            throw new RuntimeException("Distributor ID is required");
        }

        Retailer retailer = retailerRepository.findById(request.getRetailerId())
                .orElseThrow(() -> new RuntimeException("Retailer not found"));

        Distributor distributor = distributorRepository.findById(request.getDistributorId())
                .orElseThrow(() -> new RuntimeException("Distributor not found"));

        if (!distributor.getRetailPartners().contains(retailer)) {
            throw new RuntimeException("Retailer is not associated with this Distributor");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one product item");
        }

        Order order = new Order();
        order.setRetailer(retailer);
        order.setDistributor(distributor);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {

            if (itemRequest.getProductId() == null) {
                throw new RuntimeException("Product ID is required for each order item");
            }

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getDistributor() == null || !product.getDistributor().getId().equals(distributor.getId())) {
                throw new RuntimeException("Product " + product.getName() + " does not belong to the selected distributor");
            }

            Integer quantity = itemRequest.getQuantity();

            if (quantity == null || quantity <= 0) {
                throw new RuntimeException("Quantity must be greater than 0");
            }

            if (product.getStockQuantity() < quantity) {
                throw new RuntimeException(
                        "Insufficient stock for product: " + product.getName() +
                                ". Available: " + product.getStockQuantity() + ", requested: " + quantity
                );
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(subtotal);

            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(subtotal);

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepository.save(product);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Automatically initialize payment record in PENDING status
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setTotalAmount(totalAmount);
        payment.setPaidAmount(BigDecimal.ZERO);
        payment.setOutstandingAmount(totalAmount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        return savedOrder;
    }

    public OrderResponse convertToResponse(Order order) {

        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setRetailerId(order.getRetailer().getId());
        response.setRetailerName(order.getRetailer().getBusinessName());

        if (order.getDistributor() != null) {
            response.setDistributorId(order.getDistributor().getId());
            response.setDistributorName(order.getDistributor().getBusinessName());
        }

        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());

        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(item -> {
                    OrderItemResponse itemResponse = new OrderItemResponse();
                    itemResponse.setProductId(item.getProduct().getId());
                    itemResponse.setProductName(item.getProduct().getName());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setUnitPrice(item.getUnitPrice());
                    itemResponse.setSubtotal(item.getSubtotal());
                    return itemResponse;
                })
                .toList();

        response.setItems(itemResponses);

        return response;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Transactional
    public Order updateOrderStatus(Long id, OrderStatus newStatus, String username) {

        Order order = getOrderById(id);
        Distributor distributor = distributorRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Distributor not found"));

        if (order.getDistributor() == null || !order.getDistributor().getId().equals(distributor.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You can only update status for your own orders");
        }

        OrderStatus currentStatus = order.getStatus();

        boolean validTransition =
                (currentStatus == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED)
                        || (currentStatus == OrderStatus.CONFIRMED && newStatus == OrderStatus.PACKED)
                        || (currentStatus == OrderStatus.PACKED && newStatus == OrderStatus.SHIPPED)
                        || (currentStatus == OrderStatus.SHIPPED && newStatus == OrderStatus.DELIVERED);

        if (!validTransition) {
            throw new RuntimeException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus
            );
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersByDistributor(Long distributorId) {
        return orderRepository.findByDistributorId(distributorId);
    }

    public List<Order> getOrdersByDistributorAndStatus(Long distributorId, OrderStatus status) {
        return orderRepository.findByDistributorIdAndStatus(distributorId, status);
    }

    public List<Order> getOrdersByRetailer(Long retailerId) {
        return orderRepository.findByRetailerId(retailerId);
    }

    public List<Order> getOrdersByRetailerAndStatus(Long retailerId, OrderStatus status) {
        return orderRepository.findByRetailerIdAndStatus(retailerId, status);
    }

    public List<Order> getOrdersByRetailerIds(List<Long> retailerIds) {
        if (retailerIds.isEmpty()) return List.of();
        return orderRepository.findByRetailerIdIn(retailerIds);
    }

    public List<Order> getOrdersByRetailerIdsAndStatus(List<Long> retailerIds, OrderStatus status) {
        if (retailerIds.isEmpty()) return List.of();
        return orderRepository.findByRetailerIdInAndStatus(retailerIds, status);
    }
}