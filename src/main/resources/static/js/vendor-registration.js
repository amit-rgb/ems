const form = document.getElementById("vendorRegistrationForm");
const vendorStatus = document.getElementById("vendorStatus");
const tableBody = document.getElementById("vendorsTableBody");
const vendorNameInput = document.getElementById("vendorName");
const contactNumberInput = document.getElementById("contactNumber");

function setStatus(message, isError) {
    vendorStatus.textContent = message;
    vendorStatus.style.color = isError ? "#b91c1c" : "#065f46";
}

async function loadVendors() {
    const response = await fetch("/api/v1/vendors");
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || "Failed to load vendors.");
    }
    const vendors = await response.json();
    tableBody.innerHTML = "";
    vendors.forEach((vendor) => {
        const row = document.createElement("tr");
        row.innerHTML = `<td>${vendor.vendorName}</td><td>${vendor.contactNumber}</td>`;
        tableBody.appendChild(row);
    });
    if (!tableBody.children.length) {
        tableBody.innerHTML = "<tr><td colspan='2'>No vendors registered yet.</td></tr>";
    }
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        const payload = {
            vendorName: vendorNameInput.value.trim(),
            contactNumber: contactNumberInput.value.trim()
        };
        const response = await fetch("/api/v1/vendors", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(payload)
        });
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || "Failed to register vendor.");
        }
        form.reset();
        setStatus("Vendor registered successfully.", false);
        await loadVendors();
    } catch (err) {
        setStatus(err.message, true);
    }
});

loadVendors().catch((err) => setStatus(err.message, true));
