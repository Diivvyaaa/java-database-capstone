package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String secret;

    private static final long EXPIRATION_TIME = 7L * 24 * 60 * 60 * 1000; // 7 days in milliseconds

    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String identifier) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(identifier)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractIdentifier(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Alias to support both extractIdentifier (lab instructions) and extractEmail (comment block / other services)
    public String extractEmail(String token) {
        return extractIdentifier(token);
    }

    public boolean validateToken(String token, String user) {
        try {
            String identifier = extractIdentifier(token);
            if (identifier == null || user == null) {
                return false;
            }

            return switch (user.toLowerCase()) {
                case "admin" -> adminRepository.findByUsername(identifier) != null;
                case "doctor" -> doctorRepository.findByEmail(identifier) != null;
                case "patient" -> patientRepository.findByEmail(identifier) != null;
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }
}