package controller;

import model.Clinician;
import java.time.LocalDate;
import java.util.*;

public class ClinicianController {

    private List<Clinician> clinicians;
    private String dataFilename;

    public ClinicianController() {
        clinicians = new ArrayList<>();
    }

    public void loadClinicians(String filename) {
        dataFilename = filename;
        clinicians = DataLoader.loadClinicians(filename);
    }

    private void saveToFile() {
        if (dataFilename != null) {
            DataLoader.saveClinicians(dataFilename, clinicians);
        }
    }

    public List<Clinician> getAllClinicians() {
        return new ArrayList<>(clinicians);
    }

    public Clinician getClinicianById(String id) {
        for (Clinician clinician : clinicians) {
            if (clinician.getClinicianId().equals(id)) {
                return clinician;
            }
        }
        return null;
    }

    public List<Clinician> getCliniciansBySpecialization(String spec) {
        List<Clinician> result = new ArrayList<>();

        for (Clinician clinician : clinicians) {
            if (clinician.getSpecialization().equalsIgnoreCase(spec)) {
                result.add(clinician);
            }
        }

        return result;
    }

    public List<Clinician> getCliniciansByFacility(String facilityId) {
        List<Clinician> facilityDoctors = new ArrayList<>();

        for (Clinician c : clinicians) {
            if (c.getFacilityId().equals(facilityId)) {
                facilityDoctors.add(c);
            }
        }

        return facilityDoctors;
    }

    public List<Clinician> getAvailableClinicians(LocalDate date) {
        // In a real system, this would check against actual appointments
        List<Clinician> available = new ArrayList<>();

        for (Clinician clinician : clinicians) {
            if (clinician.isActive()) {
                available.add(clinician);
            }
        }

        return available;
    }

    public void addClinician(Clinician c) {
        clinicians.add(c);
        saveToFile();
    }

    public boolean updateClinician(Clinician c) {
        for (int i = 0; i < clinicians.size(); i++) {
            if (clinicians.get(i).getClinicianId().equals(c.getClinicianId())) {
                clinicians.set(i, c);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public boolean deleteClinician(String id) {
        for (int i = 0; i < clinicians.size(); i++) {
            if (clinicians.get(i).getClinicianId().equals(id)) {
                clinicians.remove(i);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public List<String> getAllSpecializations() {
        Set<String> specializations = new HashSet<>();

        // Collect all unique specializations
        for (Clinician clinician : clinicians) {
            specializations.add(clinician.getSpecialization());
        }

        // Convert to sorted list
        List<String> sortedSpecs = new ArrayList<>(specializations);
        Collections.sort(sortedSpecs);

        return sortedSpecs;
    }

    public int getClinicianCount() {
        return clinicians.size();
    }
}