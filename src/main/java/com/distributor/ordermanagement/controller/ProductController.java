package com.distributor.ordermanagement.controller;

import com.distributor.ordermanagement.entity.Distributor;
import com.distributor.ordermanagement.entity.Product;
import com.distributor.ordermanagement.entity.Retailer;
import com.distributor.ordermanagement.service.ProductService;
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
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final RetailerService retailerService;

    public ProductController(ProductService productService, RetailerService retailerService) {
        this.productService = productService;
        this.retailerService = retailerService;
    }

    private boolean isRetailer(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RETAILER"));
    }

    private boolean isDistributor(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DISTRIBUTOR"));
    }

    @PostMapping
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@Valid @RequestBody Product product) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return productService.createProduct(product, auth.getName());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DISTRIBUTOR', 'RETAILER')")
    public List<Product> getAllProducts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isDistributor(auth)) {
            return productService.getProductsForDistributor(auth.getName());
        }
        return productService.getAllProducts();
    }

    @GetMapping("/distributor/{distributorId}")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR', 'RETAILER')")
    public List<Product> getProductsByDistributor(@PathVariable Long distributorId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isRetailer(auth)) {
            Retailer currentRetailer = retailerService.getRetailerForUsername(auth.getName());
            if (!retailerService.isRetailerPartneredWithDistributor(currentRetailer.getId(), distributorId)) {
                throw new AccessDeniedException("Access denied: You are not associated with this distributor");
            }
        } else if (isDistributor(auth)) {
            Distributor currentDistributor = retailerService.getDistributorForUsername(auth.getName());
            if (!currentDistributor.getId().equals(distributorId)) {
                throw new AccessDeniedException("Access denied: You can only view your own products");
            }
        }
        return productService.getProductsByDistributor(distributorId);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR')")
    public List<Product> getLowStockProducts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return productService.getLowStockProducts(auth.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR', 'RETAILER')")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR')")
    public Product updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return productService.updateProduct(id, product, auth.getName());
    }

    @PostMapping("/{id}/restock")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR')")
    public Product restockProduct(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return productService.restockProduct(id, quantity, auth.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        productService.deleteProduct(id, auth.getName());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR', 'RETAILER')")
    public List<Product> searchProducts(@RequestParam String name) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isDistributor(auth)) {
            return productService.searchProducts(name, auth.getName());
        }
        return productService.searchProducts(name, auth.getName());
    }

    @GetMapping("/category")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR', 'RETAILER')")
    public List<Product> filterByCategory(@RequestParam String category) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isDistributor(auth)) {
            return productService.filterByCategory(category, auth.getName());
        }
        return productService.filterByCategory(category, auth.getName());
    }
}
