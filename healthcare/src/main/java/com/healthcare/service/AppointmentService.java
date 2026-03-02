package com.healthcare.service;

import com.healthcare.model.*;
import com.healthcare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AppointmentService {

    @Autowired private AppointmentRepository appointmentRepository;


    public Appointment save(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> findTodaysAppointments() {
        return appointmentRepository.findByAppointmentDate(LocalDate.now());
    }

    public List<Appointment> findByDoctor(Doctor doctor) {
        return appointmentRepository.findByDoctor(doctor);
    }

    public List<Appointment> findByPatient(Patient patient) {
        return appointmentRepository.findByPatient(patient);
    }

    public List<Appointment> findTodaysByDoctor(Doctor doctor) {
        return appointmentRepository.findByDoctorAndAppointmentDate(doctor, LocalDate.now());
    }

    public List<Appointment> findByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    public List<Appointment> searchAppointments(String search) {
        if (search == null || search.isEmpty()) return appointmentRepository.findAll();
        return appointmentRepository.searchByPatientOrDoctor(search);
    }

    public List<Appointment> searchAppointmentsByDoctor(Doctor doctor, String search) {
        if (search == null || search.isEmpty()) return appointmentRepository.findByDoctor(doctor);
        return appointmentRepository.searchByDoctorAndPatient(doctor, search);
    }

    public List<Appointment> filterAppointments(String search, String date, Long doctorId, Long patientId) {
        List<Appointment> appointments = findAll();

        if (search != null && !search.isEmpty()) {
            appointments = appointmentRepository.searchByPatientOrDoctor(search);
        }
        if (date != null && !date.isEmpty()) {
            LocalDate filterDate = LocalDate.parse(date);
            appointments = appointments.stream()
                    .filter(a -> a.getAppointmentDate().equals(filterDate))
                    .toList();
        }
        if (doctorId != null) {
            appointments = appointments.stream()
                    .filter(a -> a.getDoctor().getId().equals(doctorId))
                    .toList();
        }
        if (patientId != null) {
            appointments = appointments.stream()
                    .filter(a -> a.getPatient().getId().equals(patientId))
                    .toList();
        }
        return appointments;
    }

    public void updateStatus(Long id, AppointmentStatus status) {
        appointmentRepository.findById(id).ifPresent(a -> {
            a.setStatus(status);
            appointmentRepository.save(a);
        });
    }

    public void delete(Long id) {
        appointmentRepository.deleteById(id);
    }

    public long countToday() {
        return appointmentRepository.countByAppointmentDate(LocalDate.now());
    }

    public long countTodayByDoctor(Doctor doctor) {
        return appointmentRepository.countByDoctorAndAppointmentDate(doctor, LocalDate.now());
    }

    public long countTotalByDoctor(Doctor doctor) {
        return appointmentRepository.countByDoctor(doctor);
    }

    public List<Object[]> getAppointmentChartData(Doctor doctor) {
        return appointmentRepository.countAppointmentsByDateForDoctor(doctor);
    }

    public List<Appointment> findFutureByPatient(Patient patient) {
        return appointmentRepository.findByPatient(patient).stream()
                .filter(a -> !a.getAppointmentDate().isBefore(LocalDate.now()))
                .filter(a -> a.getStatus() != AppointmentStatus.COMPLETED)
                .toList();
    }

    public List<Appointment> findPastByPatient(Patient patient) {
        return appointmentRepository.findByPatient(patient).stream()
                .filter(a -> a.getAppointmentDate().isBefore(LocalDate.now()) ||
                             a.getStatus() == AppointmentStatus.COMPLETED)
                .toList();
    }
}
