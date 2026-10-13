package com.distributor.ordermanagement.dto;

import java.math.BigDecimal;

public class ReportResponse {

    // Sales
    private BigDecimal totalSales = BigDecimal.ZERO;
    private long totalOrders;
    private BigDecimal averageOrderValue = BigDecimal.ZERO;

    // Orders
    private long pendingOrders;
    private long confirmedOrders;
    private long packedOrders;
    private long shippedOrders;
    private long deliveredOrders;

    // Payments
    private BigDecimal totalPaid = BigDecimal.ZERO;
    private BigDecimal totalOutstanding = BigDecimal.ZERO;
    private long pendingPayments;
    private long partialPayments;
    private long paidPayments;

    // Inventory
    private long totalProducts;
    private long lowStockProducts;
    private long totalStock;

    // Retailers
    private long totalRetailers;

    public ReportResponse() {
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public long getConfirmedOrders() {
        return confirmedOrders;
    }

    public void setConfirmedOrders(long confirmedOrders) {
        this.confirmedOrders = confirmedOrders;
    }

    public long getPackedOrders() {
        return packedOrders;
    }

    public void setPackedOrders(long packedOrders) {
        this.packedOrders = packedOrders;
    }

    public long getShippedOrders() {
        return shippedOrders;
    }

    public void setShippedOrders(long shippedOrders) {
        this.shippedOrders = shippedOrders;
    }

    public long getDeliveredOrders() {
        return deliveredOrders;
    }

    public void setDeliveredOrders(long deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getTotalOutstanding() {
        return totalOutstanding;
    }

    public void setTotalOutstanding(BigDecimal totalOutstanding) {
        this.totalOutstanding = totalOutstanding;
    }

    public long getPendingPayments() {
        return pendingPayments;
    }

    public void setPendingPayments(long pendingPayments) {
        this.pendingPayments = pendingPayments;
    }

    public long getPartialPayments() {
        return partialPayments;
    }

    public void setPartialPayments(long partialPayments) {
        this.partialPayments = partialPayments;
    }

    public long getPaidPayments() {
        return paidPayments;
    }

    public void setPaidPayments(long paidPayments) {
        this.paidPayments = paidPayments;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(long lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public long getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(long totalStock) {
        this.totalStock = totalStock;
    }

    public long getTotalRetailers() {
        return totalRetailers;
    }

    public void setTotalRetailers(long totalRetailers) {
        this.totalRetailers = totalRetailers;
    }
}
