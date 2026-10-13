async function loadDashboard() {
    requireLogin();

    const username = getUsername();
    const role = getRole();

    const usernameEl = document.getElementById("usernameDisplay");
    const roleEl = document.getElementById("roleDisplay");
    if (usernameEl) usernameEl.textContent = username || "User";
    if (roleEl) roleEl.textContent = role || "UNKNOWN";

    if (role === "DISTRIBUTOR") {
        await loadManagementDashboard();
        return;
    }

    if (role === "RETAILER") {
        await loadRetailerDashboard();
        return;
    }

    alert("Invalid user role.");
    logout();
}

async function loadManagementDashboard() {
    document.getElementById("managementDashboard").style.display = "block";
    document.getElementById("managementNavigation").style.display = "block";
    document.getElementById("retailerDashboard").style.display = "none";

    document.getElementById("dashboardTitle").textContent = "Management Dashboard";
    document.getElementById("dashboardSubtitle").textContent =
        "Overview of products, retailers, orders, payments and inventory status";

    try {
        const data = await apiRequest("/dashboard");

        document.getElementById("totalProducts").textContent = data.totalProducts;
        document.getElementById("totalRetailers").textContent = data.totalRetailers;
        document.getElementById("totalOrders").textContent = data.totalOrders;
        document.getElementById("pendingOrders").textContent = data.pendingOrders;
        document.getElementById("deliveredOrders").textContent = data.deliveredOrders;
        document.getElementById("lowStockProducts").textContent = data.lowStockProducts;

        document.getElementById("totalSales").textContent = formatCurrency(data.totalSales);
        document.getElementById("totalPaid").textContent = formatCurrency(data.totalPaidAmount);
        document.getElementById("totalOutstanding").textContent = formatCurrency(data.totalOutstanding);

    } catch (error) {
        console.error("Dashboard error:", error);
        alert("Unable to load dashboard: " + error.message);
    }
}

async function loadRetailerDashboard() {
    document.getElementById("managementDashboard").style.display = "none";
    document.getElementById("managementNavigation").style.display = "none";
    document.getElementById("retailerDashboard").style.display = "block";

    document.getElementById("dashboardTitle").textContent = "Retailer Portal";
    document.getElementById("dashboardSubtitle").textContent =
        "Welcome to your Retailer portal. Manage your orders and payment balances.";

    try {
        const data = await apiRequest("/dashboard");

        document.getElementById("retailerTotalOrders").textContent = data.totalOrders;
        document.getElementById("retailerPendingOrders").textContent = data.pendingOrders;
        document.getElementById("retailerDeliveredOrders").textContent = data.deliveredOrders;

        document.getElementById("retailerTotalSales").textContent = formatCurrency(data.totalSales);
        document.getElementById("retailerTotalPaid").textContent = formatCurrency(data.totalPaidAmount);
        document.getElementById("retailerTotalOutstanding").textContent = formatCurrency(data.totalOutstanding);

    } catch (error) {
        console.error("Retailer dashboard error:", error);
        alert("Unable to load dashboard: " + error.message);
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
