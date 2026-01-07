package controller;

import model.Facility;
import java.util.*;

public class FacilityController {
    private List<Facility> facilities;
    private String dataFilename;

    public FacilityController() {
        facilities = new ArrayList<>();
    }

    public void loadFacilities(String filename) {
        dataFilename = filename;
        facilities = DataLoader.loadFacilities(filename);
    }

    private void saveToFile() {
        if (dataFilename != null) {
            DataLoader.saveFacilities(dataFilename, facilities);
        }
    }

    public List<Facility> getAllFacilities() {
        return new ArrayList<>(facilities);
    }

    public Facility getFacilityById(String facilityId) {
        for (Facility f : facilities) {
            if (f.getFacilityId().equals(facilityId)) {
                return f;
            }
        }
        return null;
    }

    public List<Facility> getFacilitiesByType(String type) {
        List<Facility> result = new ArrayList<>();

        for (Facility facility : facilities) {
            if (facility.getFacilityType().equalsIgnoreCase(type)) {
                result.add(facility);
            }
        }

        return result;
    }

    public void addFacility(Facility facility) {
        facilities.add(facility);
        saveToFile();
    }

    public boolean updateFacility(Facility facility) {
        for (int i = 0; i < facilities.size(); i++) {
            if (facilities.get(i).getFacilityId().equals(facility.getFacilityId())) {
                facilities.set(i, facility);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public boolean deleteFacility(String facilityId) {
        for (int i = 0; i < facilities.size(); i++) {
            if (facilities.get(i).getFacilityId().equals(facilityId)) {
                facilities.remove(i);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public int getFacilityCount() {
        return facilities.size();
    }
}