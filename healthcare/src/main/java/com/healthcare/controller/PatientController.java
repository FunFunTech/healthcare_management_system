package com.healthcare.controller;

import com.healthcare.model.*;
import com.healthcare.service.*;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired private UserService userService;
    @Autowired private PatientService patientService;
    @Autowired private DoctorService doctorService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private MedicalRecordService medicalRecordService;
    @Autowired private PdfService pdfService;

    private Patient getCurrentPatient(Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        return patientService.findByUser(user).orElseThrow();
    }

    // ─── Dashboard ────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        Patient patient = getCurrentPatient(principal);
        model.addAttribute("patient", patient);

        List<Appointment> future = appointmentService.findFutureByPatient(patient);
        List<Appointment> past = appointmentService.findPastByPatient(patient);
        List<MedicalRecord> records = medicalRecordService.findByPatient(patient);
        List<Medication> activeMeds = medicalRecordService.getActiveMedicationsByPatient(patient);

        model.addAttribute("futureAppointments", future);
        model.addAttribute("totalAppointments", future.size() + past.size());
        model.addAttribute("totalRecords", records.size());
        model.addAttribute("activeMedications", activeMeds.size());
        model.addAttribute("upcomingAppointments", future.stream().limit(3).toList());
        model.addAttribute("recentRecords", records.stream().limit(3).toList());
        model.addAttribute("currentMeds", activeMeds);
        return "patient/dashboard";
    }

    // ─── Appointments ─────────────────────────────────────────────
    @GetMapping("/appointments")
    public String appointments(Model model, Principal principal) {
        Patient patient = getCurrentPatient(principal);
        List<Appointment> future = appointmentService.findFutureByPatient(patient);
        List<Appointment> past = appointmentService.findPastByPatient(patient);
        model.addAttribute("futureAppointments", future);
        model.addAttribute("pastAppointments", past);
        model.addAttribute("patient", patient);
        return "patient/appointments";
    }

    @GetMapping("/appointments/book")
    public String bookAppointmentForm(Model model) {
        model.addAttribute("doctors", doctorService.findAll());
        return "patient/book-appointment";
    }

    @PostMapping("/appointments/book")
    public String bookAppointment(@RequestParam Long doctorId,
                                   @RequestParam String appointmentDate,
                                   @RequestParam(required = false) String appointmentTime,
                                   @RequestParam(required = false) String symptoms,
                                   Principal principal, RedirectAttributes ra) {
        Patient patient = getCurrentPatient(principal);
        Doctor doctor = doctorService.findById(doctorId).orElseThrow();

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(LocalDate.parse(appointmentDate));
        appointment.setSymptoms(symptoms);
        appointment.setStatus(AppointmentStatus.PENDING);
        if (appointmentTime != null && !appointmentTime.isEmpty()) {
            appointment.setAppointmentTime(java.time.LocalTime.parse(appointmentTime));
        }

        appointmentService.save(appointment);
        ra.addFlashAttribute("success", "Appointment booked! Awaiting approval.");
        return "redirect:/patient/appointments";
    }

    // ─── Records ──────────────────────────────────────────────────
    @GetMapping("/records")
    public String records(Model model, Principal principal) {
        Patient patient = getCurrentPatient(principal);
        List<MedicalRecord> records = medicalRecordService.findByPatient(patient);
        model.addAttribute("records", records);
        model.addAttribute("patient", patient);
        return "patient/records";
    }

    @GetMapping("/records/download")
    public ResponseEntity<byte[]> downloadRecord(Principal principal) throws DocumentException {
        Patient patient = getCurrentPatient(principal);
        byte[] pdf = pdfService.generatePatientReport(patient);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "my_medical_report.pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    // ─── Medications ──────────────────────────────────────────────
    @GetMapping("/medications")
    public String medications(Model model, Principal principal) {
        Patient patient = getCurrentPatient(principal);
        List<Medication> allMeds = medicalRecordService.getMedicationsByPatient(patient);
        List<Medication> activeMeds = medicalRecordService.getActiveMedicationsByPatient(patient);
        model.addAttribute("allMedications", allMeds);
        model.addAttribute("activeMedications", activeMeds);
        model.addAttribute("patient", patient);
        return "patient/medications";
    }

    // ─── Profile ──────────────────────────────────────────────────
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        Patient patient = getCurrentPatient(principal);
        model.addAttribute("patient", patient);
        return "patient/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam(required = false) String gender,
                                @RequestParam(required = false) String bloodGroup,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) String emergencyContact,
                                @RequestParam(required = false) String allergies,
                                @RequestParam(required = false) String phone,
                                Principal principal, RedirectAttributes ra) {
        Patient patient = getCurrentPatient(principal);
        patient.setGender(gender);
        patient.setBloodGroup(bloodGroup);
        patient.setAddress(address);
        patient.setEmergencyContact(emergencyContact);
        patient.setAllergies(allergies);
        patient.getUser().setPhone(phone);
        userService.updateUser(patient.getUser());
        patientService.save(patient);
        ra.addFlashAttribute("success", "Profile updated!");
        return "redirect:/patient/profile";
    }
}
