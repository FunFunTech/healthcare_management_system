package com.healthcare.repository;

import com.healthcare.model.Doctor;
import com.healthcare.model.MedicalRecord;
import com.healthcare.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatient(Patient patient);
    List<MedicalRecord> findByDoctor(Doctor doctor);
    List<MedicalRecord> findByDoctorAndPatient(Doctor doctor, Patient patient);

    @Query("SELECT r FROM MedicalRecord r WHERE r.doctor = :doctor AND (" +
           "LOWER(r.patient.user.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "CAST(r.patient.id AS string) LIKE CONCAT('%', :search, '%'))")
    List<MedicalRecord> searchByDoctorAndPatient(@Param("doctor") Doctor doctor, @Param("search") String search);
}
