package controller;

import model.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PrescriptionController {

    private List<Prescription> prescriptions;
    private PatientController patCtrl;
    private ClinicianController clinCtrl;
    private String dataFilename;
    private static final String OUTPUT_FILE = "output_prescriptions.txt";

    public PrescriptionController(PatientController pc, ClinicianController cc) {
        prescriptions = new ArrayList<>();
        patCtrl = pc;
        clinCtrl = cc;
    }

    public void loadPrescriptions(String filename) {
        dataFilename = filename;
        prescriptions = DataLoader.loadPrescriptions(filename);

        // Link prescriptions to their patients
        for (Prescription rx : prescriptions) {
            Patient p = patCtrl.getPatientById(rx.getPatientId());
            if (p != null) {
                p.addPrescription(rx);
            }
        }
    }

    private void saveToFile() {
        if (dataFilename != null) {
            DataLoader.savePrescriptions(dataFilename, prescriptions);
        }
    }

    public List<Prescription> getAllPrescriptions() {
        return new ArrayList<>(prescriptions);
    }

    public Prescription getPrescriptionById(String id) {
        for (Prescription p : prescriptions) {
            if (p.getPrescriptionId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public List<Prescription> getPrescriptionsByPatient(String patientId) {
        List<Prescription> patientPrescriptions = new ArrayList<>();

        for (Prescription rx : prescriptions) {
            if (rx.getPatientId().equals(patientId)) {
                patientPrescriptions.add(rx);
            }
        }

        // Sort by date - most recent first
        patientPrescriptions.sort((p1, p2) ->
                p2.getPrescriptionDate().compareTo(p1.getPrescriptionDate())
        );

        return patientPrescriptions;
    }

    public List<Prescription> getPrescriptionsByClinician(String clinicianId) {
        List<Prescription> clinicianPrescriptions = new ArrayList<>();

        for (Prescription rx : prescriptions) {
            if (rx.getClinicianId().equals(clinicianId)) {
                clinicianPrescriptions.add(rx);
            }
        }

        clinicianPrescriptions.sort((p1, p2) ->
                p2.getPrescriptionDate().compareTo(p1.getPrescriptionDate())
        );

        return clinicianPrescriptions;
    }

    public void addPrescription(Prescription rx) {
        prescriptions.add(rx);

        // Link to patient record
        Patient p = patCtrl.getPatientById(rx.getPatientId());
        if (p != null) {
            p.addPrescription(rx);
        }

        // Generate prescription document
        generateDoc(rx);

        saveToFile();
    }

    public boolean updatePrescription(Prescription rx) {
        for (int i = 0; i < prescriptions.size(); i++) {
            if (prescriptions.get(i).getPrescriptionId().equals(rx.getPrescriptionId())) {
                prescriptions.set(i, rx);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public boolean deletePrescription(String id) {
        for (int i = 0; i < prescriptions.size(); i++) {
            if (prescriptions.get(i).getPrescriptionId().equals(id)) {
                prescriptions.remove(i);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    // Generate a formatted prescription document
    private void generateDoc(Prescription rx) {
        Patient pat = patCtrl.getPatientById(rx.getPatientId());
        Clinician clin = clinCtrl.getClinicianById(rx.getClinicianId());

        if (pat == null || clin == null) return;

        StringBuilder doc = new StringBuilder();

        doc.append("=====================================\n");
        doc.append("  NHS ELECTRONIC PRESCRIPTION\n");
        doc.append("=====================================\n\n");

        doc.append(">> PRESCRIPTION DETAILS\n");
        doc.append("ID: ").append(rx.getPrescriptionId()).append("\n");
        doc.append("Date: ").append(rx.getPrescriptionDate()).append("\n");
        doc.append("Status: ").append(rx.getStatus()).append("\n\n");

        doc.append(">> PATIENT\n");
        doc.append("Name: ").append(pat.getFullName()).append("\n");
        doc.append("NHS#: ").append(pat.getNhsNumber()).append("\n");
        doc.append("DOB: ").append(pat.getDateOfBirth()).append("\n");
        doc.append("Address: ").append(pat.getAddress()).append(", ")
                .append(pat.getPostcode()).append("\n\n");

        doc.append(">> PRESCRIBER\n");
        doc.append("Dr. ").append(clin.getFullName()).append("\n");
        doc.append("License: ").append(clin.getLicenseNumber()).append("\n");
        doc.append("Specialty: ").append(clin.getSpecialization()).append("\n");
        doc.append("Contact: ").append(clin.getEmail()).append("\n\n");

        doc.append(">> MEDICATION\n");
        doc.append("Name: ").append(rx.getMedicationName()).append("\n");
        doc.append("Dosage: ").append(rx.getDosage()).append("\n");
        doc.append("Frequency: ").append(rx.getFrequency()).append("\n");
        doc.append("Duration: ").append(rx.getDurationDays()).append(" days\n");
        doc.append("Quantity: ").append(rx.getQuantity()).append("\n");
        doc.append("Instructions: ").append(rx.getInstructions()).append("\n\n");

        doc.append(">> PHARMACY\n");
        doc.append("Name: ").append(rx.getPharmacyName()).append("\n");

        String collectionStatus = rx.getCollectionDate() != null ?
                rx.getCollectionDate().toString() : "Not collected";
        doc.append("Collection: ").append(collectionStatus).append("\n\n");

        doc.append(">> ALLERGIES & WARNINGS\n");
        PatientRecord rec = pat.getPatientRecord();
        if (!rec.getAllergies().isEmpty()) {
            doc.append("⚠ ALLERGIES: ").append(String.join(", ", rec.getAllergies())).append("\n");
        } else {
            doc.append("No known allergies\n");
        }
        doc.append("\n");

        doc.append("=====================================\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        doc.append("Generated: ").append(LocalDateTime.now().format(formatter)).append("\n");
        doc.append("Valid for 28 days\n");
        doc.append("=====================================\n\n\n");

        // Write to output file
        writeToFile(OUTPUT_FILE, doc.toString());
    }

    private void writeToFile(String filename, String content) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(filename, true)))) {
            out.print(content);
        } catch (IOException e) {
            System.err.println("Error writing to " + filename + ": " + e.getMessage());
        }
    }

    public int getPrescriptionCount() {
        return prescriptions.size();
    }
}