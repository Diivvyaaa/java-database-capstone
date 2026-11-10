/* header.js */

/**
 * Renders the header dynamically based on the current URL,
 * user role, and session token state in localStorage.
 */
function renderHeader() {
  const headerDiv = document.getElementById("header");
  if (!headerDiv) return;

  // 1. Root page check (clear session data and render minimal header)
  if (window.location.pathname.endsWith("/") || window.location.pathname.endsWith("/index.html")) {
    localStorage.removeItem("userRole");
    localStorage.removeItem("token");
    headerDiv.innerHTML = `
      <header class="header">
        <div class="logo-section">
          <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
          <span class="logo-title">Hospital CMS</span>
        </div>
      </header>`;
    return;
  }

  // 2. Retrieve user session data
  const role = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");

  // 3. Validate authenticated roles have a valid token
  if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
    localStorage.removeItem("userRole");
    localStorage.removeItem("token");
    alert("Session expired or invalid login. Please log in again.");
    window.location.href = "/";
    return;
  }

  // 4. Base header markup
  let headerContent = `
    <header class="header">
      <div class="logo-section">
        <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
        <span class="logo-title">Hospital CMS</span>
      </div>
      <nav>`;

  // 5. Append role-specific navigation controls
  if (role === "admin") {
    headerContent += `
      <button id="addDocBtn" class="adminBtn" onclick="openModal('addDoctor')">Add Doctor</button>
      <a href="#" onclick="logout()">Logout</a>`;
  } else if (role === "doctor") {
    headerContent += `
      <button id="docHomeBtn" class="adminBtn" onclick="selectRole('doctor')">Home</button>
      <a href="#" onclick="logout()">Logout</a>`;
  } else if (role === "patient") {
    headerContent += `
      <button id="patientLogin" class="adminBtn">Login</button>
      <button id="patientSignup" class="adminBtn">Sign Up</button>`;
  } else if (role === "loggedPatient") {
    headerContent += `
      <button id="home" class="adminBtn" onclick="window.location.href='/pages/loggedPatientDashboard.html'">Home</button>
      <button id="patientAppointments" class="adminBtn" onclick="window.location.href='/pages/patientAppointments.html'">Appointments</button>
      <a href="#" onclick="logoutPatient()">Logout</a>`;
  }

  // Close nav and header tags
  headerContent += `
      </nav>
    </header>`;

  // 6. Inject HTML and attach dynamic listeners
  headerDiv.innerHTML = headerContent;
  attachHeaderButtonListeners();
}

/**
 * Attaches event listeners to dynamically rendered action buttons.
 */
function attachHeaderButtonListeners() {
  const patientLoginBtn = document.getElementById("patientLogin");
  if (patientLoginBtn) {
    patientLoginBtn.addEventListener("click", () => {
      if (typeof openModal === "function") {
        openModal("patientLogin");
      }
    });
  }

  const patientSignupBtn = document.getElementById("patientSignup");
  if (patientSignupBtn) {
    patientSignupBtn.addEventListener("click", () => {
      if (typeof openModal === "function") {
        openModal("patientSignup");
      }
    });
  }
}

/**
 * Global logout handler: clears authentication tokens and returns to root.
 */
function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("userRole");
  window.location.href = "/";
}

/**
 * Patient logout handler: resets session role back to guest "patient"
 * and returns to the unauthenticated patient dashboard.
 */
function logoutPatient() {
  localStorage.removeItem("token");
  localStorage.setItem("userRole", "patient");
  window.location.href = "/pages/patientDashboard.html";
}

// Initialize header rendering on script load
document.addEventListener("DOMContentLoaded", renderHeader);