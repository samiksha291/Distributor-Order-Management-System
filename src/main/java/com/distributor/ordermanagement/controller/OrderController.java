package com.distributor.ordermanagement.controller;

import com.distributor.ordermanagement.dto.CreateOrderRequest;
import com.distributor.ordermanagement.dto.OrderResponse;
import com.distributor.ordermanagement.entity.Order;
import com.distributor.ordermanagement.entity.OrderStatus;
import com.distributor.ordermanagement.entity.Retailer;
import com.distributor.ordermanagement.service.OrderService;
import com.distributor.ordermanagement.service.RetailerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasAnyRole('DISTRIBUTOR', 'RETAILER')")
public class OrderController {

    private final OrderService orderService;
    private final RetailerService retailerService;

    public OrderController(OrderService orderService, RetailerService retailerService) {
        this.orderService = orderService;
        this.retailerService = retailerService;
    }

    private boolean isRetailer(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RETAILER"));
    }

    private boolean isDistributor(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DISTRIBUTOR"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RETAILER')")
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Retailer currentRetailer = retailerService.getRetailerForUsername(auth.getName());
        request.setRetailerId(currentRetailer.getId());

        Order order = orderService.createOrder(request);
        return orderService.convertToResponse(order);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<Order> orders;

        if (isRetailer(auth)) {
            Retailer currentRetailer = retailerService.getRetailerForUsername(auth.getName());
            orders = orderService.getOrdersByRetailer(currentRetailer.getId());
        } else if (isDistributor(auth)) {
            com.distributor.ordermanagement.entity.Distributor currentDistributor = retailerService.getDistributorForUsername(auth.getName());
            orders = orderService.getOrdersByDistributor(currentDistributor.getId());
        } else {
            orders = orderService.getAllOrders();
        }

        return orders.stream()
                .map(orderService::convertToResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (isRetailer(auth)) {
            Retailer currentRetailer = retailerService.getRetailerForUsername(auth.getName());
            if (order.getRetailer() == null || !order.getRetailer().getId().equals(currentRetailer.getId())) {
                throw new AccessDeniedException("Access denied: You can only view your own orders");
            }
        } else if (isDistributor(auth)) {
            com.distributor.ordermanagement.entity.Distributor currentDistributor = retailerService.getDistributorForUsername(auth.getName());
            if (order.getDistributor() == null || !order.getDistributor().getId().equals(currentDistributor.getId())) {
                throw new AccessDeniedException("Access denied: You can only view your own orders");
            }
        }

        return orderService.convertToResponse(order);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public OrderResponse updateOrderStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Order order = orderService.updateOrderStatus(id, request.getStatus(), auth.getName());
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
    public List<OrderResponse> getOrdersByStatus(@PathVariable OrderStatus status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<Order> orders;

        if (isRetailer(auth)) {
            Retailer currentRetailer = retailerService.getRetailerForUsername(auth.getName());
            orders = orderService.getOrdersByRetailerAndStatus(currentRetailer.getId(), status);
        } else if (isDistributor(auth)) {
            com.distributor.ordermanagement.entity.Distributor currentDistributor = retailerService.getDistributorForUsername(auth.getName());
            orders = orderService.getOrdersByDistributorAndStatus(currentDistributor.getId(), status);
        } else {
            orders = orderService.getOrdersByStatus(status);
        }

        return orders.stream()
                .map(orderService::convertToResponse)
                .toList();
    }

    @GetMapping("/retailer/{retailerId}")
    public List<OrderResponse> getOrdersByRetailer(@PathVariable Long retailerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (isRetailer(auth)) {
            Retailer currentRetailer = retailerService.getRetailerForUsername(auth.getName());
            if (!currentRetailer.getId().equals(retailerId)) {
                throw new AccessDeniedException("Access denied: You can only view your own orders");
            }
        }

        return orderService.getOrdersByRetailer(retailerId)
                .stream()
                .map(orderService::convertToResponse)
                .toList();
    }
}