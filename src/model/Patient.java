package model;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class Patient extends User {
    private String patientId;
    private LocalDate dateOfBirth;
    private String nhsNumber;
    private String gender;
    private String address;
    private String postcode;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private LocalDate registrationDate;
    private String gpSurgeryId;
    private PatientRecord patientRecord;
    private List<Appointment> appointments;
    private List<Prescription> prescriptions;

    public Patient(String userId, String firstName, String lastName, String email, String phoneNumber,
                   String patientId, LocalDate dateOfBirth, String nhsNumber, String gender,
                   String address, String postcode, String emergencyContactName,
                   String emergencyContactPhone, LocalDate registrationDate, String gpSurgeryId) {
        super(userId, firstName, lastName, email, phoneNumber);
        this.patientId = patientId;
        this.dateOfBirth = dateOfBirth;
        this.nhsNumber = nhsNumber;
        this.gender = gender;
        this.address = address;
        this.postcode = postcode;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.registrationDate = registrationDate;
        this.gpSurgeryId = gpSurgeryId;
        this.appointments = new ArrayList<>();
        this.prescriptions = new ArrayList<>();
        this.patientRecord = new PatientRecord(this);
    }

    // Getters and Setters
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getNhsNumber() { return nhsNumber; }
    public void setNhsNumber(String nhsNumber) { this.nhsNumber = nhsNumber; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPostcode() { return postcode; }
    public void setPostcode(String postcode) { this.postcode = postcode; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String name) { this.emergencyContactName = name; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String phone) { this.emergencyContactPhone = phone; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate date) { this.registrationDate = date; }

    public String getGpSurgeryId() { return gpSurgeryId; }
    public void setGpSurgeryId(String id) { this.gpSurgeryId = id; }

    public PatientRecord getPatientRecord() { return patientRecord; }

    public List<Appointment> getAppointments() { return appointments; }
    public void addAppointment(Appointment appointment) {
        this.appointments.add(appointment);
    }

    public List<Prescription> getPrescriptions() { return prescriptions; }
    public void addPrescription(Prescription prescription) {
        this.prescriptions.add(prescription);
    }

    public int getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public void updateAddress(String address, String postcode) {
        this.address = address;
        this.postcode = postcode;
    }

    public void updateEmergencyContact(String name, String phone) {
        this.emergencyContactName = name;
        this.emergencyContactPhone = phone;
    }

    @Override
    public boolean login() {
        System.out.println("Patient " + getFullName() + " logged in");
        return true;
    }
}
