package com.distributor.ordermanagement.service;

import com.distributor.ordermanagement.entity.Product;
import com.distributor.ordermanagement.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final RetailerService retailerService;

    public ProductService(ProductRepository productRepository, RetailerService retailerService) {
        this.productRepository = productRepository;
        this.retailerService = retailerService;
    }

    public Product createProduct(Product product, String username) {
        com.distributor.ordermanagement.entity.Distributor distributor = retailerService.getDistributorForUsername(username);
        product.setDistributor(distributor);
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsForDistributor(String username) {
        com.distributor.ordermanagement.entity.Distributor distributor = retailerService.getDistributorForUsername(username);
        return productRepository.findByDistributorId(distributor.getId());
    }

    public List<Product> getProductsByDistributor(Long distributorId) {
        return productRepository.findByDistributorId(distributorId);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product updateProduct(Long id, Product updatedProduct, String username) {
        Product product = getProductById(id);
        com.distributor.ordermanagement.entity.Distributor distributor = retailerService.getDistributorForUsername(username);

        if (product.getDistributor() == null || !product.getDistributor().getId().equals(distributor.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You do not have permission to modify this product");
        }

        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setCategory(updatedProduct.getCategory());
        product.setPrice(updatedProduct.getPrice());
        product.setStockQuantity(updatedProduct.getStockQuantity());
        product.setReorderLevel(updatedProduct.getReorderLevel());
        product.setActive(updatedProduct.getActive());

        return productRepository.save(product);
    }

    public void deleteProduct(Long id, String username) {
        Product product = getProductById(id);
        com.distributor.ordermanagement.entity.Distributor distributor = retailerService.getDistributorForUsername(username);

        if (product.getDistributor() == null || !product.getDistributor().getId().equals(distributor.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You do not have permission to delete this product");
        }

        productRepository.delete(product);
    }

    public Product restockProduct(Long id, Integer quantity, String username) {

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Restock quantity must be greater than 0");
        }

        Product product = getProductById(id);
        com.distributor.ordermanagement.entity.Distributor distributor = retailerService.getDistributorForUsername(username);

        if (product.getDistributor() == null || !product.getDistributor().getId().equals(distributor.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You do not have permission to restock this product");
        }

        product.setStockQuantity(
                product.getStockQuantity() + quantity
        );

        return productRepository.save(product);
    }

    public List<Product> getLowStockProducts(String username) {
        com.distributor.ordermanagement.entity.Distributor distributor = retailerService.getDistributorForUsername(username);
        return productRepository.findByDistributorId(distributor.getId())
                .stream()
                .filter(product ->
                        product.getStockQuantity() <= product.getReorderLevel()
                )
                .toList();
    }

    public List<Product> searchProducts(String name, String username) {
        com.distributor.ordermanagement.entity.Distributor distributor = retailerService.getDistributorForUsername(username);
        return productRepository.findByDistributorIdAndNameContainingIgnoreCase(distributor.getId(), name);
    }

    public List<Product> filterByCategory(String category, String username) {
        com.distributor.ordermanagement.entity.Distributor distributor = retailerService.getDistributorForUsername(username);
        return productRepository.findByDistributorIdAndCategoryIgnoreCase(distributor.getId(), category);
    }
}