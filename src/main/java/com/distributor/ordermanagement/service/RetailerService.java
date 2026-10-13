package com.distributor.ordermanagement.service;

import com.distributor.ordermanagement.entity.Retailer;
import com.distributor.ordermanagement.entity.User;
import com.distributor.ordermanagement.repository.RetailerRepository;
import com.distributor.ordermanagement.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import com.distributor.ordermanagement.entity.Distributor;
import com.distributor.ordermanagement.repository.DistributorRepository;

@Service
public class RetailerService {

    private final RetailerRepository retailerRepository;
    private final UserRepository userRepository;
    private final DistributorRepository distributorRepository;

    public RetailerService(RetailerRepository retailerRepository, UserRepository userRepository, DistributorRepository distributorRepository) {
        this.retailerRepository = retailerRepository;
        this.userRepository = userRepository;
        this.distributorRepository = distributorRepository;
    }

    public Retailer createRetailer(Retailer retailer) {
        return retailerRepository.save(retailer);
    }

    public List<Retailer> getAllRetailers() {
        return retailerRepository.findAll();
    }

    public Distributor getDistributorForUsername(String username) {
        return distributorRepository.findByUserUsername(username)
                .orElseGet(() -> {
                    User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
                    Distributor newDistributor = new Distributor();
                    newDistributor.setUser(user);
                    newDistributor.setBusinessName(username + " Distributor");
                    newDistributor.setContactPersonName(username);
                    newDistributor.setPhone("9999999999");
                    newDistributor.setEmail(username + "@distributor.com");
                    newDistributor.setAddress("Distributor Address");
                    newDistributor.setActive(true);
                    return distributorRepository.save(newDistributor);
                });
    }

    public List<Retailer> getMyRetailers(String username) {
        Distributor distributor = getDistributorForUsername(username);
        return List.copyOf(distributor.getRetailPartners());
    }

    public List<Distributor> getDistributorsForRetailer(Long retailerId) {
        return distributorRepository.findByRetailPartnersId(retailerId);
    }

    public boolean isRetailerPartneredWithDistributor(Long retailerId, Long distributorId) {
        return distributorRepository.findByRetailPartnersId(retailerId)
                .stream()
                .anyMatch(d -> d.getId().equals(distributorId));
    }

    public void associateRetailer(Long retailerId, String username) {
        Distributor distributor = getDistributorForUsername(username);
        Retailer retailer = getRetailerById(retailerId);
        distributor.getRetailPartners().add(retailer);
        distributorRepository.save(distributor);
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

    public Retailer getRetailerForUsername(String username) {
        return retailerRepository.findByUserUsername(username)
                .or(() -> retailerRepository.findByBusinessNameContainingIgnoreCase(username).stream().findFirst())
                .orElseGet(() -> {
                    User user = userRepository.findByUsername(username).orElse(null);
                    Retailer newRetailer = new Retailer();
                    newRetailer.setUser(user);
                    newRetailer.setBusinessName(username + " Store");
                    newRetailer.setOwnerName(username);
                    newRetailer.setPhone("9999999999");
                    newRetailer.setEmail(username + "@retailer.com");
                    newRetailer.setAddress("Retail Store Address");
                    newRetailer.setActive(true);
                    return retailerRepository.save(newRetailer);
                });
    }
}