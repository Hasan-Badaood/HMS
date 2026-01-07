package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PatientRecord {
    private String recordId;
    private Patient patient;
    private List<String> allergies;
    private List<String> conditions;
    private String bloodType;
    private List<String> medications;
    private LocalDateTime lastUpdated;

    public PatientRecord(Patient patient) {
        this.recordId = java.util.UUID.randomUUID().toString();
        this.patient = patient;
        this.allergies = new ArrayList<>();
        this.conditions = new ArrayList<>();
        this.medications = new ArrayList<>();
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public String getRecordId() { return recordId; }
    public Patient getPatient() { return patient; }

    public List<String> getAllergies() { return allergies; }
    public void addAllergy(String allergy) {
        this.allergies.add(allergy);
        updateLastModified();
    }

    public List<String> getConditions() { return conditions; }
    public void addCondition(String condition) {
        this.conditions.add(condition);
        updateLastModified();
    }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
        updateLastModified();
    }

    public List<String> getMedications() { return medications; }
    public void updateMedications(List<String> medications) {
        this.medications = medications;
        updateLastModified();
    }


    private void updateLastModified() {
        this.lastUpdated = LocalDateTime.now();
    }

    public String getFullRecord() {
        StringBuilder sb = new StringBuilder();
        sb.append("Patient Record for: ").append(patient.getFullName()).append("\n");
        sb.append("NHS Number: ").append(patient.getNhsNumber()).append("\n");
        sb.append("Blood Type: ").append(bloodType != null ? bloodType : "N/A").append("\n");
        sb.append("Allergies: ").append(allergies.isEmpty() ? "None" : String.join(", ", allergies)).append("\n");
        sb.append("Conditions: ").append(conditions.isEmpty() ? "None" : String.join(", ", conditions)).append("\n");
        sb.append("Current Medications: ").append(medications.isEmpty() ? "None" : String.join(", ", medications)).append("\n");
        return sb.toString();
    }
}