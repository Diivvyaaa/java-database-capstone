package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   DoctorService doctorService,
                   PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    public ResponseEntity validateToken(String token, String user) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean isValid = tokenService.validateToken(token, user);
            if (!isValid) {
                response.put("message", "Invalid or expired token");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            response.put("message", "Token is valid");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        try {
            Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());
            if (admin == null || !admin.getPassword().equals(receivedAdmin.getPassword())) {
                response.put("message", "Invalid username or password");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            String token = tokenService.generateToken(admin.getUsername());
            response.put("token", token);
            response.put("message", "Admin login successful");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", "Internal server error during admin validation");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        boolean hasName = isProvided(name);
        boolean hasSpecialty = isProvided(specialty);
        boolean hasTime = isProvided(time);

        if (hasName && hasSpecialty && hasTime) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
        } else if (hasName && hasTime) {
            return doctorService.filterDoctorByNameAndTime(name, time);
        } else if (hasName && hasSpecialty) {
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        } else if (hasSpecialty && hasTime) {
            return doctorService.filterDoctorByTimeAndSpecility(specialty, time);
        } else if (hasName) {
            return doctorService.findDoctorByName(name);
        } else if (hasSpecialty) {
            return doctorService.filterDoctorBySpecility(specialty);
        } else if (hasTime) {
            return doctorService.filterDoctorsByTime(time);
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("doctors", doctorService.getDoctors());
            return result;
        }
    }

    public int validateAppointment(Appointment appointment) {
        if (appointment == null || appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            return -1;
        }

        Long doctorId = appointment.getDoctor().getId();
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return -1;
        }

        LocalDate appointmentDate = appointment.getAppointmentTime().toLocalDate();
        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, appointmentDate);

        String requestedTime = appointment.getAppointmentTime()
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm"));

        for (String slot : availableSlots) {
            String startSlot = slot.split("-")[0].trim();
            if (startSlot.length() == 4) {
                startSlot = "0" + startSlot;
            }
            if (startSlot.equals(requestedTime) || slot.trim().equals(requestedTime)) {
                return 1;
            }
        }

        return 0;
    }

    public boolean validatePatient(Patient patient) {
        Patient existing = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
        return existing == null;
    }

    public ResponseEntity validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        try {
            Patient patient = patientRepository.findByEmail(login.getIdentifier());
            if (patient == null || !patient.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid email or password");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            String token = tokenService.generateToken(patient.getEmail());
            response.put("token", token);
            response.put("message", "Login successful");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", "Internal server error during patient login");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity filterPatient(String condition, String name, String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = tokenService.extractEmail(token);
            Patient patient = patientRepository.findByEmail(email);

            if (patient == null) {
                response.put("message", "Patient not found or unauthorized");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            Long patientId = patient.getId();
            boolean hasCondition = isProvided(condition);
            boolean hasName = isProvided(name);

            if (hasCondition && hasName) {
               return patientService.filterByDoctorAndCondition(condition, name, patientId);
            } else if (hasCondition) {
                return patientService.filterByCondition(condition, patientId);
            } else if (hasName) {
                return patientService.filterByDoctor(name, patientId);
            } else {
                return patientService.getPatientAppointment(patientId, token);
            }
        } catch (Exception e) {
            response.put("message", "Internal server error while filtering patient appointments");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isProvided(String value) {
        return value != null && !value.trim().isEmpty() && !value.equalsIgnoreCase("null");
    }
}