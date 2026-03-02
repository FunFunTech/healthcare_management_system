package com.healthcare.service;

import com.healthcare.model.MedicalRecord;
import com.healthcare.model.Medication;
import com.healthcare.model.Patient;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    @Autowired private MedicalRecordService medicalRecordService;

    private static final BaseColor TEAL = new BaseColor(44, 160, 144);
    private static final BaseColor LIGHT_GRAY = new BaseColor(248, 249, 250);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public byte[] generatePatientReport(Patient patient) throws DocumentException {
        Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        // Fonts
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.WHITE);
        Font headingFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, TEAL);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.DARK_GRAY);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.GRAY);

        // Header Banner
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell(new Phrase("Healthcare Management System", titleFont));
        headerCell.setBackgroundColor(TEAL);
        headerCell.setPadding(15);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setBorder(Rectangle.NO_BORDER);
        header.addCell(headerCell);
        doc.add(header);

        doc.add(new Paragraph(" "));

        // Report Title
        Paragraph reportTitle = new Paragraph("Patient Medical Report", headingFont);
        reportTitle.setAlignment(Element.ALIGN_CENTER);
        doc.add(reportTitle);

        Paragraph generatedOn = new Paragraph("Generated on: " +
                java.time.LocalDate.now().format(DATE_FMT), smallFont);
        generatedOn.setAlignment(Element.ALIGN_CENTER);
        doc.add(generatedOn);
        doc.add(new Paragraph(" "));

        // Patient Info
        addSectionTitle(doc, "Patient Information", headingFont);
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(5);
        addInfoRow(infoTable, "Patient ID:", "P-" + patient.getId(), boldFont, normalFont);
        addInfoRow(infoTable, "Full Name:", patient.getUser().getName(), boldFont, normalFont);
        addInfoRow(infoTable, "Email:", patient.getUser().getEmail(), boldFont, normalFont);
        addInfoRow(infoTable, "Phone:", patient.getUser().getPhone() != null ? patient.getUser().getPhone() : "N/A", boldFont, normalFont);
        addInfoRow(infoTable, "Gender:", patient.getGender() != null ? patient.getGender() : "N/A", boldFont, normalFont);
        addInfoRow(infoTable, "Blood Group:", patient.getBloodGroup() != null ? patient.getBloodGroup() : "N/A", boldFont, normalFont);
        addInfoRow(infoTable, "DOB:", patient.getDateOfBirth() != null ? patient.getDateOfBirth().format(DATE_FMT) : "N/A", boldFont, normalFont);
        addInfoRow(infoTable, "Allergies:", patient.getAllergies() != null ? patient.getAllergies() : "None", boldFont, normalFont);
        doc.add(infoTable);
        doc.add(new Paragraph(" "));

        // Medical Records
        List<MedicalRecord> records = medicalRecordService.findByPatient(patient);
        addSectionTitle(doc, "Medical Records", headingFont);

        if (records.isEmpty()) {
            doc.add(new Paragraph("No medical records found.", normalFont));
        } else {
            for (MedicalRecord record : records) {
                // Record header
                PdfPTable recHeader = new PdfPTable(1);
                recHeader.setWidthPercentage(100);
                PdfPCell recCell = new PdfPCell(new Phrase(
                        "Record Date: " + record.getRecordDate().format(DATE_FMT) +
                        "   |   Doctor: Dr. " + record.getDoctor().getUser().getName(), boldFont));
                recCell.setBackgroundColor(LIGHT_GRAY);
                recCell.setPadding(8);
                recCell.setBorderColor(TEAL);
                recHeader.addCell(recCell);
                doc.add(recHeader);

                PdfPTable recTable = new PdfPTable(2);
                recTable.setWidthPercentage(100);
                addInfoRow(recTable, "Blood Pressure:", record.getBloodPressure() != null ? record.getBloodPressure() : "N/A", boldFont, normalFont);
                addInfoRow(recTable, "Sugar Level:", record.getSugarLevel() != null ? record.getSugarLevel() : "N/A", boldFont, normalFont);
                addInfoRow(recTable, "Weight:", record.getWeight() != null ? record.getWeight() : "N/A", boldFont, normalFont);
                addInfoRow(recTable, "Temperature:", record.getTemperature() != null ? record.getTemperature() : "N/A", boldFont, normalFont);
                addInfoRow(recTable, "Heart Rate:", record.getHeartRate() != null ? record.getHeartRate() : "N/A", boldFont, normalFont);
                doc.add(recTable);

                if (record.getDiagnosis() != null && !record.getDiagnosis().isEmpty()) {
                    doc.add(new Paragraph("Diagnosis: " + record.getDiagnosis(), normalFont));
                }
                if (record.getTests() != null && !record.getTests().isEmpty()) {
                    doc.add(new Paragraph("Tests: " + record.getTests(), normalFont));
                }
                if (record.getNotes() != null && !record.getNotes().isEmpty()) {
                    doc.add(new Paragraph("Notes: " + record.getNotes(), normalFont));
                }

                // Medications for this record
                List<Medication> meds = medicalRecordService.getMedicationsByRecord(record);
                if (!meds.isEmpty()) {
                    doc.add(new Paragraph("Prescribed Medications:", boldFont));
                    PdfPTable medTable = new PdfPTable(4);
                    medTable.setWidthPercentage(100);
                    medTable.setWidths(new float[]{3, 2, 2, 3});
                    String[] headers = {"Medicine", "Dosage", "Frequency", "Duration"};
                    Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
                    for (String h : headers) {
                        PdfPCell hCell = new PdfPCell(new Phrase(h, headerFont));
                        hCell.setBackgroundColor(TEAL);
                        hCell.setPadding(5);
                        medTable.addCell(hCell);
                    }
                    for (Medication med : meds) {
                        medTable.addCell(new PdfPCell(new Phrase(med.getMedicineName(), normalFont)));
                        medTable.addCell(new PdfPCell(new Phrase(med.getDosage() != null ? med.getDosage() : "", normalFont)));
                        medTable.addCell(new PdfPCell(new Phrase(med.getFrequency() != null ? med.getFrequency() : "", normalFont)));
                        medTable.addCell(new PdfPCell(new Phrase(med.getDuration() != null ? med.getDuration() : "", normalFont)));
                    }
                    doc.add(medTable);
                }
                doc.add(new Paragraph(" "));
            }
        }

        // Footer
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph("© 2026 Healthcare Management System - Confidential Medical Document", smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return out.toByteArray();
    }

    private void addSectionTitle(Document doc, String title, Font font) throws DocumentException {
        Paragraph p = new Paragraph(title, font);
        p.setSpacingBefore(10);
        p.setSpacingAfter(5);
        doc.add(p);

        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBorderWidthBottom(1.5f);
        lineCell.setBorderColorBottom(TEAL);
        lineCell.setBorderWidthTop(0);
        lineCell.setBorderWidthLeft(0);
        lineCell.setBorderWidthRight(0);
        lineCell.setPaddingBottom(2);
        line.addCell(lineCell);
        doc.add(line);
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }
}
