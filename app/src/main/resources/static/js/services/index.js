/* js/services/index.js */

// 1. Import Required Modules
import { openModal } from "../components/modals.js";
import { API_BASE_URL } from "../config/config.js";

// Define login API endpoints
const ADMIN_API = API_BASE_URL + "/admin";
const DOCTOR_API = API_BASE_URL + "/doctor/login";

// 2. Setup Button Event Listeners
window.onload = function () {
  const adminBtn = document.getElementById("adminLogin");
  if (adminBtn) {
    adminBtn.addEventListener("click", () => {
      openModal("adminLogin");
    });
  }

  const doctorBtn = document.getElementById("doctorLogin");
  if (doctorBtn) {
    doctorBtn.addEventListener("click", () => {
      openModal("doctorLogin");
    });
  }
};

// 3. Implement Admin Login Handler
window.adminLoginHandler = async function () {
  const usernameInput = document.getElementById("adminUsername");
  const passwordInput = document.getElementById("adminPassword");

  const username = usernameInput ? usernameInput.value.trim() : "";
  const password = passwordInput ? passwordInput.value : "";

  const admin = { username, password };

  try {
    const response = await fetch(ADMIN_API, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(admin)
    });

    if (response.ok) {
      const data = await response.json();
      const token = data.token || data;
      localStorage.setItem("token", token);
      if (typeof selectRole === "function") {
        selectRole("admin");
      }
    } else {
      alert("Invalid credentials!");
    }
  } catch (error) {
    console.error("Admin login error:", error);
    alert("An unexpected error occurred during admin login.");
  }
};

// 4. Implement Doctor Login Handler
window.doctorLoginHandler = async function () {
  const emailInput = document.getElementById("doctorEmail");
  const passwordInput = document.getElementById("doctorPassword");

  const email = emailInput ? emailInput.value.trim() : "";
  const password = passwordInput ? passwordInput.value : "";

  const doctor = { email, password };

  try {
    const response = await fetch(DOCTOR_API, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(doctor)
    });

    if (response.ok) {
      const data = await response.json();
      const token = data.token || data;
      localStorage.setItem("token", token);
      if (typeof selectRole === "function") {
        selectRole("doctor");
      }
    } else {
      alert("Invalid credentials!");
    }
  } catch (error) {
    console.error("Doctor login error:", error);
    alert("An unexpected error occurred during doctor login.");
  }
};