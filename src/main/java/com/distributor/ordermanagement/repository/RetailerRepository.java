package com.distributor.ordermanagement.repository;

import com.distributor.ordermanagement.entity.Retailer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RetailerRepository extends JpaRepository<Retailer, Long> {

    List<Retailer> findByBusinessNameContainingIgnoreCase(String businessName);

    List<Retailer> findByOwnerNameContainingIgnoreCase(String ownerName);
}