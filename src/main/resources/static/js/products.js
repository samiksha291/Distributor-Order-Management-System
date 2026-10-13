async function loadProducts() {
    requireLogin();

    const table = document.getElementById("productTable");
    table.innerHTML = `
        <tr>
            <td colspan="8" class="empty-state">Loading products...</td>
        </tr>
    `;

    try {
        const products = await apiRequest("/products");
        displayProducts(products);
    } catch (error) {
        table.innerHTML = `
            <tr>
                <td colspan="8" class="empty-state">Unable to load products: ${escapeHtml(error.message)}</td>
            </tr>
        `;
    }
}

function displayProducts(products) {
    const table = document.getElementById("productTable");

    if (!products || products.length === 0) {
        table.innerHTML = `
            <tr>
                <td colspan="8" class="empty-state">No products found.</td>
            </tr>
        `;
        return;
    }

    table.innerHTML = "";

    products.forEach(product => {
        const row = document.createElement("tr");
        const isLowStock = product.stockQuantity <= product.reorderLevel;

        row.innerHTML = `
            <td>#${product.id}</td>
            <td><strong>${escapeHtml(product.name)}</strong></td>
            <td>${escapeHtml(product.category || "-")}</td>
            <td>₹${Number(product.price).toFixed(2)}</td>
            <td>
                <span style="font-weight: 600; color: ${isLowStock ? '#b91c1c' : '#1e293b'}">
                    ${product.stockQuantity}
                </span>
            </td>
            <td>${product.reorderLevel}</td>
            <td>
                ${
                    isLowStock
                        ? `<span class="badge badge-warning">Low Stock</span>`
                        : `<span class="badge badge-success">In Stock</span>`
                }
            </td>
            <td>
                ${
                    !isRetailer()
                        ? `<button class="btn btn-small btn-primary" onclick="restockProduct(${product.id}, '${escapeHtml(product.name)}')">Restock</button>`
                        : `<span style="color: #64748b; font-size: 13px;">Available</span>`
                }
            </td>
        `;

        table.appendChild(row);
    });
}

async function addProduct() {
    requireLogin();

    const name = document.getElementById("productName").value.trim();
    const description = document.getElementById("productDescription").value.trim();
    const category = document.getElementById("productCategory").value.trim();
    const price = Number(document.getElementById("productPrice").value);
    const stockQuantity = Number(document.getElementById("productStock").value);
    const reorderLevel = Number(document.getElementById("productReorder").value);

    if (!name || !category || isNaN(price) || price <= 0 || isNaN(stockQuantity) || stockQuantity < 0 || isNaN(reorderLevel) || reorderLevel < 0) {
        alert("Please fill in all required product fields with valid numbers.");
        return;
    }

    const product = {
        name,
        description,
        category,
        price,
        stockQuantity,
        reorderLevel,
        active: true
    };

    try {
        await apiRequest("/products", {
            method: "POST",
            body: JSON.stringify(product)
        });

        alert("Product added successfully!");

        document.getElementById("productName").value = "";
        document.getElementById("productDescription").value = "";
        document.getElementById("productCategory").value = "";
        document.getElementById("productPrice").value = "";
        document.getElementById("productStock").value = "";
        document.getElementById("productReorder").value = "";

        loadProducts();

    } catch (error) {
        alert("Failed to add product: " + error.message);
    }
}

async function searchProducts() {
    const name = document.getElementById("productSearch").value.trim();

    if (!name) {
        loadProducts();
        return;
    }

    try {
        const products = await apiRequest("/products/search?name=" + encodeURIComponent(name));
        displayProducts(products);
    } catch (error) {
        alert(error.message);
    }
}

async function loadLowStock() {
    try {
        const products = await apiRequest("/products/low-stock");
        displayProducts(products);
    } catch (error) {
        alert("Unable to load low stock: " + error.message);
    }
}

async function restockProduct(id, productName) {
    const quantityStr = prompt(`Enter restock quantity for "${productName}":`, "50");
    if (!quantityStr) return;

    const quantity = Number(quantityStr);
    if (isNaN(quantity) || quantity <= 0) {
        alert("Please enter a valid positive number for restock.");
        return;
    }

    try {
        await apiRequest(`/products/${id}/restock?quantity=${quantity}`, {
            method: "POST"
        });

        alert(`Successfully restocked ${quantity} units.`);
        loadProducts();

    } catch (error) {
        alert("Restock failed: " + error.message);
    }
}

function escapeHtml(value) {
    const div = document.createElement("div");
    div.textContent = value ?? "";
    return div.innerHTML;
}