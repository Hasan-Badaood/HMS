package model;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Appointment {
    private String appointmentId;
    private String patientId;
    private String clinicianId;
    private String facilityId;
    private LocalDate appointmentDate;
    private String appointmentTime;
    private int durationMinutes;
    private String appointmentType;
    private String status;
    private String reasonForVisit;
    private String notes;
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;

    public Appointment(String appointmentId, String patientId, String clinicianId,
                       String facilityId, LocalDate appointmentDate, String appointmentTime,
                       int durationMinutes, String appointmentType, String status,
                       String reasonForVisit, String notes) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.clinicianId = clinicianId;
        this.facilityId = facilityId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.durationMinutes = durationMinutes;
        this.appointmentType = appointmentType;
        this.status = status;
        this.reasonForVisit = reasonForVisit;
        this.notes = notes;
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }

    // Getters and Setters
    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String id) { this.appointmentId = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String id) { this.patientId = id; }

    public String getClinicianId() { return clinicianId; }
    public void setClinicianId(String id) { this.clinicianId = id; }

    public String getFacilityId() { return facilityId; }
    public void setFacilityId(String id) { this.facilityId = id; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate date) {
        this.appointmentDate = date;
        this.lastModified = LocalDateTime.now();
    }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String time) {
        this.appointmentTime = time;
        this.lastModified = LocalDateTime.now();
    }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int duration) { this.durationMinutes = duration; }

    public String getAppointmentType() { return appointmentType; }
    public void setAppointmentType(String type) { this.appointmentType = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.lastModified = LocalDateTime.now();
    }

    public String getReasonForVisit() { return reasonForVisit; }
    public void setReasonForVisit(String reason) { this.reasonForVisit = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) {
        this.notes = notes;
        this.lastModified = LocalDateTime.now();
    }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getLastModified() { return lastModified; }

    public boolean reschedule(LocalDate newDate, String newTime) {
        if (this.status.equals("CANCELLED")) {
            return false;
        }
        this.appointmentDate = newDate;
        this.appointmentTime = newTime;
        this.lastModified = LocalDateTime.now();
        return true;
    }

    public boolean cancel() {
        if (this.status.equals("COMPLETED")) {
            return false;
        }
        this.status = "CANCELLED";
        this.lastModified = LocalDateTime.now();
        return true;
    }

    public boolean isUpcoming() {
        return appointmentDate.isAfter(LocalDate.now()) &&
                !status.equals("CANCELLED");
    }

    public boolean canCancel() {
        return !status.equals("COMPLETED") && !status.equals("CANCELLED");
    }

    @Override
    public String toString() {
        return appointmentDate + " " + appointmentTime + " - " + status;
    }
}