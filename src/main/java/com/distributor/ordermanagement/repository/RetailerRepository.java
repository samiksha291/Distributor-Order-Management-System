package com.distributor.ordermanagement.repository;

import com.distributor.ordermanagement.entity.Retailer;
import com.distributor.ordermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RetailerRepository extends JpaRepository<Retailer, Long> {

    List<Retailer> findByBusinessNameContainingIgnoreCase(String businessName);

    List<Retailer> findByOwnerNameContainingIgnoreCase(String ownerName);

    Optional<Retailer> findByUser(User user);

    Optional<Retailer> findByUserId(Long userId);

    Optional<Retailer> findByUserUsername(String username);
}