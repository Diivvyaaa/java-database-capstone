// patientDashboard.js

// 1. Import Required Modules
import { createDoctorCard } from './components/doctorCard.js';
import { openModal } from './components/modals.js';
import { getDoctors, filterDoctors } from './services/doctorServices.js';
import { patientLogin, patientSignup } from './services/patientServices.js';

// 2. Load Doctor Cards on Page Load
document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();
});

// 3. Bind Modal Triggers for Login and Signup
document.addEventListener("DOMContentLoaded", () => {
  const btn = document.getElementById("patientSignup");
  if (btn) {
    btn.addEventListener("click", () => openModal("patientSignup"));
  }
});

document.addEventListener("DOMContentLoaded", () => {
  const loginBtn = document.getElementById("patientLogin");
  if (loginBtn) {
    loginBtn.addEventListener("click", () => openModal("patientLogin"));
  }
});

// 4. Search and Filter Logic
document.addEventListener("DOMContentLoaded", () => {
  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("filterSpecialty");

  if (searchBar) {
    searchBar.addEventListener("input", filterDoctorsOnChange);
  }
  if (filterTime) {
    filterTime.addEventListener("change", filterDoctorsOnChange);
  }
  if (filterSpecialty) {
    filterSpecialty.addEventListener("change", filterDoctorsOnChange);
  }
});

/**
 * Fetches all doctors and populates the content container.
 */
function loadDoctorCards() {
  getDoctors()
    .then((doctors) => {
      renderDoctorCards(doctors);
    })
    .catch((error) => {
      console.error("Failed to load doctors:", error);
      const contentDiv = document.getElementById("content");
      if (contentDiv) {
        contentDiv.innerHTML = "<p class='noPatientRecord'>Failed to load doctors. Please try again later.</p>";
      }
    });
}

/**
 * 5. Gathers input values, filters doctors via service layer, and updates UI.
 */
function filterDoctorsOnChange() {
  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("filterSpecialty");

  const nameVal = searchBar ? searchBar.value.trim() : "";
  const timeVal = filterTime ? filterTime.value : "";
  const specialtyVal = filterSpecialty ? filterSpecialty.value : "";

  const name = nameVal.length > 0 ? nameVal : null;
  const time = timeVal.length > 0 ? timeVal : null;
  const specialty = specialtyVal.length > 0 ? specialtyVal : null;

  filterDoctors(name, time, specialty)
    .then((response) => {
      const doctors = Array.isArray(response) ? response : (response?.doctors || []);
      renderDoctorCards(doctors);
    })
    .catch((error) => {
      console.error("Failed to filter doctors:", error);
      alert("An error occurred while filtering doctors.");
    });
}

/**
 * 6. Render Utility to display doctor cards dynamically.
 * @param {Array} doctors - List of doctor entities.
 */
export function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  if (!contentDiv) return;

  contentDiv.innerHTML = "";

  if (doctors && doctors.length > 0) {
    doctors.forEach((doctor) => {
      const card = createDoctorCard(doctor);
      contentDiv.appendChild(card);
    });
  } else {
    contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
  }
}

/**
 * 7. Handle Patient Signup submission.
 */
window.signupPatient = async function () {
  try {
    const name = document.getElementById("patientName")?.value.trim() || document.getElementById("name")?.value.trim();
    const email = document.getElementById("patientEmail")?.value.trim() || document.getElementById("email")?.value.trim();
    const password = document.getElementById("patientPassword")?.value || document.getElementById("password")?.value;
    const phone = document.getElementById("patientPhone")?.value.trim() || document.getElementById("phone")?.value.trim();
    const address = document.getElementById("patientAddress")?.value.trim() || document.getElementById("address")?.value.trim();

    if (!name || !email || !password) {
      alert("Please fill in all required fields.");
      return;
    }

    const data = { name, email, password, phone, address };
    const { success, message } = await patientSignup(data);

    if (success) {
      alert(message || "Signup successful! Please log in.");
      const modal = document.getElementById("modal");
      if (modal) {
        modal.style.display = "none";
      }
      window.location.reload();
    } else {
      alert(message || "Registration failed. Please try again.");
    }
  } catch (error) {
    console.error("Signup failed:", error);
    alert("An error occurred while signing up.");
  }
};

/**
 * 8. Handle Patient Login submission.
 */
window.loginPatient = async function () {
  try {
    const email = document.getElementById("loginEmail")?.value.trim() || document.getElementById("email")?.value.trim();
    const password = document.getElementById("loginPassword")?.value || document.getElementById("password")?.value;

    if (!email || !password) {
      alert("Please enter both email and password.");
      return;
    }

    const data = { email, password };
    const response = await patientLogin(data);

    if (response.ok) {
      const result = await response.json();
      const token = result.token || result;

      localStorage.setItem("token", token);
      if (typeof selectRole === "function") {
        selectRole("loggedPatient");
      } else {
        localStorage.setItem("userRole", "loggedPatient");
      }

      window.location.href = "/pages/loggedPatientDashboard.html";
    } else {
      alert("Invalid credentials!");
    }
  } catch (error) {
    console.error("Login failed:", error);
    alert("Failed to login. Please try again.");
  }
};