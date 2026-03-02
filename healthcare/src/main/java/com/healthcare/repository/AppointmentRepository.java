package com.healthcare.repository;

import com.healthcare.model.Appointment;
import com.healthcare.model.AppointmentStatus;
import com.healthcare.model.Doctor;
import com.healthcare.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findByPatient(Patient patient);
    List<Appointment> findByAppointmentDate(LocalDate date);
    List<Appointment> findByDoctorAndAppointmentDate(Doctor doctor, LocalDate date);
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> findByDoctorAndStatus(Doctor doctor, AppointmentStatus status);
    List<Appointment> findByPatientAndStatus(Patient patient, AppointmentStatus status);

    long countByAppointmentDate(LocalDate date);
    long countByDoctorAndAppointmentDate(Doctor doctor, LocalDate date);
    long countByDoctor(Doctor doctor);

    @Query("SELECT a FROM Appointment a WHERE " +
           "LOWER(a.patient.user.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.doctor.user.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Appointment> searchByPatientOrDoctor(@Param("search") String search);

    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND (" +
           "LOWER(a.patient.user.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Appointment> searchByDoctorAndPatient(@Param("doctor") Doctor doctor, @Param("search") String search);

    @Query("SELECT a.appointmentDate, COUNT(a) FROM Appointment a WHERE a.doctor = :doctor GROUP BY a.appointmentDate ORDER BY a.appointmentDate")
    List<Object[]> countAppointmentsByDateForDoctor(@Param("doctor") Doctor doctor);

    List<Appointment> findByAppointmentDateBetween(LocalDate start, LocalDate end);
    List<Appointment> findByDoctorAndAppointmentDateBetween(Doctor doctor, LocalDate start, LocalDate end);
}
