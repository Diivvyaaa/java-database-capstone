/* app/src/main/resources/static/js/services/patientServices.js */

// 1. Import the API Base URL
import { API_BASE_URL } from "../config/config.js";

// 2. Set the Base Patient API Endpoint
const PATIENT_API = API_BASE_URL + "/patient";

/**
 * 3. Handle Patient Signup / Registration.
 * @param {Object} data - Patient details (name, email, password, phone, address).
 * @returns {Promise<{success: boolean, message: string}>} Structured result.
 */
export async function patientSignup(data) {
  try {
    const response = await fetch(`${PATIENT_API}/signup`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(data)
    });

    const result = await response.json().catch(() => null);

    if (response.ok) {
      return {
        success: true,
        message: result?.message || "Patient registered successfully."
      };
    } else {
      return {
        success: false,
        message: result?.message || `Registration failed with status: ${response.status}`
      };
    }
  } catch (error) {
    console.error("Error in patientSignup():", error);
    return {
      success: false,
      message: "An unexpected network or server error occurred during signup."
    };
  }
}

/**
 * 4. Handle Patient Login Authentication.
 * @param {Object} data - Login credentials { email, password }.
 * @returns {Promise<Response>} Full fetch Response object for caller handling.
 */
export async function patientLogin(data) {
  try {
    const response = await fetch(`${PATIENT_API}/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(data)
    });

    return response;
  } catch (error) {
    console.error("Error in patientLogin():", error);
    throw error;
  }
}

/**
 * 5. Fetch profile data for the authenticated logged-in patient.
 * @param {string} token - Authentication bearer token.
 * @returns {Promise<Object|null>} Patient profile details, or null on error.
 */
export async function getPatientData(token) {
  try {
    const response = await fetch(`${PATIENT_API}/profile`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
      }
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch patient data: ${response.status}`);
    }

    const patient = await response.json();
    return patient || null;
  } catch (error) {
    console.error("Error in getPatientData():", error);
    return null;
  }
}

/**
 * 6. Fetch appointments for a patient or doctor view.
 * @param {number|string} id - Patient unique identifier.
 * @param {string} token - Authentication bearer token.
 * @param {string} user - Requesting role ('patient' or 'doctor').
 * @returns {Promise<Array|null>} List of appointments, or null on failure.
 */
export async function getPatientAppointments(id, token, user) {
  try {
    const url = `${PATIENT_API}/appointments?id=${encodeURIComponent(id)}&role=${encodeURIComponent(user)}`;

    const response = await fetch(url, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
      }
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch appointments: ${response.status}`);
    }

    const appointments = await response.json();
    return Array.isArray(appointments) ? appointments : [];
  } catch (error) {
    console.error(`Error in getPatientAppointments() for user [${user}]:`, error);
    return null;
  }
}

/**
 * 7. Filter appointments by condition/status and search name.
 * @param {string} condition - Appointment status/condition (e.g., 'pending', 'consulted').
 * @param {string} name - Patient or doctor search name.
 * @param {string} token - Authentication bearer token.
 * @returns {Promise<Array>} Filtered appointments list, or an empty array on error.
 */
export async function filterAppointments(condition = "", name = "", token) {
  try {
    const queryParams = new URLSearchParams();
    if (condition && condition.trim()) queryParams.append("condition", condition.trim());
    if (name && name.trim()) queryParams.append("name", name.trim());

    const url = `${PATIENT_API}/appointments/filter?${queryParams.toString()}`;

    const response = await fetch(url, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
      }
    });

    if (!response.ok) {
      throw new Error(`Filter request failed with status: ${response.status}`);
    }

    const data = await response.json();
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.error("Error in filterAppointments():", error);
    alert("Could not load filtered appointments. Please try again.");
    return [];
  }
}