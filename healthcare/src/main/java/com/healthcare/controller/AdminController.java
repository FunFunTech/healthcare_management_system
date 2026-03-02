package com.healthcare.controller;

import com.healthcare.model.*;
import com.healthcare.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserService userService;
    @Autowired private DoctorService doctorService;
    @Autowired private PatientService patientService;
    @Autowired private AppointmentService appointmentService;
   

    // ─── Dashboard ────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("admin", userService.getCurrentUser(principal.getName()));
        model.addAttribute("totalDoctors", doctorService.count());
        model.addAttribute("totalPatients", patientService.count());
        model.addAttribute("totalAppointments", appointmentService.countToday());

        List<Appointment> todayApps = appointmentService.findTodaysAppointments();
        model.addAttribute("todayAppointments", todayApps);
        return "admin/dashboard";
    }

    // ─── Appointments ─────────────────────────────────────────────
    @GetMapping("/appointments")
    public String appointments(@RequestParam(required = false) String search,
                               @RequestParam(required = false) String date,
                               @RequestParam(required = false) Long doctorId,
                               @RequestParam(required = false) Long patientId,
                               Model model) {
        List<Appointment> appointments = appointmentService.filterAppointments(search, date, doctorId, patientId);
        model.addAttribute("appointments", appointments);
        model.addAttribute("doctors", doctorService.findAll());
        model.addAttribute("patients", patientService.findAll());
        model.addAttribute("search", search);
        model.addAttribute("date", date);
        return "admin/appointments";
    }

    @GetMapping("/appointments/create")
    public String createAppointmentForm(Model model) {
        model.addAttribute("doctors", doctorService.findAll());
        model.addAttribute("patients", patientService.findAll());
        return "admin/appointment-form";
    }

    @PostMapping("/appointments/create")
    public String createAppointment(@RequestParam Long patientId,
                                     @RequestParam Long doctorId,
                                     @RequestParam String appointmentDate,
                                     @RequestParam(required = false) String appointmentTime,
                                     @RequestParam(required = false) String symptoms,
                                     @RequestParam(required = false) String notes,
                                     RedirectAttributes ra) {
        Patient patient = patientService.findById(patientId).orElseThrow();
        Doctor doctor = doctorService.findById(doctorId).orElseThrow();

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(LocalDate.parse(appointmentDate));
        appointment.setSymptoms(symptoms);
        appointment.setNotes(notes);
        appointment.setStatus(AppointmentStatus.APPROVED); // Admin-created = auto approved

        if (appointmentTime != null && !appointmentTime.isEmpty()) {
            appointment.setAppointmentTime(java.time.LocalTime.parse(appointmentTime));
        }

        appointmentService.save(appointment);
        ra.addFlashAttribute("success", "Appointment created successfully!");
        return "redirect:/admin/appointments";
    }

    @PostMapping("/appointments/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes ra) {
        appointmentService.updateStatus(id, AppointmentStatus.valueOf(status));
        ra.addFlashAttribute("success", "Appointment status updated!");
        return "redirect:/admin/appointments";
    }

    @PostMapping("/appointments/{id}/delete")
    public String deleteAppointment(@PathVariable Long id, RedirectAttributes ra) {
        appointmentService.delete(id);
        ra.addFlashAttribute("success", "Appointment deleted!");
        return "redirect:/admin/appointments";
    }

    // ─── Doctors ──────────────────────────────────────────────────
    @GetMapping("/doctors")
    public String doctors(@RequestParam(required = false) String search, Model model) {
        List<Doctor> doctors = doctorService.search(search);
        model.addAttribute("doctors", doctors);
        model.addAttribute("search", search);
        return "admin/doctors";
    }

    @GetMapping("/doctors/create")
    public String createDoctorForm(Model model) {
        return "admin/doctor-form";
    }

    @PostMapping("/doctors/create")
    public String createDoctor(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String phone,
                               @RequestParam String password,
                               @RequestParam(required = false) String department,
                               @RequestParam(required = false) String specialization,
                               @RequestParam(required = false) String qualification,
                               @RequestParam(required = false) String experience,
                               RedirectAttributes ra) {
        if (userService.emailExists(email)) {
            ra.addFlashAttribute("error", "Email already exists!");
            return "redirect:/admin/doctors/create";
        }
        User user = new User();
        user.setName(name); user.setEmail(email); user.setPhone(phone);
        user.setPassword(password); user.setRole(Role.DOCTOR);
        User saved = userService.registerUser(user);

        doctorService.findByUser(saved).ifPresent(d -> {
            d.setDepartment(department);
            d.setSpecialization(specialization);
            d.setQualification(qualification);
            d.setExperience(experience);
            doctorService.save(d);
        });

        ra.addFlashAttribute("success", "Doctor added successfully!");
        return "redirect:/admin/doctors";
    }

    @GetMapping("/doctors/{id}/edit")
    public String editDoctorForm(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.findById(id).orElseThrow();
        model.addAttribute("doctor", doctor);
        return "admin/doctor-edit";
    }

    @PostMapping("/doctors/{id}/edit")
    public String editDoctor(@PathVariable Long id,
                             @RequestParam String name,
                             @RequestParam String phone,
                             @RequestParam(required = false) String department,
                             @RequestParam(required = false) String specialization,
                             @RequestParam(required = false) String qualification,
                             @RequestParam(required = false) String experience,
                             RedirectAttributes ra) {
        Doctor doctor = doctorService.findById(id).orElseThrow();
        doctor.getUser().setName(name);
        doctor.getUser().setPhone(phone);
        doctor.setDepartment(department);
        doctor.setSpecialization(specialization);
        doctor.setQualification(qualification);
        doctor.setExperience(experience);
        userService.updateUser(doctor.getUser());
        doctorService.save(doctor);
        ra.addFlashAttribute("success", "Doctor updated!");
        return "redirect:/admin/doctors";
    }

    @PostMapping("/doctors/{id}/delete")
    public String deleteDoctor(@PathVariable Long id, RedirectAttributes ra) {
        doctorService.delete(id);
        ra.addFlashAttribute("success", "Doctor deleted!");
        return "redirect:/admin/doctors";
    }

    // ─── Patients ─────────────────────────────────────────────────
    @GetMapping("/patients")
    public String patients(@RequestParam(required = false) String search, Model model) {
        List<Patient> patients = patientService.search(search);
        model.addAttribute("patients", patients);
        model.addAttribute("search", search);
        return "admin/patients";
    }

    @GetMapping("/patients/create")
    public String createPatientForm(Model model) {
        return "admin/patient-form";
    }

    @PostMapping("/patients/create")
    public String createPatient(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam String password,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String bloodGroup,
                                RedirectAttributes ra) {
        if (userService.emailExists(email)) {
            ra.addFlashAttribute("error", "Email already exists!");
            return "redirect:/admin/patients/create";
        }
        User user = new User();
        user.setName(name); user.setEmail(email); user.setPhone(phone);
        user.setPassword(password); user.setRole(Role.PATIENT);
        User saved = userService.registerUser(user);

        patientService.findByUser(saved).ifPresent(p -> {
            p.setGender(gender);
            p.setBloodGroup(bloodGroup);
            patientService.save(p);
        });

        ra.addFlashAttribute("success", "Patient added successfully!");
        return "redirect:/admin/patients";
    }

    @PostMapping("/patients/{id}/delete")
    public String deletePatient(@PathVariable Long id, RedirectAttributes ra) {
        patientService.delete(id);
        ra.addFlashAttribute("success", "Patient deleted!");
        return "redirect:/admin/patients";
    }
}
