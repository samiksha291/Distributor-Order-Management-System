package com.distributor.ordermanagement.service;

import com.distributor.ordermanagement.dto.CreateOrderRequest;
import com.distributor.ordermanagement.dto.OrderItemRequest;
import com.distributor.ordermanagement.entity.Order;
import com.distributor.ordermanagement.entity.OrderItem;
import com.distributor.ordermanagement.entity.OrderStatus;
import com.distributor.ordermanagement.entity.Product;
import com.distributor.ordermanagement.entity.Retailer;
import com.distributor.ordermanagement.repository.OrderRepository;
import com.distributor.ordermanagement.repository.ProductRepository;
import com.distributor.ordermanagement.repository.RetailerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.distributor.ordermanagement.dto.OrderResponse;
import com.distributor.ordermanagement.dto.OrderItemResponse;
import java.util.List;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final RetailerRepository retailerRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            RetailerRepository retailerRepository) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.retailerRepository = retailerRepository;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        Retailer retailer = retailerRepository.findById(request.getRetailerId())
                .orElseThrow(() -> new RuntimeException("Retailer not found"));

        Order order = new Order();

        order.setRetailer(retailer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            Integer quantity = itemRequest.getQuantity();

            if (quantity == null || quantity <= 0) {
                throw new RuntimeException("Quantity must be greater than 0");
            }

            if (product.getStockQuantity() < quantity) {
                throw new RuntimeException(
                        "Insufficient stock for product: " + product.getName()
                );
            }

            BigDecimal unitPrice = product.getPrice();

            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(quantity)
            );

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(subtotal);

            order.getItems().add(orderItem);

            totalAmount = totalAmount.add(subtotal);

            product.setStockQuantity(
                    product.getStockQuantity() - quantity
            );

            productRepository.save(product);
        }

        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }
    public OrderResponse convertToResponse(Order order) {

        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setRetailerId(order.getRetailer().getId());
        response.setRetailerName(order.getRetailer().getBusinessName());
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
    public Order updateOrderStatus(Long id, OrderStatus newStatus) {

        Order order = getOrderById(id);

        OrderStatus currentStatus = order.getStatus();

        boolean validTransition =
                (currentStatus == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED)
                        || (currentStatus == OrderStatus.CONFIRMED && newStatus == OrderStatus.PACKED)
                        || (currentStatus == OrderStatus.PACKED && newStatus == OrderStatus.SHIPPED)
                        || (currentStatus == OrderStatus.SHIPPED && newStatus == OrderStatus.DELIVERED);

        if (!validTransition) {
            throw new RuntimeException(
                    "Invalid status transition from "
                            + currentStatus + " to " + newStatus
            );
        }

        order.setStatus(newStatus);

        return orderRepository.save(order);
    }
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersByRetailer(Long retailerId) {
        return orderRepository.findByRetailerId(retailerId);
    }
}