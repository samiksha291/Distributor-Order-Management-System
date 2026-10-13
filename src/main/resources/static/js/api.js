const API_BASE_URL = "/api";

async function apiRequest(url, options = {}) {

    const token = localStorage.getItem("token");

    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    if (token) {
        headers["Authorization"] = "Bearer " + token;
    }

    const response = await fetch(API_BASE_URL + url, {
        ...options,
        headers
    });

    if (response.status === 401) {
        if (!url.startsWith("/auth/")) {
            localStorage.removeItem("token");
            localStorage.removeItem("username");
            localStorage.removeItem("role");
            window.location.href = "/login.html";
            throw new Error("Session expired. Please log in again.");
        }
    }

    const contentType = response.headers.get("content-type");
    let data = null;

    if (contentType && contentType.includes("application/json")) {
        data = await response.json();
    }

    if (!response.ok) {
        const errorMsg = data?.message || "Something went wrong (HTTP " + response.status + ")";
        throw new Error(errorMsg);
    }

    return data;
}