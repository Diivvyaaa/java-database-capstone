package com.project.back_end.services;

import com.project.back_end.model.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrescriptionService {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionService.class);

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public ResponseEntity savePrescription(Prescription prescription) {
        Map<String, String> response = new HashMap<>();
        try {
            List<Prescription> existing = prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());
            if (existing != null && !existing.isEmpty()) {
                response.put("message", "Prescription already exists for this appointment");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            prescriptionRepository.save(prescription);
            response.put("message", "Prescription saved");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving prescription: {}", e.getMessage(), e);
            response.put("message", "Internal server error while saving prescription");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity getPrescription(Long appointmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);
            response.put("prescription", prescriptions);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching prescription for appointment ID {}: {}", appointmentId, e.getMessage(), e);
            response.put("message", "Internal server error while fetching prescription");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}