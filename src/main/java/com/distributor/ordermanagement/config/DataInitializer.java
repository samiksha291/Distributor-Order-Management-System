package com.distributor.ordermanagement.config;

import com.distributor.ordermanagement.entity.Distributor;
import com.distributor.ordermanagement.entity.Product;
import com.distributor.ordermanagement.entity.Role;
import com.distributor.ordermanagement.entity.User;
import com.distributor.ordermanagement.repository.DistributorRepository;
import com.distributor.ordermanagement.repository.OrderRepository;
import com.distributor.ordermanagement.repository.ProductRepository;
import com.distributor.ordermanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DistributorRepository distributorRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public DataInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            DistributorRepository distributorRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.distributorRepository = distributorRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        User defaultUser = userRepository.findByUsername("distributor").orElse(null);
        if (defaultUser == null) {
            defaultUser = new User();
            defaultUser.setUsername("distributor");
            defaultUser.setPassword(passwordEncoder.encode("distributor123"));
            defaultUser.setRole(Role.DISTRIBUTOR);
            defaultUser.setActive(true);
            defaultUser = userRepository.save(defaultUser);
        }

        final User finalDefaultUser = defaultUser;
        Distributor defaultDistributor = distributorRepository.findByUserUsername("distributor")
                .orElseGet(() -> {
                    Distributor d = new Distributor();
                    d.setUser(finalDefaultUser);
                    d.setBusinessName("Global Supplies");
                    d.setContactPersonName("Main Distributor");
                    d.setPhone("9876543210");
                    d.setEmail("distributor@example.com");
                    d.setAddress("Distributor Central Hub");
                    d.setActive(true);
                    return distributorRepository.save(d);
                });

        // Assign any legacy products with null distributor to defaultDistributor
        List<Product> orphanProducts = productRepository.findAll().stream()
                .filter(p -> p.getDistributor() == null)
                .toList();
        for (Product p : orphanProducts) {
            p.setDistributor(defaultDistributor);
            productRepository.save(p);
        }

        // Assign any legacy orders with null distributor to defaultDistributor
        List<com.distributor.ordermanagement.entity.Order> orphanOrders = orderRepository.findAll().stream()
                .filter(o -> o.getDistributor() == null)
                .toList();
        for (com.distributor.ordermanagement.entity.Order o : orphanOrders) {
            o.setDistributor(defaultDistributor);
            orderRepository.save(o);
        }
    }
}
