/* doctorCard.js */

// 1. Imports from service and component modules
import { showBookingOverlay } from "../components/loggedPatient.js";
import { deleteDoctor } from "../services/doctorServices.js";
import { getPatientData } from "../services/patientServices.js";

/**
 * Creates and returns a DOM element representing a doctor card.
 * @param {Object} doctor - Doctor entity data (id, name, specialty, email, availableTimes/timeSlots).
 * @returns {HTMLElement} card - The assembled doctor card DOM element.
 */
export function createDoctorCard(doctor) {
  // 2. Create the Main Card Container
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  // 3. Fetch the User’s Role from localStorage
  const role = localStorage.getItem("userRole");

  // 4. Create Doctor Info Section
  const infoDiv = document.createElement("div");
  infoDiv.classList.add("doctor-info");

  // Doctor Name
  const name = document.createElement("h3");
  name.textContent = doctor.name || "Doctor Name";

  // Doctor Specialization
  const specialization = document.createElement("p");
  specialization.classList.add("doctor-specialty");
  specialization.textContent = `Specialty: ${doctor.specialty || doctor.specialization || "General"}`;

  // Doctor Email
  const email = document.createElement("p");
  email.classList.add("doctor-email");
  email.textContent = `Email: ${doctor.email || "N/A"}`;

  // Doctor Availability
  const availability = document.createElement("p");
  availability.classList.add("doctor-availability");
  const timeSlots = Array.isArray(doctor.availableTimes)
    ? doctor.availableTimes.join(", ")
    : Array.isArray(doctor.timeSlots)
    ? doctor.timeSlots.join(", ")
    : doctor.availableTimes || "Schedule not available";
  availability.textContent = `Available: ${timeSlots}`;

  // Append elements to Info Container
  infoDiv.appendChild(name);
  infoDiv.appendChild(specialization);
  infoDiv.appendChild(email);
  infoDiv.appendChild(availability);

  // 5. Create Button Container
  const actionsDiv = document.createElement("div");
  actionsDiv.classList.add("card-actions");

  // 6. Conditionally Add Buttons Based on Role

  // === ADMIN ROLE ACTIONS ===
  if (role === "admin") {
    const removeBtn = document.createElement("button");
    removeBtn.textContent = "Delete";
    removeBtn.classList.add("adminBtn");

    removeBtn.addEventListener("click", async () => {
      const confirmDelete = confirm(`Are you sure you want to delete ${doctor.name}?`);
      if (!confirmDelete) return;

      const token = localStorage.getItem("token");
      if (!token) {
        alert("Session expired. Please log in again.");
        window.location.href = "/";
        return;
      }

      try {
        const success = await deleteDoctor(doctor.id, token);
        if (success) {
          alert("Doctor removed successfully.");
          card.remove();
        } else {
          alert("Failed to delete doctor. Please try again.");
        }
      } catch (error) {
        console.error("Error deleting doctor:", error);
        alert("An error occurred while deleting doctor.");
      }
    });

    actionsDiv.appendChild(removeBtn);
  }

  // === PATIENT (NOT LOGGED-IN) ROLE ACTIONS ===
  else if (role === "patient") {
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.classList.add("btn");

    bookNow.addEventListener("click", () => {
      alert("Patient needs to login first.");
    });

    actionsDiv.appendChild(bookNow);
  }

  // === LOGGED-IN PATIENT ROLE ACTIONS ===
  else if (role === "loggedPatient") {
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.classList.add("btn");

    bookNow.addEventListener("click", async (e) => {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("Session expired. Please log in again.");
        window.location.href = "/";
        return;
      }

      try {
        const patientData = await getPatientData(token);
        if (patientData) {
          showBookingOverlay(e, doctor, patientData);
        } else {
          alert("Unable to fetch patient profile. Please log in again.");
        }
      } catch (error) {
        console.error("Error preparing booking overlay:", error);
        alert("Failed to initiate booking. Please try again later.");
      }
    });

    actionsDiv.appendChild(bookNow);
  }

  // 7. Final Assembly
  card.appendChild(infoDiv);
  card.appendChild(actionsDiv);

  return card;
}