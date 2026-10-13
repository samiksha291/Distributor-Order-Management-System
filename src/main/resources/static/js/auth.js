function login() {

    const username =
        document.getElementById("loginUsername").value.trim();

    const password =
        document.getElementById("loginPassword").value;

    if (!username || !password) {
        alert("Please enter username and password.");
        return;
    }

    apiRequest("/auth/login", {
        method: "POST",
        body: JSON.stringify({
            username: username,
            password: password
        })
    })
        .then(data => {

            localStorage.setItem("token", data.token);
            localStorage.setItem("username", data.username);
            localStorage.setItem("role", data.role);

            window.location.href = "/dashboard.html";
        })
        .catch(error => {
            alert(error.message);
        });
}


function toggleRegistrationFields() {
    const role = document.getElementById("registerRole").value;
    const dynamicFields = document.getElementById("dynamicFields");
    
    let html = "";
    
    if (role === "DISTRIBUTOR") {
        html = `
            <div class="form-group"><label>Business Name</label><input type="text" id="regBusinessName" required></div>
            <div class="form-group"><label>Contact Person Name</label><input type="text" id="regContactName" required></div>
            <div class="form-group"><label>Phone</label><input type="text" id="regPhone" required pattern="[0-9]{10}"></div>
            <div class="form-group"><label>Email</label><input type="email" id="regEmail" required></div>
            <div class="form-group"><label>Address</label><input type="text" id="regAddress"></div>
        `;
    } else if (role === "RETAILER") {
        html = `
            <div class="form-group"><label>Business Name</label><input type="text" id="regBusinessName" required></div>
            <div class="form-group"><label>Owner Name</label><input type="text" id="regOwnerName" required></div>
            <div class="form-group"><label>Phone</label><input type="text" id="regPhone" required pattern="[0-9]{10}"></div>
            <div class="form-group"><label>Email</label><input type="email" id="regEmail" required></div>
            <div class="form-group"><label>Address</label><input type="text" id="regAddress"></div>
        `;
    }
    
    dynamicFields.innerHTML = html;
}

function register() {
    const username = document.getElementById("registerUsername").value.trim();
    const password = document.getElementById("registerPassword").value;
    const role = document.getElementById("registerRole").value;

    if (!username || !password || !role) {
        alert("Please fill all base registration fields.");
        return;
    }

    if (password.length < 6) {
        alert("Password must contain at least 6 characters.");
        return;
    }
    
    const requestBody = {
        username: username,
        password: password,
        role: role
    };
    
    if (role === "DISTRIBUTOR") {
        requestBody.businessName = document.getElementById("regBusinessName")?.value.trim();
        requestBody.contactPersonName = document.getElementById("regContactName")?.value.trim();
        requestBody.phone = document.getElementById("regPhone")?.value.trim();
        requestBody.email = document.getElementById("regEmail")?.value.trim();
        requestBody.address = document.getElementById("regAddress")?.value.trim();
    } else if (role === "RETAILER") {
        requestBody.businessName = document.getElementById("regBusinessName")?.value.trim();
        requestBody.ownerName = document.getElementById("regOwnerName")?.value.trim();
        requestBody.phone = document.getElementById("regPhone")?.value.trim();
        requestBody.email = document.getElementById("regEmail")?.value.trim();
        requestBody.address = document.getElementById("regAddress")?.value.trim();
    }

    apiRequest("/auth/register", {
        method: "POST",
        body: JSON.stringify(requestBody)
    })
        .then(data => {
            alert(
                "Registration successful!\n\n" +
                "Username: " + data.username + "\n" +
                "Role: " + data.role
            );
            showLogin();
            document.getElementById("registerForm").reset();
            toggleRegistrationFields();
        })
        .catch(error => {
            alert(error.message);
        });
}


function showRegister() {

    document.getElementById("loginSection").style.display = "none";
    document.getElementById("registerSection").style.display = "block";
}


function showLogin() {

    document.getElementById("registerSection").style.display = "none";
    document.getElementById("loginSection").style.display = "block";
}


function logout() {

    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("role");

    window.location.href = "/login.html";
}


function requireLogin() {

    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "/login.html";
    }
}


function getUsername() {
    return localStorage.getItem("username");
}


function getRole() {
    return localStorage.getItem("role");
}


function isAdmin() {
    return false;
}


function isDistributor() {
    return getRole() === "DISTRIBUTOR";
}


function isRetailer() {
    return getRole() === "RETAILER";
}


function isDistributorUser() {
    return isDistributor();
}

function renderNavigation(activePage = "") {
    const role = getRole();
    const username = getUsername();

    const navLinksEl = document.getElementById("navLinks");
    if (navLinksEl) {
        let linksHtml = `<a href="/dashboard.html" class="${activePage === 'dashboard' ? 'active-link' : ''}">Dashboard</a>`;
        if (role === 'DISTRIBUTOR') {
            linksHtml += `
                <a href="/products.html" class="${activePage === 'products' ? 'active-link' : ''}">Products</a>
                <a href="/retailers.html" class="${activePage === 'retailers' ? 'active-link' : ''}">Retailers</a>
            `;
        }
        linksHtml += `<a href="/orders.html" class="${activePage === 'orders' ? 'active-link' : ''}">Orders</a>`;
        linksHtml += `<a href="/payments.html" class="${activePage === 'payments' ? 'active-link' : ''}">Payments</a>`;
        if (role === 'DISTRIBUTOR') {
            linksHtml += `<a href="/reports.html" class="${activePage === 'reports' ? 'active-link' : ''}">Reports</a>`;
        }
        navLinksEl.innerHTML = linksHtml;
    }

    const userSpan = document.getElementById("navUsername") || document.getElementById("usernameDisplay");
    const roleSpan = document.getElementById("navRole") || document.getElementById("roleDisplay");

    if (userSpan) userSpan.textContent = username || "User";
    if (roleSpan) roleSpan.textContent = role || "";
}