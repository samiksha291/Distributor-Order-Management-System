package com.distributor.ordermanagement.controller;

import com.distributor.ordermanagement.dto.DashboardResponse;
import com.distributor.ordermanagement.entity.Retailer;
import com.distributor.ordermanagement.service.DashboardService;
import com.distributor.ordermanagement.service.RetailerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('DISTRIBUTOR', 'RETAILER')")
public class DashboardController {

    private final DashboardService dashboardService;
    private final RetailerService retailerService;

    public DashboardController(DashboardService dashboardService, RetailerService retailerService) {
        this.dashboardService = dashboardService;
        this.retailerService = retailerService;
    }

    @GetMapping
    public DashboardResponse getDashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RETAILER"))) {
                Retailer currentRetailer = retailerService.getRetailerForUsername(auth.getName());
                return dashboardService.getDashboardForRetailer(currentRetailer.getId());
            } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DISTRIBUTOR"))) {
                return dashboardService.getDashboardForDistributor(auth.getName());
            }
        }
        return dashboardService.getDashboard();
    }
}