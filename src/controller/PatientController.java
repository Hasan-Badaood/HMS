package controller;

import model.Patient;
import java.util.ArrayList;
import java.util.List;

public class PatientController {
    private List<Patient> patients;
    private String dataFilename;

    public PatientController() {
        patients = new ArrayList<>();
    }

    public void loadPatients(String filename) {
        dataFilename = filename;
        patients = DataLoader.loadPatients(filename);
    }

    private void saveToFile() {
        if (dataFilename != null) {
            DataLoader.savePatients(dataFilename, patients);
        }
    }

    public List<Patient> getAllPatients() {
        return new ArrayList<>(patients);
    }

    public Patient getPatientById(String patientId) {
        for (Patient p : patients) {
            if (p.getPatientId().equals(patientId)) {
                return p;
            }
        }
        return null;
    }

    public Patient getPatientByNHS(String nhsNumber) {
        for (Patient patient : patients) {
            if (patient.getNhsNumber().equals(nhsNumber)) {
                return patient;
            }
        }
        return null;
    }

    public List<Patient> searchPatients(String query) {
        List<Patient> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Patient p : patients) {
            // Check if query matches name, NHS number, or email
            if (p.getFullName().toLowerCase().contains(lowerQuery) ||
                    p.getNhsNumber().contains(query) ||
                    p.getEmail().toLowerCase().contains(lowerQuery)) {
                results.add(p);
            }
        }

        return results;
    }

    public void addPatient(Patient patient) {
        patients.add(patient);
        saveToFile();
    }

    public boolean updatePatient(Patient patient) {
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getPatientId().equals(patient.getPatientId())) {
                patients.set(i, patient);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public boolean deletePatient(String patientId) {
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getPatientId().equals(patientId)) {
                patients.remove(i);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public int getPatientCount() {
        return patients.size();
    }
}