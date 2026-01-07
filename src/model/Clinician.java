package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Clinician extends Staff {

    private String clinicianId;
    private String title;
    private String specialization;
    private String licenseNumber;
    private String workplaceType;
    private List<Appointment> appointments;

    public Clinician(
            String clinicianId,
            String firstName,
            String lastName,
            String title,
            String specialization,
            String licenseNumber,
            String phoneNumber,
            String email,
            String facilityId,
            String workplaceType,
            String employmentStatus,
            LocalDate startDate
    ) {
        super(
                clinicianId,          // userId
                firstName,
                lastName,
                email,
                phoneNumber,
                clinicianId,          // staffId
                "CLINICIAN",          // role
                title,                // department (using title)
                facilityId,
                employmentStatus,
                startDate,
                "",                   // lineManager
                "FULL"                // accessLevel
        );

        this.clinicianId = clinicianId;
        this.title = title;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.workplaceType = workplaceType;
        this.appointments = new ArrayList<>();
    }

    // getters and setters

    public String getClinicianId() {
        return clinicianId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getWorkplaceType() {
        return workplaceType;
    }

    public void setWorkplaceType(String workplaceType) {
        this.workplaceType = workplaceType;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void addAppointment(Appointment appointment) {
        this.appointments.add(appointment);
    }

    // business methods

    public Prescription createPrescription(
            Patient patient,
            String medicationName,
            String dosage,
            String frequency,
            int durationDays
    ) {
        Prescription prescription = new Prescription(
                java.util.UUID.randomUUID().toString(),
                patient.getPatientId(),
                this.clinicianId,
                null,
                LocalDate.now(),
                medicationName,
                dosage,
                frequency,
                durationDays,
                "",
                "",
                "",
                "PENDING",
                LocalDate.now(),
                null
        );
        patient.addPrescription(prescription);
        return prescription;
    }

    public PatientRecord viewPatientRecord(Patient patient) {
        return patient.getPatientRecord();
    }

    public boolean checkAvailability(LocalDate date, String time) {
        return true; // simplified
    }
}