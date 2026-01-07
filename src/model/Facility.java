package model;

import java.util.ArrayList;
import java.util.List;

public class Facility {
    private String facilityId;
    private String facilityName;
    private String facilityType;
    private String address;
    private String postcode;
    private String phoneNumber;
    private String email;
    private String openingHours;
    private String managerName;
    private int capacity;
    private List<String> specialitiesOffered;

    public Facility(String facilityId, String facilityName, String facilityType,
                    String address, String postcode, String phoneNumber, String email,
                    String openingHours, String managerName, int capacity) {
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.facilityType = facilityType;
        this.address = address;
        this.postcode = postcode;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.openingHours = openingHours;
        this.managerName = managerName;
        this.capacity = capacity;
        this.specialitiesOffered = new ArrayList<>();
    }

    // Getters and Setters
    public String getFacilityId() { return facilityId; }
    public void setFacilityId(String id) { this.facilityId = id; }

    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String name) { this.facilityName = name; }

    public String getFacilityType() { return facilityType; }
    public void setFacilityType(String type) { this.facilityType = type; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPostcode() { return postcode; }
    public void setPostcode(String postcode) { this.postcode = postcode; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phone) { this.phoneNumber = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String hours) { this.openingHours = hours; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String name) { this.managerName = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public List<String> getSpecialitiesOffered() { return specialitiesOffered; }

    public void addSpeciality(String speciality) {
        if (!specialitiesOffered.contains(speciality)) {
            specialitiesOffered.add(speciality);
        }
    }

    public void removeSpeciality(String speciality) {
        specialitiesOffered.remove(speciality);
    }

    @Override
    public String toString() {
        return facilityName + " (" + facilityType + ")";
    }
}