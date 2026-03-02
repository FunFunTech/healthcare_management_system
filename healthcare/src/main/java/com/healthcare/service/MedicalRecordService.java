package com.healthcare.service;

import com.healthcare.model.*;
import com.healthcare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MedicalRecordService {

    @Autowired private MedicalRecordRepository recordRepository;
    @Autowired private MedicationRepository medicationRepository;

    public MedicalRecord save(MedicalRecord record) {
        return recordRepository.save(record);
    }

    public Optional<MedicalRecord> findById(Long id) {
        return recordRepository.findById(id);
    }

    public List<MedicalRecord> findByPatient(Patient patient) {
        return recordRepository.findByPatient(patient);
    }

    public List<MedicalRecord> findByDoctor(Doctor doctor) {
        return recordRepository.findByDoctor(doctor);
    }

    public List<MedicalRecord> searchByDoctorAndPatient(Doctor doctor, String search) {
        if (search == null || search.isEmpty()) return recordRepository.findByDoctor(doctor);
        return recordRepository.searchByDoctorAndPatient(doctor, search);
    }

    public void delete(Long id) {
        recordRepository.deleteById(id);
    }

    // Medications
    public Medication saveMedication(Medication medication) {
        return medicationRepository.save(medication);
    }

    public List<Medication> getMedicationsByRecord(MedicalRecord record) {
        return medicationRepository.findByMedicalRecord(record);
    }

    public List<Medication> getMedicationsByPatient(Patient patient) {
        return medicationRepository.findByPatient(patient);
    }

    public List<Medication> getActiveMedicationsByPatient(Patient patient) {
        return medicationRepository.findByPatientAndActive(patient, true);
    }

    public List<Medication> getMedicationsByDoctor(Doctor doctor) {
        return medicationRepository.findByDoctor(doctor);
    }

    public Optional<Medication> findMedicationById(Long id) {
        return medicationRepository.findById(id);
    }

    public void deleteMedication(Long id) {
        medicationRepository.deleteById(id);
    }
}
