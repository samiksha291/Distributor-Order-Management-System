package com.distributor.ordermanagement.controller;

import com.distributor.ordermanagement.entity.Retailer;
import com.distributor.ordermanagement.service.RetailerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/retailers")
public class RetailerController {

    private final RetailerService retailerService;

    public RetailerController(RetailerService retailerService) {
        this.retailerService = retailerService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DISTRIBUTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public Retailer createRetailer(@Valid @RequestBody Retailer retailer) {
        return retailerService.createRetailer(retailer);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DISTRIBUTOR', 'RETAILER')")
    public List<Retailer> getAllRetailers() {
        return retailerService.getAllRetailers();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public List<Retailer> getMyRetailers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return retailerService.getMyRetailers(auth.getName());
    }

    @GetMapping("/my-distributors")
    @PreAuthorize("hasRole('RETAILER')")
    public List<com.distributor.ordermanagement.entity.Distributor> getMyDistributors() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Retailer currentRetailer = retailerService.getRetailerForUsername(auth.getName());
        return retailerService.getDistributorsForRetailer(currentRetailer.getId());
    }

    @PostMapping("/{id}/associate")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    @ResponseStatus(HttpStatus.OK)
    public void associateRetailer(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        retailerService.associateRetailer(id, auth.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR', 'RETAILER')")
    public Retailer getRetailerById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RETAILER"))) {
            Retailer currentRetailer = retailerService.getRetailerForUsername(auth.getName());
            if (!currentRetailer.getId().equals(id)) {
                throw new AccessDeniedException("Access denied: You can only access your own retailer information");
            }
        }
        return retailerService.getRetailerById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR')")
    public Retailer updateRetailer(
            @PathVariable Long id,
            @Valid @RequestBody Retailer retailer) {

        return retailerService.updateRetailer(id, retailer);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRetailer(@PathVariable Long id) {
        retailerService.deleteRetailer(id);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR')")
    public List<Retailer> searchRetailers(@RequestParam String name) {
        return retailerService.searchRetailers(name);
    }
}
