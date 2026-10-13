package com.distributor.ordermanagement.repository;

import com.distributor.ordermanagement.entity.Distributor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DistributorRepository extends JpaRepository<Distributor, Long> {
    Optional<Distributor> findByUserUsername(String username);

    java.util.List<Distributor> findByRetailPartnersId(Long retailerId);
}
