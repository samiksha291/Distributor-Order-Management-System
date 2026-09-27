package com.distributor.ordermanagement.repository;

import com.distributor.ordermanagement.entity.Order;
import com.distributor.ordermanagement.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByRetailerId(Long retailerId);
}