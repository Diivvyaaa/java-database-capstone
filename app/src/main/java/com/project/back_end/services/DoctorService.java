package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.model.Appointment;
import com.project.back_end.model.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Doctor doctor = doctorOpt.get();
        List<String> availableSlots = new ArrayList<>(doctor.getAvailableTimes());

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        Set<String> bookedTimes = appointments.stream()
                .map(a -> a.getAppointmentTime().toLocalTime().format(timeFormatter))
                .collect(Collectors.toSet());

        return availableSlots.stream()
                .filter(slot -> {
                    String startSlot = slot.split("-")[0].trim();
                    if (startSlot.length() == 4) {
                        startSlot = "0" + startSlot;
                    }
                    return !bookedTimes.contains(startSlot) && !bookedTimes.contains(slot.trim());
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            Doctor existing = doctorRepository.findByEmail(doctor.getEmail());
            if (existing != null) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public int updateDoctor(Doctor doctor) {
        try {
            if (doctor.getId() == null || !doctorRepository.existsById(doctor.getId())) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        doctors.forEach(d -> d.getAvailableTimes().size());
        return doctors;
    }

    @Transactional
    public int deleteDoctor(long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return -1;
            }
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public <ResponseEntity> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();
        Doctor doctor = doctorRepository.findByEmail(login.getIdentifier());

        if (doctor == null) {
            response.put("message", "Doctor not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if (!doctor.getPassword().equals(login.getPassword())) {
            response.put("message", "Invalid credentials");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String token = tokenService.generateToken(doctor.getEmail());
        response.put("token", token);
        response.put("message", "Login successful");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        doctors.forEach(d -> d.getAvailableTimes().size());
        response.put("doctors", doctors);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository
                .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        doctors.forEach(d -> d.getAvailableTimes().size());
        List<Doctor> filtered = filterDoctorByTime(doctors, amOrPm);
        response.put("doctors", filtered);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        doctors.forEach(d -> d.getAvailableTimes().size());
        List<Doctor> filtered = filterDoctorByTime(doctors, amOrPm);
        response.put("doctors", filtered);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specilty) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository
                .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specilty);
        doctors.forEach(d -> d.getAvailableTimes().size());
        response.put("doctors", doctors);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specilty, String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specilty);
        doctors.forEach(d -> d.getAvailableTimes().size());
        List<Doctor> filtered = filterDoctorByTime(doctors, amOrPm);
        response.put("doctors", filtered);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorBySpecility(String specilty) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specilty);
        doctors.forEach(d -> d.getAvailableTimes().size());
        response.put("doctors", doctors);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findAll();
        doctors.forEach(d -> d.getAvailableTimes().size());
        List<Doctor> filtered = filterDoctorByTime(doctors, amOrPm);
        response.put("doctors", filtered);
        return response;
    }

    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        if (amOrPm == null || amOrPm.trim().isEmpty()) {
            return doctors;
        }

        String period = amOrPm.trim().toUpperCase();

        return doctors.stream()
                .filter(doctor -> doctor.getAvailableTimes() != null &&
                        doctor.getAvailableTimes().stream().anyMatch(slot -> isSlotInPeriod(slot, period)))
                .collect(Collectors.toList());
    }

    private boolean isSlotInPeriod(String slot, String period) {
        if (slot == null || slot.isEmpty()) {
            return false;
        }
        String upperSlot = slot.toUpperCase();
        if (upperSlot.contains("AM") || upperSlot.contains("PM")) {
            return upperSlot.contains(period);
        }
        try {
            String startPart = slot.split("-")[0].trim();
            int hour = Integer.parseInt(startPart.split(":")[0].trim());
            if ("AM".equals(period)) {
                return hour < 12;
            } else if ("PM".equals(period)) {
                return hour >= 12;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }
}