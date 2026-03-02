package com.healthcare.config;

import com.healthcare.model.*;
import com.healthcare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create default admin if not exists
        if (!userRepository.existsByEmail("admin@healthcare.com")) {
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@healthcare.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setPhone("0000000000");
            userRepository.save(admin);
            System.out.println("✅ Default admin created: admin@healthcare.com / admin123");
        }

        // Create sample doctor
        if (!userRepository.existsByEmail("doctor@healthcare.com")) {
            User docUser = new User();
            docUser.setName("Dr. John Smith");
            docUser.setEmail("doctor@healthcare.com");
            docUser.setPassword(passwordEncoder.encode("doctor123"));
            docUser.setRole(Role.DOCTOR);
            docUser.setPhone("9876543210");
            User savedDoc = userRepository.save(docUser);

            Doctor doctor = new Doctor();
            doctor.setUser(savedDoc);
            doctor.setDepartment("Cardiology");
            doctor.setSpecialization("Heart Specialist");
            doctor.setQualification("MBBS, MD");
            doctor.setExperience("10 years");
            doctorRepository.save(doctor);
            System.out.println("✅ Sample doctor created: doctor@healthcare.com / doctor123");
        }

        // Create sample patient
        if (!userRepository.existsByEmail("patient@healthcare.com")) {
            User patUser = new User();
            patUser.setName("Alice Johnson");
            patUser.setEmail("patient@healthcare.com");
            patUser.setPassword(passwordEncoder.encode("patient123"));
            patUser.setRole(Role.PATIENT);
            patUser.setPhone("9123456789");
            User savedPat = userRepository.save(patUser);

            Patient patient = new Patient();
            patient.setUser(savedPat);
            patient.setGender("Female");
            patient.setBloodGroup("O+");
            patientRepository.save(patient);
            System.out.println("✅ Sample patient created: patient@healthcare.com / patient123");
        }
    }
}
