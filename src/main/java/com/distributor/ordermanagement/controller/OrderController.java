package com.distributor.ordermanagement.controller;

import com.distributor.ordermanagement.dto.CreateOrderRequest;
import com.distributor.ordermanagement.dto.OrderResponse;
import com.distributor.ordermanagement.entity.Order;
import com.distributor.ordermanagement.entity.OrderStatus;
import com.distributor.ordermanagement.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasAnyRole('ADMIN', 'DISTRIBUTOR', 'RETAILER')")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder( @Valid @RequestBody CreateOrderRequest request) {

        Order order = orderService.createOrder(request);

        return orderService.convertToResponse(order);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {

        return orderService.getAllOrders()
                .stream()
                .map(orderService::convertToResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {

        Order order = orderService.getOrderById(id);

        return orderService.convertToResponse(order);
    }

    @PutMapping("/{id}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {

        Order order = orderService.updateOrderStatus(
                id,
                request.getStatus()
        );

        return orderService.convertToResponse(order);
    }

    public static class StatusUpdateRequest {

        private OrderStatus status;

        public StatusUpdateRequest() {
        }

        public OrderStatus getStatus() {
            return status;
        }

        public void setStatus(OrderStatus status) {
            this.status = status;
        }
    }
    @GetMapping("/status/{status}")
    public List<OrderResponse> getOrdersByStatus(
            @PathVariable OrderStatus status) {

        return orderService.getOrdersByStatus(status)
                .stream()
                .map(orderService::convertToResponse)
                .toList();
    }

    @GetMapping("/retailer/{retailerId}")
    public List<OrderResponse> getOrdersByRetailer(
            @PathVariable Long retailerId) {

        return orderService.getOrdersByRetailer(retailerId)
                .stream()
                .map(orderService::convertToResponse)
                .toList();
    }
}