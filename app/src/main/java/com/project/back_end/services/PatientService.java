package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.model.Appointment;
import com.project.back_end.model.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            logger.error("Error creating patient: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public <ResponseEntity> getPatientAppointment(Long id, String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = tokenService.extractEmail(token);
            Patient patient = patientRepository.findByEmail(email);

            if (patient == null || !patient.getId().equals(id)) {
                response.put("message", "Unauthorized access");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            List<Appointment> appointments = appointmentRepository.findByPatientId(id);
            List<AppointmentDTO> appointmentDTOs = convertToDTOList(appointments);

            response.put("appointments", appointmentDTOs);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching appointments for patient ID {}: {}", id, e.getMessage(), e);
            response.put("message", "Internal server error while fetching appointments");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public <ResponseEntity> filterByCondition(String condition, Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            int status;
            if ("past".equalsIgnoreCase(condition)) {
                status = 1;
            } else if ("future".equalsIgnoreCase(condition)) {
                status = 0;
            } else {
                response.put("message", "Invalid condition. Use 'past' or 'future'.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            List<Appointment> appointments = appointmentRepository
                    .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status);
            List<AppointmentDTO> appointmentDTOs = convertToDTOList(appointments);

            response.put("appointments", appointmentDTOs);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error filtering appointments by condition for patient ID {}: {}", id, e.getMessage(), e);
            response.put("message", "Internal server error while filtering appointments");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public <ResponseEntity> filterByDoctor(String name, Long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Appointment> appointments = appointmentRepository
                    .filterByDoctorNameAndPatientId(name, patientId);
            List<AppointmentDTO> appointmentDTOs = convertToDTOList(appointments);

            response.put("appointments", appointmentDTOs);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error filtering appointments by doctor for patient ID {}: {}", patientId, e.getMessage(), e);
            response.put("message", "Internal server error while filtering appointments by doctor");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public <ResponseEntity> filterByDoctorAndCondition(String condition, String name, long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            int status;
            if ("past".equalsIgnoreCase(condition)) {
                status = 1;
            } else if ("future".equalsIgnoreCase(condition)) {
                status = 0;
            } else {
                response.put("message", "Invalid condition. Use 'past' or 'future'.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            List<Appointment> appointments = appointmentRepository
                    .filterByDoctorNameAndPatientIdAndStatus(name, patientId, status);
            List<AppointmentDTO> appointmentDTOs = convertToDTOList(appointments);

            response.put("appointments", appointmentDTOs);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error filtering appointments by doctor and condition for patient ID {}: {}", patientId, e.getMessage(), e);
            response.put("message", "Internal server error while filtering appointments");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public <ResponseEntity> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = tokenService.extractEmail(token);
            Patient patient = patientRepository.findByEmail(email);

            if (patient == null) {
                response.put("message", "Patient not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            response.put("patient", patient);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching patient details from token: {}", e.getMessage(), e);
            response.put("message", "Internal server error while fetching patient details");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<AppointmentDTO> convertToDTOList(List<Appointment> appointments) {
        return appointments.stream()
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
    }
}