package com.healthcare.repository;

import com.healthcare.model.Doctor;
import com.healthcare.model.Medication;
import com.healthcare.model.MedicalRecord;
import com.healthcare.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findByPatient(Patient patient);
    List<Medication> findByDoctor(Doctor doctor);
    List<Medication> findByMedicalRecord(MedicalRecord record);
    List<Medication> findByPatientAndActive(Patient patient, boolean active);
}
