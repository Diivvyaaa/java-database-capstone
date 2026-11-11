package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final Service service;

    public PatientController(PatientService patientService, Service service) {
        this.patientService = patientService;
        this.service = service;
    }

    @GetMapping("/{token}")
    public <ResponseEntity> getPatient(@PathVariable String token) {
        ResponseEntity tokenValidation = service.validateToken(token, "patient");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> errorResponse = new HashMap<>(tokenValidation.getBody());
            return new ResponseEntity<>(errorResponse, tokenValidation.getStatusCode());
        }

        return patientService.getPatientDetails(token);
    }

    @PostMapping
    public <ResponseEntity> createPatient(@Valid @RequestBody Patient patient) {
        Map<String, String> response = new HashMap<>();

        if (!service.validatePatient(patient)) {
            response.put("message", "Patient with email id or phone no already exist");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        int result = patientService.createPatient(patient);
        if (result == 1) {
            response.put("message", "Signup successful");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else {
            response.put("message", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity> login(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    @GetMapping("/{id}/{token}")
    public <ResponseEntity> getPatientAppointment(
            @PathVariable Long id,
            @PathVariable String token) {

        ResponseEntity tokenValidation = service.validateToken(token, "patient");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> errorResponse = new HashMap<>(tokenValidation.getBody());
            return new ResponseEntity<>(errorResponse, tokenValidation.getStatusCode());
        }

        return patientService.getPatientAppointment(id, token);
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity filterPatientAppointment(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {

        ResponseEntity tokenValidation = service.validateToken(token, "patient");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> errorResponse = new HashMap<>(tokenValidation.getBody());
            return new ResponseEntity<>(errorResponse, tokenValidation.getStatusCode());
        }

        return service.filterPatient(condition, name, token);
    }
}