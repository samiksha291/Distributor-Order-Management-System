package com.distributor.ordermanagement.repository;

import com.distributor.ordermanagement.entity.Order;
import com.distributor.ordermanagement.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByDistributorId(Long distributorId);

    List<Order> findByDistributorIdAndStatus(Long distributorId, OrderStatus status);

    long countByDistributorId(Long distributorId);

    List<Order> findByRetailerId(Long retailerId);

    List<Order> findByRetailerIdAndStatus(Long retailerId, OrderStatus status);

    List<Order> findByRetailerIdIn(List<Long> retailerIds);

    List<Order> findByRetailerIdInAndStatus(List<Long> retailerIds, OrderStatus status);
}