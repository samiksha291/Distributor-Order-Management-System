async function loadPayments() {
    requireLogin();

    if (isDistributor()) {
        const recordPaymentCard = document.getElementById("recordPaymentCard");
        if (recordPaymentCard) {
            recordPaymentCard.style.display = "none";
        }
    }

    const table = document.getElementById("paymentTable");
    table.innerHTML = `
        <tr>
            <td colspan="7" class="empty-state">Loading payments...</td>
        </tr>
    `;

    try {
        const payments = await apiRequest("/payments");
        displayPayments(payments);
    } catch (error) {
        console.error("Load payments error:", error);
        table.innerHTML = `
            <tr>
                <td colspan="7" class="empty-state">Unable to load payments: ${escapeHtml(error.message)}</td>
            </tr>
        `;
    }
}

function displayPayments(payments) {
    const table = document.getElementById("paymentTable");

    if (!payments || payments.length === 0) {
        table.innerHTML = `
            <tr>
                <td colspan="7" class="empty-state">No payment records found.</td>
            </tr>
        `;
        return;
    }

    table.innerHTML = "";

    payments.forEach(payment => {
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>#${payment.id}</td>
            <td><strong>#${payment.orderId}</strong></td>
            <td>₹${Number(payment.totalAmount).toFixed(2)}</td>
            <td>₹${Number(payment.paidAmount).toFixed(2)}</td>
            <td style="color: ${Number(payment.outstandingAmount) > 0 ? '#b91c1c' : '#15803d'}; font-weight: 600;">
                ₹${Number(payment.outstandingAmount).toFixed(2)}
            </td>
            <td>
                <span class="badge ${getPaymentStatusClass(payment.status)}">
                    ${payment.status}
                </span>
            </td>
            <td>${formatDate(payment.paymentDate)}</td>
        `;

        table.appendChild(row);
    });
}

async function makePayment() {
    requireLogin();

    const orderIdInput = document.getElementById("paymentOrderId");
    const amountInput = document.getElementById("paymentAmount");
    const errorBox = document.getElementById("paymentError");

    if (errorBox) {
        errorBox.style.display = "none";
        errorBox.textContent = "";
    }

    const orderId = Number(orderIdInput.value);
    const amount = Number(amountInput.value);

    if (!orderId || orderId <= 0) {
        alert("Please enter a valid Order ID.");
        return;
    }

    if (!amount || amount <= 0) {
        alert("Payment amount must be greater than zero.");
        return;
    }

    try {
        await apiRequest(`/payments/orders/${orderId}`, {
            method: "POST",
            body: JSON.stringify({
                amount: amount
            })
        });

        alert("Payment recorded successfully!");
        amountInput.value = "";
        orderIdInput.value = "";

        await loadPayments();

    } catch (error) {
        console.error("Payment error:", error);
        if (errorBox) {
            errorBox.textContent = "Payment failed: " + error.message;
            errorBox.style.display = "block";
        } else {
            alert("Payment failed: " + error.message);
        }
    }
}

function getPaymentStatusClass(status) {
    switch (status) {
        case "PAID":
            return "badge-success";
        case "PARTIAL":
            return "badge-warning";
        case "PENDING":
            return "badge-unpaid";
        default:
            return "";
    }
}

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

function escapeHtml(value) {
    const div = document.createElement("div");
    div.textContent = value ?? "";
    return div.innerHTML;
}