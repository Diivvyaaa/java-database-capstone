package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;

    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity> getAppointments(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String patientName,
            @PathVariable String token) {

        ResponseEntity> tokenValidation = service.validateToken(token, "doctor");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> errorResponse = new HashMap<>(tokenValidation.getBody());
            return new ResponseEntity<>(errorResponse, tokenValidation.getStatusCode());
        }

        Map<String, Object> appointments = appointmentService.getAppointment(patientName, date, token);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @PostMapping("/{token}")
    public ResponseEntity> bookAppointment(
            @PathVariable String token,
            @Valid @RequestBody Appointment appointment) {

        ResponseEntity> tokenValidation = service.validateToken(token, "patient");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            return tokenValidation;
        }

        Map<String, String> response = new HashMap<>();
        int validationResult = service.validateAppointment(appointment);

        if (validationResult == -1) {
            response.put("message", "Invalid doctor ID");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else if (validationResult == 0) {
            response.put("message", "Appointment slot is unavailable or already taken");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        int booked = appointmentService.bookAppointment(appointment);
        if (booked == 1) {
            response.put("message", "Appointment booked successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else {
            response.put("message", "Error booking appointment");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{token}")
    public ResponseEntity> updateAppointment(
            @PathVariable String token,
            @Valid @RequestBody Appointment appointment) {

        ResponseEntity> tokenValidation = service.validateToken(token, "patient");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            return tokenValidation;
        }

        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity> cancelAppointment(
            @PathVariable long id,
            @PathVariable String token) {

        ResponseEntity tokenValidation = service.validateToken(token, "patient");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            return tokenValidation;
        }

        return ResponseEntity appointmentService.cancelAppointment(id, token);
    }
}