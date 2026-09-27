package com.distributor.ordermanagement.service;

import com.distributor.ordermanagement.entity.Retailer;
import com.distributor.ordermanagement.repository.RetailerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RetailerService {

    private final RetailerRepository retailerRepository;

    public RetailerService(RetailerRepository retailerRepository) {
        this.retailerRepository = retailerRepository;
    }

    public Retailer createRetailer(Retailer retailer) {
        return retailerRepository.save(retailer);
    }

    public List<Retailer> getAllRetailers() {
        return retailerRepository.findAll();
    }

    public Retailer getRetailerById(Long id) {
        return retailerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Retailer not found"));
    }

    public Retailer updateRetailer(Long id, Retailer updatedRetailer) {

        Retailer retailer = getRetailerById(id);

        retailer.setBusinessName(updatedRetailer.getBusinessName());
        retailer.setOwnerName(updatedRetailer.getOwnerName());
        retailer.setPhone(updatedRetailer.getPhone());
        retailer.setEmail(updatedRetailer.getEmail());
        retailer.setAddress(updatedRetailer.getAddress());
        retailer.setActive(updatedRetailer.getActive());

        return retailerRepository.save(retailer);
    }

    public void deleteRetailer(Long id) {
        Retailer retailer = getRetailerById(id);
        retailerRepository.delete(retailer);
    }
    public List<Retailer> searchRetailers(String name) {
        return retailerRepository.findByBusinessNameContainingIgnoreCase(name);
    }
}