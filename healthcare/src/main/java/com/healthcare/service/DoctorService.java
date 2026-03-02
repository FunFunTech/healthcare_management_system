package com.healthcare.service;

import com.healthcare.model.Doctor;
import com.healthcare.model.User;
import com.healthcare.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DoctorService {

    @Autowired private DoctorRepository doctorRepository;

    public Optional<Doctor> findByUser(User user) {
        return doctorRepository.findByUser(user);
    }

    public Optional<Doctor> findById(Long id) {
        return doctorRepository.findById(id);
    }

    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    public List<Doctor> search(String query) {
        if (query == null || query.isEmpty()) return findAll();
        return doctorRepository.searchByNameOrDepartment(query);
    }

    public Doctor save(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public void delete(Long id) {
        doctorRepository.deleteById(id);
    }

    public long count() {
        return doctorRepository.count();
    }

    public void addOffDay(Doctor doctor, String date) {
        doctor.getOffDays().add(date);
        doctorRepository.save(doctor);
    }

    public void removeOffDay(Doctor doctor, String date) {
        doctor.getOffDays().remove(date);
        doctorRepository.save(doctor);
    }
}
