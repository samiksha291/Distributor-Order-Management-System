package com.distributor.ordermanagement.service;

import com.distributor.ordermanagement.dto.LoginRequest;
import com.distributor.ordermanagement.dto.LoginResponse;
import com.distributor.ordermanagement.dto.RegisterRequest;
import com.distributor.ordermanagement.dto.RegisterResponse;

import com.distributor.ordermanagement.entity.Distributor;
import com.distributor.ordermanagement.entity.Retailer;
import com.distributor.ordermanagement.entity.Role;
import com.distributor.ordermanagement.entity.User;

import com.distributor.ordermanagement.repository.DistributorRepository;
import com.distributor.ordermanagement.repository.RetailerRepository;
import com.distributor.ordermanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RetailerRepository retailerRepository;
    private final DistributorRepository distributorRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RetailerRepository retailerRepository,
            DistributorRepository distributorRepository,

            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.retailerRepository = retailerRepository;
        this.distributorRepository = distributorRepository;

        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    // =========================================================
    // REGISTER
    // =========================================================

    public RegisterResponse register(RegisterRequest request) {

        if (request.getUsername() == null ||
                request.getUsername().trim().isEmpty()) {

            throw new RuntimeException("Username is required");
        }


        if (request.getPassword() == null ||
                request.getPassword().length() < 6) {

            throw new RuntimeException(
                    "Password must contain at least 6 characters"
            );
        }


        if (request.getRole() == null) {
            throw new RuntimeException("Please select a valid Account Type");
        }

        String username = request.getUsername().trim();

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.DISTRIBUTOR) {
            if (request.getBusinessName() == null || request.getBusinessName().trim().isEmpty() ||
                request.getContactPersonName() == null || request.getContactPersonName().trim().isEmpty() ||
                request.getPhone() == null || request.getPhone().trim().isEmpty()) {
                throw new RuntimeException("Business Name, Contact Person Name, and Phone are required for Distributor");
            }
            Distributor distributor = new Distributor();
            distributor.setUser(savedUser);
            distributor.setBusinessName(request.getBusinessName().trim());
            distributor.setContactPersonName(request.getContactPersonName().trim());
            distributor.setPhone(request.getPhone().trim());
            distributor.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
            distributor.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
            distributor.setActive(true);
            distributorRepository.save(distributor);
        } else if (savedUser.getRole() == Role.RETAILER) {
            if (request.getBusinessName() == null || request.getBusinessName().trim().isEmpty() ||
                request.getOwnerName() == null || request.getOwnerName().trim().isEmpty() ||
                request.getPhone() == null || request.getPhone().trim().isEmpty()) {
                throw new RuntimeException("Business Name, Owner Name, and Phone are required for Retailer");
            }
            Retailer retailer = new Retailer();
            retailer.setUser(savedUser);
            retailer.setBusinessName(request.getBusinessName().trim());
            retailer.setOwnerName(request.getOwnerName().trim());
            retailer.setPhone(request.getPhone().trim());
            retailer.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
            retailer.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
            retailer.setActive(true);
            retailerRepository.save(retailer);
        }

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole().name()
        );
    }


    // =========================================================
    // LOGIN
    // =========================================================

    public LoginResponse login(LoginRequest request) {

        User user =
                userRepository
                        .findByUsername(request.getUsername())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid username or password"
                                )
                        );


        if (!user.getActive()) {

            throw new RuntimeException(
                    "User account is inactive"
            );
        }


        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException(
                    "Invalid username or password"
            );
        }


        // Your existing JwtService expects User
        String token =
                jwtService.generateToken(user);


        return new LoginResponse(
                token,
                user.getUsername(),
                user.getRole().name()
        );
    }
}