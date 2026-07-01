const API_BASE = "http://localhost:8081"; // Gateway

// ====== AUTHENTICATION HELPER ======
function getHeaders() {
  const token = localStorage.getItem("token");
  return {
    "Content-Type": "application/json",
    "Authorization": token ? `Bearer ${token}` : ""
  };
}

// ====== TOAST NOTIFICATION ======
function showToast(message, type = "success") {
  const container = document.getElementById("toast-container");
  if (!container) return;
  
  const toast = document.createElement("div");
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <span>${message}</span>
    <button onclick="this.parentElement.remove()" style="background:none;border:none;color:white;cursor:pointer;font-size:1.1rem;margin-left:10px;">&times;</button>
  `;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateX(50px)";
    toast.style.transition = "all 0.3s ease";
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

function checkAuth() {
  const token = localStorage.getItem("token");
  const username = localStorage.getItem("username");
  const role = localStorage.getItem("role");

  const authSection = document.getElementById("auth-section");
  const mainNav = document.getElementById("main-nav");
  const userProfile = document.getElementById("user-profile");
  const usernameBadge = document.getElementById("username-badge");
  const roleBadge = document.getElementById("role-badge");
  const adminBtn = document.getElementById("admin-btn");

  if (token) {
    authSection.classList.add("hidden");
    mainNav.classList.remove("hidden");
    userProfile.classList.remove("hidden");
    usernameBadge.textContent = username;
    roleBadge.textContent = role;

    // Show/hide elements based on roles
    if (role === "ADMIN") {
      adminBtn.classList.remove("hidden");
      document.getElementById("ngoForm").classList.remove("hidden");
      document.getElementById("donationForm").classList.add("hidden");
    } else if (role === "NGO") {
      adminBtn.classList.add("hidden");
      document.getElementById("ngoForm").classList.add("hidden");
      document.getElementById("donationForm").classList.add("hidden");
    } else {
      adminBtn.classList.add("hidden");
      document.getElementById("ngoForm").classList.add("hidden");
      document.getElementById("donationForm").classList.remove("hidden");
    }

    // Default view: Dashboard
    showSection("dashboard-section", document.getElementById("dashboard-btn"));
  } else {
    authSection.classList.remove("hidden");
    mainNav.classList.add("hidden");
    userProfile.classList.add("hidden");
    
    // Hide all main cards
    document.getElementById("dashboard-section").classList.add("hidden");
    document.getElementById("ngo-section").classList.add("hidden");
    document.getElementById("donation-section").classList.add("hidden");
    document.getElementById("admin-section").classList.add("hidden");
  }
}

let isSignUp = false;
let editingNgoId = null;
let editingDonationId = null;

function toggleAuthMode(e) {
  e.preventDefault();
  isSignUp = !isSignUp;
  
  const title = document.getElementById("auth-title");
  const submitBtn = document.getElementById("authSubmitBtn");
  const toggleText = document.getElementById("auth-toggle-text");
  const signupFields = document.getElementById("signup-fields");

  if (isSignUp) {
    title.textContent = "Sign Up";
    submitBtn.textContent = "Sign Up";
    signupFields.classList.remove("hidden");
    toggleText.innerHTML = `Already have an account? <a href="#" onclick="toggleAuthMode(event)">Sign In</a>`;
    toggleRegNoField();
  } else {
    title.textContent = "Sign In";
    submitBtn.textContent = "Sign In";
    signupFields.classList.add("hidden");
    toggleText.innerHTML = `Don't have an account? <a href="#" onclick="toggleAuthMode(event)">Sign Up</a>`;
  }
}

function toggleRegNoField() {
  const role = document.getElementById("authRole").value;
  const regNoField = document.getElementById("authNgoRegNo");
  const nameField = document.getElementById("authNgoName");
  const causeField = document.getElementById("authNgoCause");
  
  if (role === "NGO") {
    regNoField.style.display = "block";
    regNoField.required = true;
    nameField.style.display = "block";
    nameField.required = true;
    causeField.style.display = "block";
    causeField.required = true;
  } else {
    regNoField.style.display = "none";
    regNoField.required = false;
    nameField.style.display = "none";
    nameField.required = false;
    causeField.style.display = "none";
    causeField.required = false;
  }
}

async function handleAuthSubmit(e) {
  e.preventDefault();
  const username = document.getElementById("authUsername").value;
  const password = document.getElementById("authPassword").value;

  if (isSignUp) {
    const email = document.getElementById("authEmail").value;
    const role = document.getElementById("authRole").value;
    const ngoName = role === "NGO" ? document.getElementById("authNgoName").value : null;
    const ngoCause = role === "NGO" ? document.getElementById("authNgoCause").value : null;
    const ngoRegNo = role === "NGO" ? document.getElementById("authNgoRegNo").value : null;

    const payload = { username, password, email, role, ngoRegNo, ngoName, ngoCause };
    try {
      const res = await fetch(`${API_BASE}/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      const data = await res.text();
      if (!res.ok) throw new Error(data);

      showToast("Registration successful! (NGOs require Admin verification before logging in)", "success");
      isSignUp = false;
      document.getElementById("authForm").reset();
      toggleAuthMode(e);
    } catch (err) {
      showToast("Registration failed: " + err.message, "error");
    }
  } else {
    const payload = { username, password };
    try {
      const res = await fetch(`${API_BASE}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || "Login failed");
      }
      
      const data = await res.json();
      localStorage.setItem("token", data.token);
      localStorage.setItem("username", data.username);
      localStorage.setItem("role", data.role);
      if (data.ngoRegNo) {
        localStorage.setItem("ngoRegNo", data.ngoRegNo);
      }
      
      document.getElementById("authForm").reset();
      showToast("Signed in successfully!", "success");
      checkAuth();
    } catch (err) {
      showToast("Login failed: " + err.message, "error");
    }
  }
}

function logout() {
  localStorage.clear();
  checkAuth();
  showToast("Logged out successfully.", "info");
}

// ====== DASHBOARD COMPILATION ======
async function loadDashboardData() {
  try {
    // 1. Fetch NGOs count
    const resNgos = await fetch(`${API_BASE}/ngos`, { headers: getHeaders() });
    const ngos = resNgos.ok ? await resNgos.json() : [];
    
    // 2. Fetch Donations count
    const resDons = await fetch(`${API_BASE}/donations`, { headers: getHeaders() });
    const donations = resDons.ok ? await resDons.json() : [];

    // Compute stats
    const totalDonations = donations.reduce((sum, d) => sum + d.amount, 0);
    const activeNgosCount = ngos.filter(n => n.verified).length;
    const role = localStorage.getItem("role");

    // Populate UI
    document.getElementById("kpi-donations-val").textContent = `$${totalDonations.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    document.getElementById("kpi-ngos-val").textContent = activeNgosCount;
    document.getElementById("kpi-status-val").textContent = role === "ADMIN" ? "SYSTEM OK" : "ACTIVE";
    
    // Populate Activity Feed
    const feed = document.getElementById("activityFeed");
    feed.innerHTML = "";

    const activities = [];
    donations.slice(-3).forEach(d => {
      activities.push({
        desc: `Donor <strong>${d.donorName}</strong> donated <span style="color:var(--accent-emerald)">$${d.amount}</span> to <strong>${d.ngoName || d.ngoRegNo}</strong>`,
        time: d.donationDate ? new Date(d.donationDate).toLocaleTimeString() : "Recent"
      });
    });

    ngos.slice(-2).forEach(n => {
      activities.push({
        desc: `NGO profile <strong>${n.name}</strong> (${n.registrationNumber}) is registered`,
        time: n.verified ? `<span class="badge emerald">Verified</span>` : `<span class="badge red">Pending Verification</span>`
      });
    });

    if (activities.length === 0) {
      feed.innerHTML = `<tr><td colspan="2" style="color:var(--text-secondary); text-align:center; padding:1.5rem;">No recent activities logged on this workspace.</td></tr>`;
    } else {
      activities.reverse().forEach(act => {
        feed.innerHTML += `
          <tr>
            <td style="padding: 0.8rem 1rem;">${act.desc}</td>
            <td style="padding: 0.8rem 1rem; text-align: right;">${act.time}</td>
          </tr>
        `;
      });
    }

  } catch (err) {
    console.error("Failed to load dashboard metrics:", err);
  }
}

// ====== NGO FUNCTIONS ======
async function fetchNgos() {
  try {
    const res = await fetch(`${API_BASE}/ngos`, {
      headers: getHeaders()
    });
    if (!res.ok) throw new Error(res.statusText);
    const ngos = await res.json();

    renderNgos(ngos);
    loadNgoDropdown(ngos);
  } catch (err) {
    console.error("Failed to fetch NGOs:", err);
  }
}

async function addNgo() {
  const name = document.getElementById("ngoName").value;
  const registrationNumber = document.getElementById("ngoRegNo").value;
  const cause = document.getElementById("ngoCause").value;

  const ngo = { name, registrationNumber, cause };

  try {
    const res = await fetch(`${API_BASE}/ngos`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(ngo)
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || res.status);
    }

    showToast("NGO Profile registered successfully!", "success");
    document.getElementById("ngoForm").reset();
    fetchNgos();
  } catch (err) {
    showToast("Failed to add NGO: " + err.message, "error");
  }
}

async function deleteNgo(id) {
  if (!confirm("Are you sure you want to delete this NGO?")) return;
  try {
    const res = await fetch(`${API_BASE}/ngos/${id}`, { 
      method: "DELETE",
      headers: getHeaders()
    });
    if (!res.ok) throw new Error(await res.text());
    showToast("NGO profile deleted.", "info");
    fetchNgos();
  } catch (err) {
    showToast("Failed to delete NGO: " + err.message, "error");
  }
}

// ====== DONATION FUNCTIONS ======
async function fetchDonations() {
  try {
    const res = await fetch(`${API_BASE}/donations`, {
      headers: getHeaders()
    });
    if (!res.ok) throw new Error(res.statusText);
    const donations = await res.json();

    renderDonations(donations);
  } catch (err) {
    console.error("Failed to fetch donations:", err);
  }
}

async function addDonation() {
  const amount = parseFloat(document.getElementById("donation-amount").value);
  const ngoRegNo = document.getElementById("donation-ngo").value;
  const ngoName = document.getElementById("donation-ngo")
    .selectedOptions[0]?.textContent.split(" - ")[1] ?? "";

  if (!ngoRegNo) {
    showToast("Please select an NGO.", "error");
    return;
  }

  const donation = { amount, ngoRegNo, ngoName };

  try {
    const res = await fetch(`${API_BASE}/donations`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(donation)
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || res.status);
    }

    showToast("Thank you for your donation!", "success");
    document.getElementById("donationForm").reset();
    fetchDonations();
  } catch (err) {
    showToast("Failed to log donation: " + err.message, "error");
  }
}

async function deleteDonation(id) {
  if (!confirm("Delete this donation record?")) return;
  try {
    const res = await fetch(`${API_BASE}/donations/${id}`, { 
      method: "DELETE",
      headers: getHeaders()
    });
    if (!res.ok) throw new Error(await res.text());
    showToast("Donation record deleted.", "info");
    fetchDonations();
  } catch (err) {
    showToast("Failed to delete record: " + err.message, "error");
  }
}

// ====== ADMIN PANEL FUNCTIONS ======
async function fetchPendingNgos() {
  try {
    const res = await fetch(`${API_BASE}/auth/admin/pending-ngos`, {
      headers: getHeaders()
    });
    if (!res.ok) throw new Error(res.statusText);
    const pending = await res.json();

    renderPendingNgos(pending);
  } catch (err) {
    console.error("Failed to fetch pending NGOs:", err);
  }
}

async function approveNgo(userId) {
  try {
    const res = await fetch(`${API_BASE}/auth/admin/approve-ngo/${userId}`, {
      method: "PUT",
      headers: getHeaders()
    });
    if (!res.ok) throw new Error(await res.text());
    
    showToast("NGO account verified successfully!", "success");
    fetchPendingNgos();
  } catch (err) {
    showToast("Failed to verify NGO: " + err.message, "error");
  }
}

// ====== RENDERERS ======
function renderNgos(ngos) {
  const grid = document.getElementById("ngoGrid");
  grid.innerHTML = "";
  const role = localStorage.getItem("role");

  if (ngos.length === 0) {
    grid.innerHTML = `<div style="grid-column: 1/-1; color:var(--text-secondary); text-align:center; padding:3rem;">No registered NGOs found.</div>`;
    return;
  }

  ngos.forEach(ngo => {
    let actionButtons = "";
    if (role === "ADMIN") {
      actionButtons = `
        <button class="edit-btn" onclick="editNgo('${ngo.id}', '${ngo.name}', '${ngo.registrationNumber}', '${ngo.cause}')">Edit</button>
        <button class="delete-btn" onclick="deleteNgo('${ngo.id}')">Delete</button>
      `;
    }

    const verificationBadge = ngo.verified 
      ? `<span class="badge emerald">Verified</span>` 
      : `<span class="badge red">Pending Verification</span>`;

    grid.innerHTML += `
      <div class="item-card">
        <span class="badge teal" style="align-self: flex-start; margin-bottom: 0.8rem;">${ngo.cause}</span>
        <h3>${ngo.name}</h3>
        <p>Registration No: <strong>${ngo.registrationNumber}</strong></p>
        <div class="item-footer">
          ${verificationBadge}
          <div class="item-actions">
            ${actionButtons}
          </div>
        </div>
      </div>
    `;
  });
}

function renderDonations(donations) {
  const table = document.getElementById("donationTable");
  table.innerHTML = "";
  const role = localStorage.getItem("role");

  if (donations.length === 0) {
    table.innerHTML = `<tr><td colspan="7" style="color:var(--text-secondary); text-align:center; padding:2rem;">No donations recorded.</td></tr>`;
    return;
  }

  donations.forEach(d => {
    const formattedDate = d.donationDate
      ? new Date(d.donationDate).toLocaleString("en-GB", {
          year: "numeric",
          month: "2-digit",
          day: "2-digit",
          hour: "2-digit",
          minute: "2-digit",
          second: "2-digit"
        }).replace(",", "")
      : "";

    let actionButtons = "";
    if (role === "DONOR") {
      actionButtons = `
        <button class="edit-btn" onclick="editDonation('${d.id}', '${d.amount}', '${d.ngoRegNo}')">Edit</button>
        <button class="delete-btn" onclick="deleteDonation('${d.id}')">Delete</button>
      `;
    } else {
      actionButtons = `<span style="color:var(--text-secondary); font-size:0.85rem;">No actions</span>`;
    }

    table.innerHTML += `
      <tr>
        <td>${d.id}</td>
        <td>${d.donorName}</td>
        <td><span style="color:var(--accent-emerald); font-weight:600;">$${d.amount.toFixed(2)}</span></td>
        <td><span class="badge purple">${d.ngoRegNo || ""}</span></td>
        <td>${d.ngoName || ""}</td>
        <td>${formattedDate}</td>
        <td><div style="display:flex; gap:6px; justify-content:center;">${actionButtons}</div></td>
      </tr>
    `;
  });
}

function renderPendingNgos(pending) {
  const table = document.getElementById("pendingNgoTable");
  table.innerHTML = "";
  
  if (pending.length === 0) {
    table.innerHTML = `<tr><td colspan="5" style="color:var(--text-secondary); text-align:center; padding:2rem;">No pending NGO verifications.</td></tr>`;
    return;
  }

  pending.forEach(u => {
    table.innerHTML += `
      <tr>
        <td>${u.id}</td>
        <td>${u.username}</td>
        <td>${u.email}</td>
        <td><span class="badge purple">${u.ngoRegNo || ""}</span></td>
        <td>
          <button class="edit-btn" style="background-color:var(--accent-emerald); border-color:var(--accent-emerald); color:white;" onclick="approveNgo('${u.id}')">Verify Account</button>
        </td>
      </tr>
    `;
  });
}

// ====== EDIT HANDLERS ======
function editNgo(id, name, regNo, cause) {
  editingNgoId = id;
  document.getElementById("ngoName").value = name;
  document.getElementById("ngoRegNo").value = regNo;
  document.getElementById("ngoCause").value = cause;

  const btn = document.querySelector("#ngoForm button");
  btn.textContent = "Update NGO";
  
  document.getElementById("ngoForm").scrollIntoView({ behavior: "smooth" });
}

async function updateNgo(id) {
  const name = document.getElementById("ngoName").value;
  const registrationNumber = document.getElementById("ngoRegNo").value;
  const cause = document.getElementById("ngoCause").value;

  const updatedNgo = { name, registrationNumber, cause };

  try {
    const res = await fetch(`${API_BASE}/ngos/${id}`, {
      method: "PUT",
      headers: getHeaders(),
      body: JSON.stringify(updatedNgo)
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || res.status);
    }

    showToast("NGO profile updated successfully!", "success");
    editingNgoId = null;
    document.getElementById("ngoForm").reset();
    document.querySelector("#ngoForm button").textContent = "Add NGO";
    fetchNgos();
  } catch (err) {
    showToast("Failed to update NGO: " + err.message, "error");
  }
}

function editDonation(id, amount, ngoRegNo) {
  editingDonationId = id;
  document.getElementById("donation-amount").value = amount;
  document.getElementById("donation-ngo").value = ngoRegNo;

  const btn = document.querySelector("#donationForm button");
  btn.textContent = "Update Donation";
  
  document.getElementById("donationForm").scrollIntoView({ behavior: "smooth" });
}

async function updateDonation(id) {
  const amount = parseFloat(document.getElementById("donation-amount").value);
  const ngoRegNo = document.getElementById("donation-ngo").value;
  const ngoName = document.getElementById("donation-ngo")
    .selectedOptions[0]?.textContent.split(" - ")[1] ?? "";

  const updatedDonation = { amount, ngoRegNo, ngoName };

  try {
    const res = await fetch(`${API_BASE}/donations/${id}`, {
      method: "PUT",
      headers: getHeaders(),
      body: JSON.stringify(updatedDonation)
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || res.status);
    }

    showToast("Donation record updated successfully!", "success");
    editingDonationId = null;
    document.getElementById("donationForm").reset();
    document.querySelector("#donationForm button").textContent = "Add Donation";
    fetchDonations();
  } catch (err) {
    showToast("Failed to update donation: " + err.message, "error");
  }
}

// ====== DROPDOWN LOADER ======
function loadNgoDropdown(ngos) {
  const dropdown = document.getElementById("donation-ngo");
  if (!dropdown) return;

  dropdown.innerHTML = "<option value=''>Select NGO</option>";
  ngos.forEach(ngo => {
    if (ngo.verified) { // Only display verified NGOs in dropdown
      const opt = document.createElement("option");
      opt.value = ngo.registrationNumber;
      opt.textContent = `${ngo.registrationNumber} - ${ngo.name}`;
      dropdown.appendChild(opt);
    }
  });
}

// ====== NAVIGATION ======
function showSection(sectionId, btn) {
  document.querySelectorAll(".card").forEach(c => c.classList.add("hidden"));
  document.getElementById(sectionId).classList.remove("hidden");

  document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
  btn.classList.add("active");

  const role = localStorage.getItem("role");

  if (sectionId === "dashboard-section") {
    loadDashboardData();
  } else if (sectionId === "donation-section") {
    fetchDonations();
    fetchNgos(); // refresh dropdown
  } else if (sectionId === "ngo-section") {
    fetchNgos();
  } else if (sectionId === "admin-section" && role === "ADMIN") {
    fetchPendingNgos();
  }
}

// ====== INIT ======
document.getElementById("ngoForm").onsubmit = e => {
  e.preventDefault();
  if (editingNgoId) {
    updateNgo(editingNgoId);
  } else {
    addNgo();
  }
};
document.getElementById("donationForm").onsubmit = e => {
  e.preventDefault();
  if (editingDonationId) {
    updateDonation(editingDonationId);
  } else {
    addDonation();
  }
};

// Check Auth on load
checkAuth();
