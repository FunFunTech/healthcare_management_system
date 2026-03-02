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
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired private UserService userService;
    @Autowired private DoctorService doctorService;
    @Autowired private PatientService patientService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private MedicalRecordService medicalRecordService;
    @Autowired private PdfService pdfService;

    private Doctor getCurrentDoctor(Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        return doctorService.findByUser(user).orElseThrow();
    }

    // ─── Dashboard ────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        Doctor doctor = getCurrentDoctor(principal);
        model.addAttribute("doctor", doctor);
        model.addAttribute("todayCount", appointmentService.countTodayByDoctor(doctor));
        model.addAttribute("totalPatients", appointmentService.countTotalByDoctor(doctor));

        List<Appointment> todayApps = appointmentService.findTodaysByDoctor(doctor);
        model.addAttribute("todayAppointments", todayApps);

        // Chart data
        List<Object[]> chartData = appointmentService.getAppointmentChartData(doctor);
        StringBuilder chartLabels = new StringBuilder("[");
        StringBuilder chartValues = new StringBuilder("[");
        for (int i = 0; i < chartData.size(); i++) {
            Object[] row = chartData.get(i);
            chartLabels.append("'").append(row[0].toString()).append("'");
            chartValues.append(row[1].toString());
            if (i < chartData.size() - 1) { chartLabels.append(","); chartValues.append(","); }
        }
        chartLabels.append("]"); chartValues.append("]");
        model.addAttribute("chartLabels", chartLabels.toString());
        model.addAttribute("chartValues", chartValues.toString());
        model.addAttribute("offDays", doctor.getOffDays());
        return "doctor/dashboard";
    }

    @PostMapping("/off-days/add")
    public String addOffDay(@RequestParam String date, Principal principal, RedirectAttributes ra) {
        Doctor doctor = getCurrentDoctor(principal);
        doctorService.addOffDay(doctor, date);
        ra.addFlashAttribute("success", "Off day marked!");
        return "redirect:/doctor/dashboard";
    }

    @PostMapping("/off-days/remove")
    public String removeOffDay(@RequestParam String date, Principal principal, RedirectAttributes ra) {
        Doctor doctor = getCurrentDoctor(principal);
        doctorService.removeOffDay(doctor, date);
        ra.addFlashAttribute("success", "Off day removed!");
        return "redirect:/doctor/dashboard";
    }

    // ─── Appointments ─────────────────────────────────────────────
    @GetMapping("/appointments")
    public String appointments(@RequestParam(required = false) String search,
                               @RequestParam(required = false) String date,
                               Model model, Principal principal) {
        Doctor doctor = getCurrentDoctor(principal);
        List<Appointment> appointments = appointmentService.searchAppointmentsByDoctor(doctor, search);

        if (date != null && !date.isEmpty()) {
            LocalDate filterDate = LocalDate.parse(date);
            appointments = appointments.stream()
                    .filter(a -> a.getAppointmentDate().equals(filterDate)).toList();
        }

        model.addAttribute("appointments", appointments);
        model.addAttribute("search", search);
        model.addAttribute("date", date);
        model.addAttribute("patients", patientService.findAll());
        return "doctor/appointments";
    }

    @GetMapping("/appointments/create")
    public String createAppointmentForm(Model model, Principal principal) {
        model.addAttribute("patients", patientService.findAll());
        model.addAttribute("doctor", getCurrentDoctor(principal));
        return "doctor/appointment-form";
    }

    @PostMapping("/appointments/create")
    public String createAppointment(@RequestParam Long patientId,
                                     @RequestParam String appointmentDate,
                                     @RequestParam(required = false) String appointmentTime,
                                     @RequestParam(required = false) String symptoms,
                                     @RequestParam(required = false) String notes,
                                     Principal principal, RedirectAttributes ra) {
        Doctor doctor = getCurrentDoctor(principal);
        Patient patient = patientService.findById(patientId).orElseThrow();

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(LocalDate.parse(appointmentDate));
        appointment.setSymptoms(symptoms);
        appointment.setNotes(notes);
        appointment.setStatus(AppointmentStatus.PENDING); // Doctor can't approve

        if (appointmentTime != null && !appointmentTime.isEmpty()) {
            appointment.setAppointmentTime(java.time.LocalTime.parse(appointmentTime));
        }

        appointmentService.save(appointment);
        ra.addFlashAttribute("success", "Appointment created! Awaiting admin approval.");
        return "redirect:/doctor/appointments";
    }

    // ─── Records ──────────────────────────────────────────────────
    @GetMapping("/records")
    public String records(@RequestParam(required = false) String search,
                          Model model, Principal principal) {
        Doctor doctor = getCurrentDoctor(principal);
        List<MedicalRecord> records = medicalRecordService.searchByDoctorAndPatient(doctor, search);
        model.addAttribute("records", records);
        model.addAttribute("search", search);
        model.addAttribute("patients", patientService.findAll());
        return "doctor/records";
    }

    @GetMapping("/records/create")
    public String createRecordForm(Model model, Principal principal) {
        Doctor doctor = getCurrentDoctor(principal);
        // Only patients that have appointments with this doctor
        List<Appointment> myApps = appointmentService.findByDoctor(doctor);
        List<Patient> myPatients = myApps.stream().map(Appointment::getPatient).distinct().toList();
        model.addAttribute("patients", myPatients);
        model.addAttribute("appointments", myApps);
        return "doctor/record-form";
    }

    @PostMapping("/records/create")
    public String createRecord(@RequestParam Long patientId,
                               @RequestParam(required = false) Long appointmentId,
                               @RequestParam(required = false) String bloodPressure,
                               @RequestParam(required = false) String sugarLevel,
                               @RequestParam(required = false) String weight,
                               @RequestParam(required = false) String temperature,
                               @RequestParam(required = false) String heartRate,
                               @RequestParam(required = false) String tests,
                               @RequestParam(required = false) String diagnosis,
                               @RequestParam(required = false) String notes,
                               Principal principal, RedirectAttributes ra) {
        Doctor doctor = getCurrentDoctor(principal);
        Patient patient = patientService.findById(patientId).orElseThrow();

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setBloodPressure(bloodPressure);
        record.setSugarLevel(sugarLevel);
        record.setWeight(weight);
        record.setTemperature(temperature);
        record.setHeartRate(heartRate);
        record.setTests(tests);
        record.setDiagnosis(diagnosis);
        record.setNotes(notes);

        if (appointmentId != null) {
            appointmentService.findById(appointmentId).ifPresent(record::setAppointment);
        }

        medicalRecordService.save(record);
        ra.addFlashAttribute("success", "Medical record saved!");
        return "redirect:/doctor/records";
    }

    @GetMapping("/records/{patientId}/download")
    public ResponseEntity<byte[]> downloadRecord(@PathVariable Long patientId) throws DocumentException {
        Patient patient = patientService.findById(patientId).orElseThrow();
        byte[] pdf = pdfService.generatePatientReport(patient);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "record_" + patient.getUser().getName().replaceAll(" ", "_") + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    // ─── Medication ───────────────────────────────────────────────
    @GetMapping("/medications")
    public String medications(Model model, Principal principal) {
        Doctor doctor = getCurrentDoctor(principal);
        List<Medication> medications = medicalRecordService.getMedicationsByDoctor(doctor);
        model.addAttribute("medications", medications);

        List<Appointment> myApps = appointmentService.findByDoctor(doctor);
        List<Patient> myPatients = myApps.stream().map(Appointment::getPatient).distinct().toList();
        model.addAttribute("patients", myPatients);

        List<MedicalRecord> myRecords = medicalRecordService.findByDoctor(doctor);
        model.addAttribute("records", myRecords);
        return "doctor/medications";
    }

    @PostMapping("/medications/prescribe")
    public String prescribeMedication(@RequestParam Long patientId,
                                       @RequestParam(required = false) Long recordId,
                                       @RequestParam String medicineName,
                                       @RequestParam(required = false) String dosage,
                                       @RequestParam(required = false) String frequency,
                                       @RequestParam(required = false) String duration,
                                       @RequestParam(required = false) String instructions,
                                       @RequestParam(required = false) String endDate,
                                       Principal principal, RedirectAttributes ra) {
        Doctor doctor = getCurrentDoctor(principal);
        Patient patient = patientService.findById(patientId).orElseThrow();

        Medication med = new Medication();
        med.setPatient(patient);
        med.setDoctor(doctor);
        med.setMedicineName(medicineName);
        med.setDosage(dosage);
        med.setFrequency(frequency);
        med.setDuration(duration);
        med.setInstructions(instructions);
        if (endDate != null && !endDate.isEmpty()) med.setEndDate(LocalDate.parse(endDate));

        if (recordId != null) {
            medicalRecordService.findById(recordId).ifPresent(med::setMedicalRecord);
        }

        medicalRecordService.saveMedication(med);
        ra.addFlashAttribute("success", "Medication prescribed successfully!");
        return "redirect:/doctor/medications";
    }

    @PostMapping("/medications/{id}/deactivate")
    public String deactivateMedication(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        medicalRecordService.findMedicationById(id).ifPresent(m -> {
            m.setActive(false);
            medicalRecordService.saveMedication(m);
        });
        ra.addFlashAttribute("success", "Medication deactivated.");
        return "redirect:/doctor/medications";
    }
}
