package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final com.project.back_end.services.Service service;
    private final TokenService tokenService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              @Lazy com.project.back_end.services.Service service,
                              TokenService tokenService,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.service = service;
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public <ResponseEntity> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();

        Optional<Appointment> existingOpt = appointmentRepository.findById(appointment.getId());
        if (existingOpt.isEmpty()) {
            response.put("message", "Appointment not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Appointment existing = existingOpt.get();
        if (!existing.getPatient().getId().equals(appointment.getPatient().getId())) {
            response.put("message", "Patient ID mismatch");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        int validationResult = service.validateAppointment(appointment);
        if (validationResult == 1) {
            appointmentRepository.save(appointment);
            response.put("message", "Appointment updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else if (validationResult == -1) {
            response.put("message", "Invalid doctor ID");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else {
            response.put("message", "Appointment time is not available or already booked");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public <ResponseEntity> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();

        Optional<Appointment> existingOpt = appointmentRepository.findById(id);
        if (existingOpt.isEmpty()) {
            response.put("message", "Appointment not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Appointment appointment = existingOpt.get();
        String email = tokenService.extractEmail(token);
        Patient patient = patientRepository.findByEmail(email);

        if (patient == null || !appointment.getPatient().getId().equals(patient.getId())) {
            response.put("message", "Unauthorized to cancel this appointment");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        appointmentRepository.delete(appointment);
        response.put("message", "Appointment cancelled successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> result = new HashMap<>();

        String email = tokenService.extractEmail(token);
        Doctor doctor = doctorRepository.findByEmail(email);

        if (doctor == null) {
            result.put("appointments", List.of());
            return result;
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<Appointment> appointments;
        if (pname != null && !pname.trim().isEmpty() && !pname.equalsIgnoreCase("null")) {
            appointments = appointmentRepository
                    .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                            doctor.getId(), pname.trim(), start, end);
        } else {
            appointments = appointmentRepository
                    .findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), start, end);
        }

        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(a -> new AppointmentDTO(
                        a.getId(),
                        a.getDoctor().getId(),
                        a.getDoctor().getName(),
                        a.getPatient().getId(),
                        a.getPatient().getName(),
                        a.getPatient().getEmail(),
                        a.getPatient().getPhone(),
                        a.getPatient().getAddress(),
                        a.getAppointmentTime(),
                        a.getStatus()
                ))
                .collect(Collectors.toList());

        result.put("appointments", appointmentDTOs);
        return result;
    }

    @Transactional
    public void changeStatus(long id) {
        appointmentRepository.updateStatus(1, id);
    }
}