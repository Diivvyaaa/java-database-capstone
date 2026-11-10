/* app/src/main/resources/static/js/adminDashboard.js */

// 1. Import Required Modules
import { openModal } from "./components/modals.js";
import { getDoctors, filterDoctors, saveDoctor } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

// 2. Initialize Dashboard on DOM Load
document.addEventListener("DOMContentLoaded", () => {
  // Bind Add Doctor button (injected via header.js)
  const addDocBtn = document.getElementById("addDocBtn");
  if (addDocBtn) {
    addDocBtn.addEventListener("click", () => {
      openModal("addDoctor");
    });
  }

  // Setup Search and Filter Event Listeners
  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("timeFilter") || document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("specialtyFilter") || document.getElementById("filterSpecialty");

  if (searchBar) {
    searchBar.addEventListener("input", filterDoctorsOnChange);
  }
  if (filterTime) {
    filterTime.addEventListener("change", filterDoctorsOnChange);
  }
  if (filterSpecialty) {
    filterSpecialty.addEventListener("change", filterDoctorsOnChange);
  }

  // Fetch and display initial doctor list
  loadDoctorCards();
});

/**
 * 3. Fetch all doctors and display them in the dashboard content area.
 */
export async function loadDoctorCards() {
  const contentDiv = document.getElementById("content");
  if (!contentDiv) return;

  try {
    const doctors = await getDoctors();
    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Error loading doctor cards:", error);
    contentDiv.innerHTML = `<p class="noPatientRecord">Failed to load doctors. Please try again later.</p>`;
  }
}

/**
 * 4. Helper function to render a list of doctor cards.
 * @param {Array} doctors - List of doctor records.
 */
export function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  if (!contentDiv) return;

  contentDiv.innerHTML = "";

  if (!doctors || doctors.length === 0) {
    contentDiv.innerHTML = `<p class="noPatientRecord">No doctors found with the given filters.</p>`;
    return;
  }

  doctors.forEach((doctor) => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });
}

/**
 * 5. Gathers current filter inputs and fetches matching doctor records.
 */
export async function filterDoctorsOnChange() {
  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("timeFilter") || document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("specialtyFilter") || document.getElementById("filterSpecialty");

  const name = searchBar && searchBar.value.trim() !== "" ? searchBar.value.trim() : null;
  const time = filterTime && filterTime.value !== "" ? filterTime.value : null;
  const specialty = filterSpecialty && filterSpecialty.value !== "" ? filterSpecialty.value : null;

  try {
    const filteredDoctors = await filterDoctors(name, time, specialty);
    renderDoctorCards(filteredDoctors);
  } catch (error) {
    console.error("Error filtering doctors:", error);
    alert("An error occurred while filtering doctor records.");
  }
}

/**
 * 6. Collects data from Add Doctor modal form and saves the new record.
 * Exposed globally on window so modal submission handlers can invoke it directly.
 */
window.adminAddDoctor = async function () {
  const token = localStorage.getItem("token");
  if (!token) {
    alert("Session expired. Please log in again.");
    window.location.href = "/";
    return;
  }

  const nameInput = document.getElementById("docName");
  const emailInput = document.getElementById("docEmail");
  const phoneInput = document.getElementById("docPhone");
  const passwordInput = document.getElementById("docPassword");
  const specialtySelect = document.getElementById("docSpecialty");

  // Collect checked availability checkboxes (e.g., AM, PM, or specific timeslots)
  const timeCheckboxes = document.querySelectorAll("input[name='docTime']:checked");
  const availableTimes = Array.from(timeCheckboxes).map((cb) => cb.value);

  const doctorData = {
    name: nameInput ? nameInput.value.trim() : "",
    email: emailInput ? emailInput.value.trim() : "",
    phone: phoneInput ? phoneInput.value.trim() : "",
    password: passwordInput ? passwordInput.value : "",
    specialty: specialtySelect ? specialtySelect.value : "",
    availableTimes: availableTimes
  };

  if (!doctorData.name || !doctorData.email || !doctorData.password || !doctorData.specialty) {
    alert("Please fill in all required fields.");
    return;
  }

  try {
    const result = await saveDoctor(doctorData, token);

    if (result && result.success) {
      alert("Doctor added successfully!");

      const modal = document.getElementById("modal");
      if (modal) {
        modal.style.display = "none";
      }

      // Reload updated doctor cards list
      loadDoctorCards();
    } else {
      alert(result?.message || "Failed to add doctor. Please verify details.");
    }
  } catch (error) {
    console.error("Error saving doctor:", error);
    alert("An unexpected error occurred while adding the doctor.");
  }
};