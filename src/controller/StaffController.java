package controller;

import model.Staff;
import java.util.*;

public class StaffController {

    private List<Staff> staffList;
    private String dataFilename;

    public StaffController() {
        staffList = new ArrayList<>();
    }

    public void loadStaff(String filename) {
        dataFilename = filename;
        staffList = DataLoader.loadStaff(filename);
    }

    private void saveToFile() {
        if (dataFilename != null) {
            DataLoader.saveStaff(dataFilename, staffList);
        }
    }

    public List<Staff> getAllStaff() {
        return new ArrayList<>(staffList);
    }

    public Staff getStaffById(String staffId) {
        for (Staff s : staffList) {
            if (s.getStaffId().equals(staffId)) {
                return s;
            }
        }
        return null;
    }

    public List<Staff> getStaffByRole(String role) {
        List<Staff> result = new ArrayList<>();

        for (Staff staff : staffList) {
            if (staff.getRole().equalsIgnoreCase(role)) {
                result.add(staff);
            }
        }

        return result;
    }

    public List<Staff> getStaffByFacility(String facilityId) {
        List<Staff> facilityStaff = new ArrayList<>();

        for (Staff s : staffList) {
            if (s.getFacilityId().equals(facilityId)) {
                facilityStaff.add(s);
            }
        }

        return facilityStaff;
    }

    public List<Staff> getActiveStaff() {
        List<Staff> activeStaff = new ArrayList<>();

        for (Staff staff : staffList) {
            String status = staff.getEmploymentStatus();
            if ("Full-time".equalsIgnoreCase(status) ||
                    "Part-time".equalsIgnoreCase(status)) {
                activeStaff.add(staff);
            }
        }

        return activeStaff;
    }

    public void addStaff(Staff staff) {
        staffList.add(staff);
        saveToFile();
    }

    public boolean updateStaff(Staff staff) {
        for (int i = 0; i < staffList.size(); i++) {
            if (staffList.get(i).getStaffId().equals(staff.getStaffId())) {
                staffList.set(i, staff);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public boolean deleteStaff(String staffId) {
        for (int i = 0; i < staffList.size(); i++) {
            if (staffList.get(i).getStaffId().equals(staffId)) {
                staffList.remove(i);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public List<String> getAllRoles() {
        Set<String> roles = new HashSet<>();

        for (Staff staff : staffList) {
            roles.add(staff.getRole());
        }

        // Convert to list and sort
        List<String> roleList = new ArrayList<>(roles);
        Collections.sort(roleList);

        return roleList;
    }

    public int getStaffCount() {
        return staffList.size();
    }
}