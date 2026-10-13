async function loadReports() {
    requireLogin();

    const errorBox = document.getElementById("reportError");
    if (errorBox) {
        errorBox.style.display = "none";
        errorBox.textContent = "";
    }

    try {
        const data = await apiRequest("/reports");

        // 1. Sales
        document.getElementById("repTotalSales").textContent = formatCurrency(data.totalSales);
        document.getElementById("repTotalOrders").textContent = data.totalOrders;
        document.getElementById("repAvgOrderValue").textContent = formatCurrency(data.averageOrderValue);

        // 2. Orders Status
        document.getElementById("repPendingOrders").textContent = data.pendingOrders;
        document.getElementById("repConfirmedOrders").textContent = data.confirmedOrders;
        document.getElementById("repPackedOrders").textContent = data.packedOrders;
        document.getElementById("repShippedOrders").textContent = data.shippedOrders;
        document.getElementById("repDeliveredOrders").textContent = data.deliveredOrders;

        // 3. Payments
        document.getElementById("repTotalPaid").textContent = formatCurrency(data.totalPaid);
        document.getElementById("repTotalOutstanding").textContent = formatCurrency(data.totalOutstanding);
        document.getElementById("repPendingPayments").textContent = data.pendingPayments;
        document.getElementById("repPartialPayments").textContent = data.partialPayments;
        document.getElementById("repPaidPayments").textContent = data.paidPayments;

        // 4. Inventory
        document.getElementById("repTotalProducts").textContent = data.totalProducts;
        document.getElementById("repLowStockProducts").textContent = data.lowStockProducts;
        document.getElementById("repTotalStock").textContent = data.totalStock;

        // 5. Retailers
        document.getElementById("repTotalRetailers").textContent = data.totalRetailers;

    } catch (error) {
        console.error("Reports error:", error);
        if (errorBox) {
            errorBox.textContent = "Failed to load report data: " + error.message;
            errorBox.style.display = "block";
        } else {
            alert("Failed to load reports: " + error.message);
        }
    }
}

function formatCurrency(value) {
    if (value === null || value === undefined) {
        return "₹0.00";
    }
    return "₹" + Number(value).toLocaleString("en-IN", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}
