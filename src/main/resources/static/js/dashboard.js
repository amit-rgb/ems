const expenseForm = document.getElementById("expenseForm");
const projectIdInput = document.getElementById("projectId");
const projectMeta = document.getElementById("projectMeta");
const dashboardStatus = document.getElementById("dashboardStatus");
const expenseStatus = document.getElementById("expenseStatus");
const totalSpent = document.getElementById("totalSpent");
const remainingBudget = document.getElementById("remainingBudget");
const expenseDateInput = document.getElementById("expenseDate");
const todayButton = document.getElementById("todayButton");
const uploadedBillUrl = document.getElementById("uploadedBillUrl");
const billFileInput = document.getElementById("billFile");
const vendorNameSelect = document.getElementById("vendorName");

function formatMoney(value) {
    return value === null || value === undefined ? "-" : Number(value).toLocaleString("en-IN", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function getProjectId() {
    const value = projectIdInput.value;
    if (!value) {
        throw new Error("Project ID is required.");
    }
    return Number(value);
}

function setStatus(element, message, isError) {
    element.textContent = message;
    element.style.color = isError ? "#b91c1c" : "#065f46";
}

async function loadDashboard(projectId) {
    const response = await fetch(`/api/v1/projects/${projectId}/dashboard`);
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || "Failed to fetch dashboard.");
    }
    return response.json();
}

async function loadKnownVendors() {
    const response = await fetch("/api/v1/vendors");
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || "Failed to load vendors.");
    }
    const vendors = await response.json();
    vendorNameSelect.innerHTML = "<option value=''>-- Select Registered Vendor --</option>";
    vendors.forEach((vendor) => {
        const option = document.createElement("option");
        option.value = vendor.vendorName;
        option.textContent = vendor.vendorName;
        vendorNameSelect.appendChild(option);
    });
}

function renderDashboard(data) {
    totalSpent.textContent = formatMoney(data.totalSpent);
    remainingBudget.textContent = formatMoney(data.remainingBudget);
}

function currentDateISO() {
    return new Date().toISOString().split("T")[0];
}

async function uploadBillIfSelected() {
    if (!billFileInput.files || billFileInput.files.length === 0) {
        uploadedBillUrl.textContent = "";
        return null;
    }
    const formData = new FormData();
    formData.append("file", billFileInput.files[0]);

    const response = await fetch("/api/v1/bills/upload", {
        method: "POST",
        body: formData
    });
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || "Failed to upload bill.");
    }
    const data = await response.json();
    uploadedBillUrl.textContent = `Uploaded: ${data.billReceiptUrl}`;
    return data.billReceiptUrl;
}

async function refreshSpentSummary(projectId) {
    const data = await loadDashboard(projectId);
    renderDashboard(data);
    setStatus(dashboardStatus, "Updated from latest expenses.", false);
}

expenseForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        const billReceiptUrl = await uploadBillIfSelected();
        const payload = {
            projectId: getProjectId(),
            amount: document.getElementById("amount").value,
            category: document.getElementById("category").value,
            stage: document.getElementById("stage").value,
            paymentMode: document.getElementById("paymentMode").value,
            vendorName: vendorNameSelect.value || null,
            description: document.getElementById("description").value || null,
            billReceiptUrl: billReceiptUrl,
            status: document.getElementById("status").value,
            expenseDate: document.getElementById("expenseDate").value
        };

        const response = await fetch("/api/v1/expenses", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || "Failed to log expense.");
        }
        const data = await response.json();
        setStatus(expenseStatus, `Expense logged. ID: ${data.expenseId}`, false);
        await refreshSpentSummary(payload.projectId);
        const selectedProjectId = String(payload.projectId || "");
        expenseForm.reset();
        projectIdInput.value = selectedProjectId;
        expenseDateInput.value = currentDateISO();
    } catch (err) {
        setStatus(expenseStatus, err.message, true);
    }
});

todayButton.addEventListener("click", () => {
    expenseDateInput.value = currentDateISO();
});

if (!projectIdInput.value && projectMeta) {
    projectIdInput.value = projectMeta.dataset.projectId || "";
}
expenseDateInput.value = currentDateISO();
loadKnownVendors().catch((err) => setStatus(expenseStatus, err.message, true));
refreshSpentSummary(getProjectId()).catch((err) => setStatus(dashboardStatus, err.message, true));
