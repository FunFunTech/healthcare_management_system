package com.healthcare.service;

import com.healthcare.model.Patient;
import com.healthcare.model.User;
import com.healthcare.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PatientService {

    @Autowired private PatientRepository patientRepository;

    public Optional<Patient> findByUser(User user) {
        return patientRepository.findByUser(user);
    }

    public Optional<Patient> findById(Long id) {
        return patientRepository.findById(id);
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public List<Patient> search(String query) {
        if (query == null || query.isEmpty()) return findAll();
        return patientRepository.searchByNameOrEmail(query);
    }

    public Patient save(Patient patient) {
        return patientRepository.save(patient);
    }

    public void delete(Long id) {
        patientRepository.deleteById(id);
    }

    public long count() {
        return patientRepository.count();
    }
}
