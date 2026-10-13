package com.distributor.ordermanagement.repository;

import com.distributor.ordermanagement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByDistributorId(Long distributorId);

    List<Product> findByDistributorIdAndNameContainingIgnoreCase(Long distributorId, String name);

    List<Product> findByDistributorIdAndCategoryIgnoreCase(Long distributorId, String category);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryIgnoreCase(String category);
}