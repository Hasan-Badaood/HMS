package model;
import java.time.LocalDate;

public class Prescription {
    private String prescriptionId;
    private String patientId;
    private String clinicianId;
    private String appointmentId;
    private LocalDate prescriptionDate;
    private String medicationName;
    private String dosage;
    private String frequency;
    private int durationDays;
    private String quantity;
    private String instructions;
    private String pharmacyName;
    private String status;
    private LocalDate issueDate;
    private LocalDate collectionDate;

    public Prescription(String prescriptionId, String patientId, String clinicianId,
                        String appointmentId, LocalDate prescriptionDate, String medicationName,
                        String dosage, String frequency, int durationDays, String quantity,
                        String instructions, String pharmacyName, String status,
                        LocalDate issueDate, LocalDate collectionDate) {
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
        this.clinicianId = clinicianId;
        this.appointmentId = appointmentId;
        this.prescriptionDate = prescriptionDate;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.quantity = quantity;
        this.instructions = instructions;
        this.pharmacyName = pharmacyName;
        this.status = status;
        this.issueDate = issueDate;
        this.collectionDate = collectionDate;
    }

    // Getters and Setters
    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String id) { this.prescriptionId = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String id) { this.patientId = id; }

    public String getClinicianId() { return clinicianId; }
    public void setClinicianId(String id) { this.clinicianId = id; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String id) { this.appointmentId = id; }

    public LocalDate getPrescriptionDate() { return prescriptionDate; }
    public void setPrescriptionDate(LocalDate date) { this.prescriptionDate = date; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String name) { this.medicationName = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int days) { this.durationDays = days; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getPharmacyName() { return pharmacyName; }
    public void setPharmacyName(String name) { this.pharmacyName = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate date) { this.issueDate = date; }

    public LocalDate getCollectionDate() { return collectionDate; }
    public void setCollectionDate(LocalDate date) { this.collectionDate = date; }


    public String generateEPrescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("=============== E-PRESCRIPTION ===============\n");
        sb.append("Prescription ID: ").append(prescriptionId).append("\n");
        sb.append("Patient ID: ").append(patientId).append("\n");
        sb.append("Clinician ID: ").append(clinicianId).append("\n");
        sb.append("Date Issued: ").append(prescriptionDate).append("\n");
        sb.append("-------------------------------------------\n");
        sb.append("Medication: ").append(medicationName).append("\n");
        sb.append("Dosage: ").append(dosage).append("\n");
        sb.append("Frequency: ").append(frequency).append("\n");
        sb.append("Duration: ").append(durationDays).append(" days\n");
        sb.append("Quantity: ").append(quantity).append("\n");
        sb.append("Instructions: ").append(instructions).append("\n");
        sb.append("Pharmacy: ").append(pharmacyName).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("==============================================\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return medicationName + " - " + dosage + " (" + status + ")";
    }
}