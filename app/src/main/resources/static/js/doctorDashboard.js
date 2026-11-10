/* doctorDashboard.js */

// 1. Import Required Modules
import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

// 2. Initialize Global Variables
const patientTableBody = document.getElementById("patientTableBody");
let selectedDate = new Date().toISOString().split("T")[0]; // 'YYYY-MM-DD'
const token = localStorage.getItem("token");
let patientName = null;

// 3. Setup Search Bar Functionality
const searchBar = document.getElementById("searchBar");
if (searchBar) {
  searchBar.addEventListener("input", (e) => {
    const value = e.target.value.trim();
    patientName = value !== "" ? value : "null";
    loadAppointments();
  });
}

// 4. Bind Event Listeners to Filter Controls
const todayButton = document.getElementById("todayButton");
const datePicker = document.getElementById("datePicker");

if (todayButton) {
  todayButton.addEventListener("click", () => {
    selectedDate = new Date().toISOString().split("T")[0];
    if (datePicker) {
      datePicker.value = selectedDate;
    }
    loadAppointments();
  });
}

if (datePicker) {
  datePicker.value = selectedDate;
  datePicker.addEventListener("change", (e) => {
    selectedDate = e.target.value;
    loadAppointments();
  });
}

/**
 * 5. Fetch and display appointments based on selected date, filter term, and auth token
 */
export async function loadAppointments() {
  if (!patientTableBody) return;

  // Clear existing content
  patientTableBody.innerHTML = "";

  try {
    const appointments = await getAllAppointments(selectedDate, patientName, token);

    // If no appointments returned or list is empty
    if (!appointments || appointments.length === 0) {
      patientTableBody.innerHTML = `
        <tr>
          <td colspan="5" class="noPatientRecord">No Appointments found for today.</td>
        </tr>
      `;
      return;
    }

    // Iterate through appointments and render rows
    appointments.forEach((appointment) => {
      const patient = {
        id: appointment.patientId || appointment.patient?.id || appointment.id,
        name: appointment.patientName || appointment.patient?.name || "N/A",
        phone: appointment.patientPhone || appointment.patient?.phone || "N/A",
        email: appointment.patientEmail || appointment.patient?.email || "N/A",
        appointmentId: appointment.appointmentId || appointment.id
      };

      const row = createPatientRow(patient);
      patientTableBody.appendChild(row);
    });
  } catch (error) {
    console.error("Error loading appointments:", error);
    patientTableBody.innerHTML = `
      <tr>
        <td colspan="5" class="noPatientRecord">Error loading appointments. Try again later.</td>
      </tr>
    `;
  }
}

// 6. Initial Render on Page Load
document.addEventListener("DOMContentLoaded", () => {
  if (typeof renderContent === "function") {
    renderContent();
  }
  loadAppointments();
});