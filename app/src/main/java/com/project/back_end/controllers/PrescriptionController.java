package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final Service service;
    private final AppointmentService appointmentService;

    public PrescriptionController(PrescriptionService prescriptionService,
                                  Service service,
                                  AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.service = service;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/{token}")
    public <ResponseEntity> savePrescription(
            @PathVariable String token,
            @Valid @RequestBody Prescription prescription) {

        ResponseEntity tokenValidation = service.validateToken(token, "doctor");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            return tokenValidation;
        }

        appointmentService.changeStatus(prescription.getAppointmentId());
        return prescriptionService.savePrescription(prescription);
    }

    @GetMapping("/{appointmentId}/{token}")
    public <ResponseEntity> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token) {

        ResponseEntity tokenValidation = service.validateToken(token, "doctor");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> errorResponse = new HashMap<>(tokenValidation.getBody());
            return new ResponseEntity<>(errorResponse, tokenValidation.getStatusCode());
        }

        return prescriptionService.getPrescription(appointmentId);
    }
}