const state = { tables: [], reservations: [], menu: [], orders: [], bills: [] };
const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => [...document.querySelectorAll(selector)];
let toastTimer;

function activateTab(tabId, shouldScroll = false) {
    $$(".tab-panel").forEach(panel => panel.classList.toggle("active", panel.id === tabId));
    $$(".tab-btn").forEach(button => {
        const active = button.dataset.tab === tabId;
        button.classList.toggle("active", active);
        button.setAttribute("aria-selected", String(active));
    });
    if (shouldScroll) {
        document.querySelector(".section-nav")?.scrollIntoView({ behavior: "smooth", block: "start" });
    }
}

function seatedReservationForTable(tableId) {
    return state.reservations.find(r => Number(r.tableId) === Number(tableId) && r.status === "SEATED");
}

function fillOrderCustomerName() {
    const tableId = Number($("#order-table").value);
    const reservation = seatedReservationForTable(tableId);
    $("#order-name").value = reservation?.customerName || "";
}

function esc(value) {
    return String(value ?? "").replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"}[c]));
}

function money(value) {
    return new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" }).format(Number(value || 0));
}

function dt(value) {
    if (!value) return "—";
    return new Intl.DateTimeFormat("en-IN", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value));
}

function badge(value) {
    const cls = String(value).toLowerCase();
    return `<span class="badge ${cls}">${esc(value)}</span>`;
}

async function api(path, options = {}) {
    const response = await fetch(`/api${path}`, {
        ...options,
        headers: { "Content-Type": "application/json", ...(options.headers || {}) }
    });
    const body = response.status === 204 ? null : await response.json().catch(() => null);
    if (!response.ok) {
        const fields = body?.fieldErrors ? Object.values(body.fieldErrors).join(", ") : "";
        throw new Error(body?.message ? `${body.message}${fields ? `: ${fields}` : ""}` : `Request failed (${response.status})`);
    }
    return body;
}

function notify(message, error = false) {
    const toast = $("#toast");
    toast.textContent = message;
    toast.classList.toggle("error", error);
    toast.classList.add("show");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.classList.remove("show"), 3200);
}

async function refreshAll(showMessage = false) {
    try {
        const [tables, reservations, menu, orders, bills] = await Promise.all([
            api("/tables"), api("/reservations"), api("/menu-items"), api("/orders"), api("/bills")
        ]);
        Object.assign(state, { tables, reservations, menu, orders, bills });
        renderAll();
        if (showMessage) notify("Data refreshed");
    } catch (e) {
        notify(e.message, true);
    }
}

function renderAll() {
    renderStats();
    renderTables();
    renderReservations();
    renderMenu();
    renderOrders();
    renderBills();
    populateSelects();
}

function renderStats() {
    $("#stat-total").textContent = state.tables.length;
    $("#stat-free").textContent = state.tables.filter(t => t.status === "FREE").length;
    $("#stat-occupied").textContent = state.tables.filter(t => t.status === "OCCUPIED").length;
    $("#stat-reserved").textContent = state.reservations.filter(r => r.status === "RESERVED").length;
    $("#stat-bills").textContent = state.bills.length;
}

function renderTables() {
    const query = $("#table-search").value.trim().toLowerCase();
    const rows = state.tables.filter(t => [t.id, t.tableNumber, t.capacity, t.status].join(" ").toLowerCase().includes(query));
    $("#tables-body").innerHTML = rows.length ? rows.map(t => `
        <tr>
            <td>${t.id}</td><td><strong>Table ${t.tableNumber}</strong></td><td>${t.capacity}</td><td>${badge(t.status)}</td>
            <td><div class="actions">
                <button class="btn small ghost" onclick="editTable(${t.id})" ${t.status !== "FREE" ? "disabled" : ""}>Edit</button>
                <button class="btn small danger" onclick="deleteTable(${t.id})" ${t.status !== "FREE" ? "disabled" : ""}>Delete</button>
                ${t.status === "FREE" ? `<button class="btn small primary" onclick="openReservationForm(${t.id}, 'WALK_IN')">Walk-in</button>` : ""}
                ${t.status === "OCCUPIED" ? `<button class="btn small primary" onclick="openOrderForm(${t.id})">Take Order</button><button class="btn small dark" onclick="generateBill(${t.id})">Bill</button>` : ""}
                ${t.status === "BILLED" ? `<button class="btn small primary" onclick="vacateTable(${t.id})">Vacate</button>` : ""}
            </div></td>
        </tr>`).join("") : `<tr><td colspan="5" class="empty">No tables found.</td></tr>`;
}

function renderReservations() {
    $("#reservations-body").innerHTML = state.reservations.length ? state.reservations.map(r => `
        <tr>
            <td>${r.id}</td>
            <td><strong>${esc(r.customerName)}</strong><br><span class="muted">${esc(r.phone || "No phone")}</span></td>
            <td>${badge(r.type)}</td><td>Table ${r.tableNumber}</td><td>${r.partySize}</td>
            <td>${dt(r.startTime)}<br><span class="muted">to ${dt(r.endTime)}</span></td>
            <td>${badge(r.status)}</td>
            <td><div class="actions">
                ${r.status === "RESERVED" ? `<button class="btn small primary" onclick="seatReservation(${r.id})">Seat</button><button class="btn small ghost" onclick="editReservation(${r.id})">Edit</button><button class="btn small warn" onclick="cancelReservation(${r.id})">Cancel</button>` : ""}
                ${r.status === "SEATED" ? `<button class="btn small primary" onclick="openOrderForm(${r.tableId})">Take Order</button>` : ""}
                ${["RESERVED", "CANCELLED"].includes(r.status) ? `<button class="btn small danger" onclick="deleteReservation(${r.id})">Delete</button>` : ""}
            </div></td>
        </tr>`).join("") : `<tr><td colspan="8" class="empty">No reservations yet.</td></tr>`;
}

function renderMenu() {
    $("#menu-body").innerHTML = state.menu.length ? state.menu.map(m => `
        <tr><td>${m.id}</td><td><strong>${esc(m.itemName)}</strong></td><td>${esc(m.description || "—")}</td><td>${money(m.unitPrice)}</td><td>${badge(m.active ? "ACTIVE" : "INACTIVE")}</td>
        <td><div class="actions"><button class="btn small ghost" onclick="editMenu(${m.id})">Edit</button><button class="btn small danger" onclick="deleteMenu(${m.id})">Delete</button></div></td></tr>`).join("") : `<tr><td colspan="6" class="empty">No menu items.</td></tr>`;
}

function renderOrders() {
    $("#orders-body").innerHTML = state.orders.length ? state.orders.map(o => `
        <tr><td>${o.id}</td><td><strong>${esc(o.customerName)}</strong></td><td>Table ${o.tableNumber}</td><td>#${o.reservationId}</td>
        <td>${o.items.map(i => `${esc(i.itemName)} × ${i.quantity}`).join("<br>")}</td><td><strong>${money(o.total)}</strong></td><td>${o.billId ? `#${o.billId}` : "Unbilled"}</td>
        <td><div class="actions"><button class="btn small ghost" onclick="showOrder(${o.id})">View</button>${!o.billId ? `<button class="btn small danger" onclick="deleteOrder(${o.id})">Delete</button>` : ""}</div></td></tr>`).join("") : `<tr><td colspan="8" class="empty">No orders yet.</td></tr>`;
}

function renderBills() {
    $("#bills-body").innerHTML = state.bills.length ? state.bills.map(b => `
        <tr><td>#${b.id}</td><td>Table ${b.tableNumber}</td><td>${esc(b.customerName)}</td><td>${dt(b.generatedAt)}</td><td><strong>${money(b.total)}</strong></td>
        <td><button class="btn small ghost" onclick="showBill(${b.id})">View Bill</button></td></tr>`).join("") : `<tr><td colspan="6" class="empty">No bills generated.</td></tr>`;
}

function populateSelects() {
    const freeTableOptions = state.tables.filter(t => t.status === "FREE").map(t => `<option value="${t.id}">Table ${t.tableNumber} — ${t.capacity} seats — FREE</option>`).join("");
    $("#reservation-table").innerHTML = freeTableOptions ? `<option value="">Select free table</option>${freeTableOptions}` : `<option value="">No free tables</option>`;

    const occupied = state.tables.filter(t => t.status === "OCCUPIED");
    const occupiedOptions = occupied.map(t => `<option value="${t.id}">Table ${t.tableNumber}</option>`).join("");
    $("#order-table").innerHTML = occupiedOptions ? `<option value="">Select occupied table</option>${occupiedOptions}` : `<option value="">No occupied tables</option>`;
    $("#bill-table").innerHTML = occupiedOptions ? `<option value="">Select occupied table</option>${occupiedOptions}` : `<option value="">No occupied tables</option>`;
    renderOrderMenuGrid();
}

function renderOrderMenuGrid() {
    const activeMenu = state.menu.filter(m => m.active);
    $("#order-menu-grid").innerHTML = activeMenu.length ? activeMenu.map(m => `
        <label class="menu-choice">
            <span><strong>${esc(m.itemName)}</strong><span>${money(m.unitPrice)}</span></span>
            <input class="order-qty" data-menu-id="${m.id}" type="number" min="0" value="0" title="Quantity">
        </label>`).join("") : `<div class="empty">Add active menu items first.</div>`;
}

// TABLES
function openTableForm() {
    activateTab("tables", true);
    $("#table-edit-id").value = "";
    $("#table-number").value = "";
    $("#table-capacity").value = 4;
    $("#table-form").classList.remove("hidden");
    $("#table-number").focus();
}
window.editTable = function(id) {
    const t = state.tables.find(x => x.id === id);
    if (!t) return;
    $("#table-edit-id").value = t.id;
    $("#table-number").value = t.tableNumber;
    $("#table-capacity").value = t.capacity;
    $("#table-form").classList.remove("hidden");
    $("#table-number").focus();
};
window.deleteTable = async function(id) {
    if (!confirm("Delete this unused table?")) return;
    try { await api(`/tables/${id}`, { method: "DELETE" }); notify("Table deleted"); await refreshAll(); } catch (e) { notify(e.message, true); }
};

// RESERVATIONS
window.openReservationForm = function(tableId = null, type = "WALK_IN") {
    activateTab("reservations", true);
    $("#reservation-edit-id").value = "";
    $("#reservation-form").reset();
    $("#reservation-party").value = 2;
    $("#reservation-duration").value = 120;
    $("#reservation-type").value = type;
    $("#reservation-table").value = tableId || "";
    const now = new Date(Date.now() + 60 * 60 * 1000);
    now.setMinutes(Math.ceil(now.getMinutes()/15)*15, 0, 0);
    $("#reservation-start").value = new Date(now.getTime() - now.getTimezoneOffset()*60000).toISOString().slice(0,16);
    $("#reservation-form").classList.remove("hidden");
    toggleScheduleFields();
    $("#reservation-name").focus();
};
window.editReservation = function(id) {
    const r = state.reservations.find(x => x.id === id);
    if (!r) return;
    $("#reservation-edit-id").value = r.id;
    $("#reservation-name").value = r.customerName;
    $("#reservation-phone").value = r.phone || "";
    $("#reservation-type").value = "RESERVE";
    $("#reservation-type").disabled = true;
    $("#reservation-table").value = r.tableId;
    $("#reservation-party").value = r.partySize;
    $("#reservation-start").value = r.startTime.slice(0,16);
    $("#reservation-duration").value = Math.round((new Date(r.endTime) - new Date(r.startTime))/60000);
    $("#reservation-form").classList.remove("hidden");
    toggleScheduleFields();
};
window.seatReservation = async function(id) { try { await api(`/reservations/${id}/seat`, { method: "POST" }); notify("Guest seated. Table is now occupied."); await refreshAll(); } catch (e) { notify(e.message, true); } };
window.cancelReservation = async function(id) { try { await api(`/reservations/${id}/cancel`, { method: "POST" }); notify("Reservation cancelled"); await refreshAll(); } catch (e) { notify(e.message, true); } };
window.deleteReservation = async function(id) { if (!confirm("Delete this reservation record?")) return; try { await api(`/reservations/${id}`, { method: "DELETE" }); notify("Reservation deleted"); await refreshAll(); } catch (e) { notify(e.message, true); } };

function toggleScheduleFields() {
    const reserve = $("#reservation-type").value === "RESERVE";
    $$(".schedule-field").forEach(el => el.classList.toggle("hidden", !reserve));
    $("#reservation-start").required = reserve;
}

// MENU
function openMenuForm() {
    activateTab("menu", true);
    $("#menu-edit-id").value = "";
    $("#menu-form").reset();
    $("#menu-active").value = "true";
    $("#menu-form").classList.remove("hidden");
    $("#menu-name").focus();
}
window.editMenu = function(id) {
    const m = state.menu.find(x => x.id === id);
    if (!m) return;
    $("#menu-edit-id").value = m.id;
    $("#menu-name").value = m.itemName;
    $("#menu-description").value = m.description || "";
    $("#menu-price").value = m.unitPrice;
    $("#menu-active").value = String(m.active);
    $("#menu-form").classList.remove("hidden");
};
window.deleteMenu = async function(id) { if (!confirm("Delete this menu item? Existing old orders keep their copied item details.")) return; try { await api(`/menu-items/${id}`, { method: "DELETE" }); notify("Menu item deleted"); await refreshAll(); } catch (e) { notify(e.message, true); } };

// ORDERS
window.openOrderForm = function(tableId = null) {
    activateTab("orders", true);
    $("#order-form").classList.remove("hidden");
    renderOrderMenuGrid();
    $("#order-table").value = tableId || "";
    fillOrderCustomerName();
    if (!$("#order-name").value) $("#order-name").focus();
};
window.deleteOrder = async function(id) { if (!confirm("Delete this unbilled order?")) return; try { await api(`/orders/${id}`, { method: "DELETE" }); notify("Order deleted"); await refreshAll(); } catch (e) { notify(e.message, true); } };
window.showOrder = function(id) {
    const o = state.orders.find(x => x.id === id);
    if (!o) return;
    $("#dialog-content").innerHTML = `<div class="invoice-head"><h4>Order #${o.id}</h4><div class="muted">${esc(o.customerName)} · Table ${o.tableNumber} · Reservation #${o.reservationId} · ${dt(o.createdAt)}</div></div>${o.items.map(i => `<div class="invoice-line"><span>${esc(i.itemName)} × ${i.quantity}</span><strong>${money(i.lineTotal)}</strong></div>`).join("")}<div class="invoice-total"><span>Order total</span><span>${money(o.total)}</span></div>`;
    $("#detail-dialog").showModal();
};

// BILLS
window.generateBill = async function(tableId) {
    try { const bill = await api("/bills", { method: "POST", body: JSON.stringify({ tableId }) }); notify(`Bill #${bill.id} generated`); await refreshAll(); showBill(bill.id); } catch (e) { notify(e.message, true); }
};
window.vacateTable = async function(tableId) { try { await api(`/bills/tables/${tableId}/vacate`, { method: "POST" }); notify("Table vacated and marked FREE"); await refreshAll(); } catch (e) { notify(e.message, true); } };
window.showBill = async function(id) {
    try {
        const b = await api(`/bills/${id}`);
        $("#dialog-content").innerHTML = `<div class="invoice-head"><span class="eyebrow">TABLETURN BILL</span><h4>Bill #${b.id}</h4><div class="muted">Table ${b.tableNumber} · ${esc(b.customerName)} · ${dt(b.generatedAt)}</div></div>
        ${b.orders.map(o => `<div class="invoice-order"><h5>Order #${o.id}</h5>${o.items.map(i => `<div class="invoice-line"><span>${esc(i.itemName)} × ${i.quantity} @ ${money(i.unitPrice)}</span><span>${money(i.lineTotal)}</span></div>`).join("")}<div class="invoice-line"><strong>Order total</strong><strong>${money(o.total)}</strong></div></div>`).join("")}
        <div class="invoice-total"><span>Grand Total</span><span>${money(b.total)}</span></div>`;
        $("#detail-dialog").showModal();
    } catch (e) { notify(e.message, true); }
};

// EVENTS
$$(".tab-btn").forEach(button => button.addEventListener("click", () => activateTab(button.dataset.tab)));

$("#add-table-btn").addEventListener("click", openTableForm);
$("#cancel-table-form").addEventListener("click", () => $("#table-form").classList.add("hidden"));
$("#table-form").addEventListener("submit", async e => {
    e.preventDefault();
    const id = $("#table-edit-id").value;
    const body = { tableNumber: Number($("#table-number").value), capacity: Number($("#table-capacity").value) };
    try { await api(id ? `/tables/${id}` : "/tables", { method: id ? "PUT" : "POST", body: JSON.stringify(body) }); notify(id ? "Table updated" : "Table created"); $("#table-form").classList.add("hidden"); await refreshAll(); } catch (e2) { notify(e2.message, true); }
});
$("#table-search").addEventListener("input", renderTables);
$("#refresh-btn").addEventListener("click", () => refreshAll(true));

$("#add-reservation-btn").addEventListener("click", () => openReservationForm());
$("#reservation-type").addEventListener("change", toggleScheduleFields);
$("#cancel-reservation-form").addEventListener("click", () => { $("#reservation-type").disabled = false; $("#reservation-form").classList.add("hidden"); });
$("#reservation-form").addEventListener("submit", async e => {
    e.preventDefault();
    const id = $("#reservation-edit-id").value;
    const type = $("#reservation-type").value;
    const body = {
        tableId: Number($("#reservation-table").value), customerName: $("#reservation-name").value.trim(), phone: $("#reservation-phone").value.trim() || null,
        type, partySize: Number($("#reservation-party").value), startTime: type === "RESERVE" ? $("#reservation-start").value : null,
        durationMinutes: Number($("#reservation-duration").value || 120)
    };
    try { await api(id ? `/reservations/${id}` : "/reservations", { method: id ? "PUT" : "POST", body: JSON.stringify(body) }); notify(type === "WALK_IN" ? "Walk-in seated; table is occupied" : (id ? "Reservation updated" : "Reservation added")); $("#reservation-type").disabled = false; $("#reservation-form").classList.add("hidden"); await refreshAll(); } catch (e2) { notify(e2.message, true); }
});

$("#add-menu-btn").addEventListener("click", openMenuForm);
$("#cancel-menu-form").addEventListener("click", () => $("#menu-form").classList.add("hidden"));
$("#menu-form").addEventListener("submit", async e => {
    e.preventDefault();
    const id = $("#menu-edit-id").value;
    const body = { itemName: $("#menu-name").value.trim(), description: $("#menu-description").value.trim() || null, unitPrice: Number($("#menu-price").value), active: $("#menu-active").value === "true" };
    try { await api(id ? `/menu-items/${id}` : "/menu-items", { method: id ? "PUT" : "POST", body: JSON.stringify(body) }); notify(id ? "Menu item updated" : "Menu item created"); $("#menu-form").classList.add("hidden"); await refreshAll(); } catch (e2) { notify(e2.message, true); }
});

$("#add-order-btn").addEventListener("click", () => openOrderForm());
$("#order-table").addEventListener("change", fillOrderCustomerName);
$("#cancel-order-form").addEventListener("click", () => $("#order-form").classList.add("hidden"));
$("#order-form").addEventListener("submit", async e => {
    e.preventDefault();
    const items = $$(".order-qty").map(input => ({ menuItemId: Number(input.dataset.menuId), quantity: Number(input.value) })).filter(x => x.quantity > 0);
    if (!items.length) { notify("Choose at least one menu item quantity", true); return; }
    const tableId = Number($("#order-table").value);
    if (!tableId) { notify("Select an occupied table", true); return; }
    const customerName = $("#order-name").value.trim();
    if (!customerName) { notify("Enter customer name", true); return; }
    try { await api("/orders", { method: "POST", body: JSON.stringify({ tableId, customerName, items }) }); notify("Order created and tracked"); $("#order-form").classList.add("hidden"); await refreshAll(); } catch (e2) { notify(e2.message, true); }
});

$("#bill-id-form").addEventListener("submit", e => { e.preventDefault(); showBill(Number($("#bill-id").value)); });
$("#generate-bill-form").addEventListener("submit", e => { e.preventDefault(); const id = Number($("#bill-table").value); if (id) generateBill(id); });
$("#dialog-close").addEventListener("click", () => $("#detail-dialog").close());
$("#detail-dialog").addEventListener("click", e => { if (e.target === $("#detail-dialog")) $("#detail-dialog").close(); });

activateTab("tables");
refreshAll();
