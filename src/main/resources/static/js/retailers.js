async function loadRetailers() {
    requireLogin();

    const allTable = document.getElementById("retailerTable");
    const myTable = document.getElementById("myRetailerTable");
    
    allTable.innerHTML = `<tr><td colspan="5" class="empty-state">Loading retailers...</td></tr>`;
    myTable.innerHTML = `<tr><td colspan="7" class="empty-state">Loading your partners...</td></tr>`;

    try {
        const allRetailers = await apiRequest("/retailers");
        let myRetailers = [];
        
        if (isDistributor()) {
            myRetailers = await apiRequest("/retailers/my");
            displayMyRetailers(myRetailers);
        } else {
            // Hide My Retail Partners section for non-distributors
            const myCard = document.getElementById("myRetailersCard");
            if (myCard) myCard.style.display = "none";
        }
        
        displayAllRetailers(allRetailers, myRetailers);
    } catch (error) {
        allTable.innerHTML = `<tr><td colspan="5" class="empty-state">Unable to load retailers: ${escapeHtml(error.message)}</td></tr>`;
        if (myTable) {
            myTable.innerHTML = `<tr><td colspan="7" class="empty-state">Unable to load partners: ${escapeHtml(error.message)}</td></tr>`;
        }
    }
}

function displayMyRetailers(myRetailers) {
    const table = document.getElementById("myRetailerTable");

    if (!myRetailers || myRetailers.length === 0) {
        table.innerHTML = `<tr><td colspan="7" class="empty-state">You have not added any retail partners yet.</td></tr>`;
        return;
    }

    table.innerHTML = "";

    myRetailers.forEach(retailer => {
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>#${retailer.id}</td>
            <td><strong>${escapeHtml(retailer.businessName)}</strong></td>
            <td>${escapeHtml(retailer.ownerName)}</td>
            <td>${escapeHtml(retailer.phone)}</td>
            <td>${escapeHtml(retailer.email || "-")}</td>
            <td>${escapeHtml(retailer.address || "-")}</td>
            <td>
                <span class="badge ${retailer.active !== false ? 'badge-success' : 'badge-unpaid'}">
                    ${retailer.active !== false ? 'Active' : 'Inactive'}
                </span>
            </td>
        `;

        table.appendChild(row);
    });
}

function displayAllRetailers(allRetailers, myRetailers) {
    const table = document.getElementById("retailerTable");

    if (!allRetailers || allRetailers.length === 0) {
        table.innerHTML = `<tr><td colspan="5" class="empty-state">No retailers found.</td></tr>`;
        return;
    }

    table.innerHTML = "";

    allRetailers.forEach(retailer => {
        const isAssociated = myRetailers.some(r => r.id === retailer.id);
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>#${retailer.id}</td>
            <td><strong>${escapeHtml(retailer.businessName)}</strong></td>
            <td>${escapeHtml(retailer.ownerName)}</td>
            <td>${escapeHtml(retailer.phone)}</td>
            <td>
                ${isAssociated 
                    ? `<span class="badge badge-success">Already Added</span>`
                    : `<button class="btn btn-small btn-primary" onclick="addRetailer(${retailer.id})">Add Retailer</button>`
                }
            </td>
        `;

        table.appendChild(row);
    });
}

async function addRetailer(id) {
    requireLogin();

    try {
        await apiRequest(`/retailers/${id}/associate`, {
            method: "POST"
        });

        alert("Retail partner added successfully!");
        loadRetailers();

    } catch (error) {
        alert("Failed to add retailer: " + error.message);
    }
}

async function createRetailer() {
    requireLogin();

    const businessName = document.getElementById("businessName").value.trim();
    const ownerName = document.getElementById("ownerName").value.trim();
    const phone = document.getElementById("phone").value.trim();
    const email = document.getElementById("email").value.trim();
    const address = document.getElementById("address").value.trim();

    if (!businessName || !ownerName || !phone) {
        alert("Please enter Business Name, Owner Name, and a 10-digit Phone Number.");
        return;
    }

    if (!/^[0-9]{10}$/.test(phone)) {
        alert("Phone number must contain exactly 10 digits.");
        return;
    }

    const retailer = {
        businessName,
        ownerName,
        phone,
        email: email || null,
        address: address || null,
        active: true
    };

    try {
        // Create the retailer
        const createdRetailer = await apiRequest("/retailers", {
            method: "POST",
            body: JSON.stringify(retailer)
        });

        // Automatically associate if it's a distributor
        if (isDistributor() && createdRetailer.id) {
            await apiRequest(`/retailers/${createdRetailer.id}/associate`, {
                method: "POST"
            });
        }

        alert("Retailer created and added successfully!");

        document.getElementById("businessName").value = "";
        document.getElementById("ownerName").value = "";
        document.getElementById("phone").value = "";
        document.getElementById("email").value = "";
        document.getElementById("address").value = "";

        loadRetailers();

    } catch (error) {
        alert("Failed to add retailer: " + error.message);
    }
}

async function searchRetailers() {
    const name = document.getElementById("retailerSearch").value.trim();

    if (!name) {
        loadRetailers();
        return;
    }

    try {
        const allRetailers = await apiRequest("/retailers/search?name=" + encodeURIComponent(name));
        let myRetailers = [];
        if (isDistributor()) {
            myRetailers = await apiRequest("/retailers/my");
        }
        displayAllRetailers(allRetailers, myRetailers);
    } catch (error) {
        alert("Search failed: " + error.message);
    }
}

function escapeHtml(value) {
    const div = document.createElement("div");
    div.textContent = value ?? "";
    return div.innerHTML;
}