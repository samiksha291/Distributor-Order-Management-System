let products = [];
let retailers = [];

// ==========================================
// LOAD PRODUCTS + RETAILERS
// ==========================================

let distributors = [];

async function loadOrderFormData() {
    requireLogin();

    if (!isRetailer()) {
        document.getElementById("createOrderCard").style.display = "none";
        return;
    }

    const distributorSelect = document.getElementById("distributorSelect");
    const retailerSelect = document.getElementById("retailerSelect");
    const productSelect = document.getElementById("productSelect");
    const errorBox = document.getElementById("orderError");

    distributorSelect.innerHTML = `<option value="">Loading distributors...</option>`;
    productSelect.innerHTML = `<option value="">Select Distributor First</option>`;
    retailerSelect.innerHTML = `<option value="">Loading retailers...</option>`;
    errorBox.style.display = "none";
    errorBox.textContent = "";

    let errorMessages = [];

    // Load Distributors
    try {
        distributors = await apiRequest("/retailers/my-distributors") || [];
        populateDistributorSelect();
    } catch (distErr) {
        console.error("Distributors load error:", distErr);
        distributorSelect.innerHTML = `<option value="">Error loading distributors: ${escapeHtml(distErr.message)}</option>`;
        errorMessages.push("Distributors: " + distErr.message);
    }

    // Load Retailers (for auto-selecting their own retailer ID)
    try {
        retailers = await apiRequest("/retailers") || [];
        populateRetailerSelect();
    } catch (retErr) {
        console.error("Retailers load error:", retErr);
        errorMessages.push("Retailers: " + retErr.message);
    }

    if (errorMessages.length > 0) {
        errorBox.textContent = "Unable to load order form data: " + errorMessages.join(" | ");
        errorBox.style.display = "block";
    }
}

function populateDistributorSelect() {
    const select = document.getElementById("distributorSelect");
    select.innerHTML = `<option value="">Select Distributor</option>`;

    if (!distributors || distributors.length === 0) {
        select.innerHTML = `<option value="">No associated distributors</option>`;
        return;
    }

    distributors.forEach(d => {
        const option = document.createElement("option");
        option.value = d.id;
        option.textContent = d.businessName + " — " + d.contactPersonName;
        select.appendChild(option);
    });
}

async function loadProductsForDistributor() {
    const distributorId = document.getElementById("distributorSelect").value;
    const productSelect = document.getElementById("productSelect");
    
    if (!distributorId) {
        products = [];
        productSelect.innerHTML = `<option value="">Select Distributor First</option>`;
        return;
    }

    productSelect.innerHTML = `<option value="">Loading products...</option>`;

    try {
        products = await apiRequest(`/products/distributor/${distributorId}`) || [];
        populateProductSelect();
    } catch (prodErr) {
        console.error("Products load error:", prodErr);
        productSelect.innerHTML = `<option value="">Error loading products</option>`;
    }
}

// ==========================================
// PRODUCT DROPDOWN
// ==========================================

function populateProductSelect() {
    const select = document.getElementById("productSelect");

    select.innerHTML = `<option value="">Select Product</option>`;

    if (!products || products.length === 0) {
        select.innerHTML = `<option value="">No products available</option>`;
        return;
    }

    products.forEach(product => {
        const option = document.createElement("option");
        option.value = product.id;
        option.textContent =
            product.name +
            " — ₹" +
            Number(product.price).toFixed(2) +
            " | Stock: " +
            product.stockQuantity;

        select.appendChild(option);
    });
}

// ==========================================
// RETAILER DROPDOWN
// ==========================================

function populateRetailerSelect() {
    const select = document.getElementById("retailerSelect");

    select.innerHTML = `<option value="">Select Retailer</option>`;

    if (!retailers || retailers.length === 0) {
        select.innerHTML = `<option value="">No retailers available</option>`;
        return;
    }

    retailers.forEach(retailer => {
        const option = document.createElement("option");
        option.value = retailer.id;
        option.textContent =
            retailer.businessName +
            " — " +
            retailer.ownerName;

        select.appendChild(option);
    });

    // If logged in as RETAILER, auto-select their retailer and lock dropdown
    if (isRetailer() && retailers.length > 0) {
        select.value = retailers[0].id;
        select.disabled = true;
    } else {
        select.disabled = false;
    }
}

// ==========================================
// CREATE ORDER
// ==========================================

async function createOrder() {
    requireLogin();

    const retailerSelect = document.getElementById("retailerSelect");
    const retailerId = retailerSelect.value;
    const distributorId = document.getElementById("distributorSelect").value;
    const productId = document.getElementById("productSelect").value;
    const quantity = Number(document.getElementById("quantity").value);

    if (!distributorId) {
        alert("Please select a distributor.");
        return;
    }

    if (!retailerId) {
        alert("Please select a retailer.");
        return;
    }

    if (!productId) {
        alert("Please select a product.");
        return;
    }

    if (!quantity || quantity <= 0) {
        alert("Quantity must be greater than zero.");
        return;
    }

    const selectedProduct = products.find(p => String(p.id) === String(productId));
    if (selectedProduct && selectedProduct.stockQuantity < quantity) {
        alert(`Insufficient stock for "${selectedProduct.name}". Available stock: ${selectedProduct.stockQuantity}, requested: ${quantity}`);
        return;
    }

    const request = {
        retailerId: Number(retailerId),
        distributorId: Number(distributorId),
        items: [
            {
                productId: Number(productId),
                quantity: quantity
            }
        ]
    };

    try {
        await apiRequest("/orders", {
            method: "POST",
            body: JSON.stringify(request)
        });

        alert("Order created successfully!");

        document.getElementById("quantity").value = 1;
        document.getElementById("productSelect").value = "";

        await loadOrderFormData();
        await loadOrders();

    } catch (error) {
        alert("Unable to create order: " + error.message);
    }
}

// ==========================================
// LOAD ORDERS
// ==========================================

async function loadOrders() {
    requireLogin();

    const status = document.getElementById("statusFilter").value;
    const table = document.getElementById("orderTable");

    table.innerHTML = `
        <tr>
            <td colspan="6" class="empty-state">Loading orders...</td>
        </tr>
    `;

    try {
        let url = "/orders";
        if (status) {
            url = "/orders/status/" + encodeURIComponent(status);
        }

        const orders = await apiRequest(url);
        displayOrders(orders);

    } catch (error) {
        console.error(error);
        table.innerHTML = `
            <tr>
                <td colspan="6" class="empty-state">
                    Unable to load orders: ${escapeHtml(error.message)}
                </td>
            </tr>
        `;
    }
}

// ==========================================
// DISPLAY ORDERS
// ==========================================

function displayOrders(orders) {
    const table = document.getElementById("orderTable");
    const partyHeader = document.getElementById("partyHeader");
    if (partyHeader) {
        partyHeader.textContent = isRetailer() ? "Distributor" : "Retailer";
    }

    if (!orders || orders.length === 0) {
        table.innerHTML = `
            <tr>
                <td colspan="6" class="empty-state">No orders found.</td>
            </tr>
        `;
        return;
    }

    table.innerHTML = "";

    orders.forEach(order => {
        const row = document.createElement("tr");
        const partyName = isRetailer()
            ? (order.distributorName || (order.distributorId ? `Distributor #${order.distributorId}` : "Unknown Distributor"))
            : (order.retailerName || "Unknown Retailer");

        row.innerHTML = `
            <td>#${order.id}</td>
            <td><strong>${escapeHtml(partyName)}</strong></td>
            <td>${formatDate(order.orderDate)}</td>
            <td>₹${Number(order.totalAmount).toFixed(2)}</td>
            <td>
                <span class="badge ${getStatusClass(order.status)}">
                    ${order.status}
                </span>
            </td>
            <td>
                ${
                    (!isRetailer() && order.status !== "DELIVERED")
                        ? `<button class="btn btn-small btn-primary" onclick="moveToNextStatus(${order.id}, '${order.status}')">Next Status</button>`
                        : `<span class="badge ${order.status === 'DELIVERED' ? 'badge-success' : getStatusClass(order.status)}">${order.status === 'DELIVERED' ? 'Delivered' : order.status}</span>`
                }
            </td>
        `;

        table.appendChild(row);
    });
}

// ==========================================
// NEXT STATUS
// ==========================================

async function moveToNextStatus(orderId, currentStatus) {
    const nextStatusMap = {
        PENDING: "CONFIRMED",
        CONFIRMED: "PACKED",
        PACKED: "SHIPPED",
        SHIPPED: "DELIVERED"
    };

    const nextStatus = nextStatusMap[currentStatus];

    if (!nextStatus) {
        alert("This order has already reached its final status (DELIVERED).");
        return;
    }

    try {
        await apiRequest(`/orders/${orderId}/status`, {
            method: "PUT",
            body: JSON.stringify({
                status: nextStatus
            })
        });

        await loadOrders();

    } catch (error) {
        alert("Unable to update order status: " + error.message);
    }
}

// ==========================================
// HELPERS
// ==========================================

function formatDate(date) {
    if (!date) return "-";
    return new Date(date).toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}

function getStatusClass(status) {
    switch (status) {
        case "PENDING":
            return "badge-warning";
        case "CONFIRMED":
            return "badge-info";
        case "PACKED":
            return "badge-primary";
        case "SHIPPED":
            return "badge-info";
        case "DELIVERED":
            return "badge-success";
        default:
            return "";
    }
}

function escapeHtml(value) {
    const div = document.createElement("div");
    div.textContent = value ?? "";
    return div.innerHTML;
}