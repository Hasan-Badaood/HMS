package model;
import java.time.LocalDate;

public class Staff extends User {
    private String staffId;
    private String role;
    private String department;
    private String facilityId;
    private String employmentStatus;
    private LocalDate startDate;
    private String lineManager;
    private String accessLevel;

    public Staff(String userId, String firstName, String lastName, String email, String phoneNumber,
                 String staffId, String role, String department, String facilityId,
                 String employmentStatus, LocalDate startDate, String lineManager, String accessLevel) {
        super(userId, firstName, lastName, email, phoneNumber);
        this.staffId = staffId;
        this.role = role;
        this.department = department;
        this.facilityId = facilityId;
        this.employmentStatus = employmentStatus;
        this.startDate = startDate;
        this.lineManager = lineManager;
        this.accessLevel = accessLevel;
    }

    // Getters and Setters
    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getFacilityId() { return facilityId; }
    public void setFacilityId(String facilityId) { this.facilityId = facilityId; }

    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String status) { this.employmentStatus = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate date) { this.startDate = date; }

    public String getLineManager() { return lineManager; }
    public void setLineManager(String manager) { this.lineManager = manager; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String level) { this.accessLevel = level; }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(employmentStatus);
    }

    @Override
    public boolean login() {
        if (isActive()) {
            System.out.println("Staff " + getFullName() + " logged in");
            return true;
        }
        return false;
    }
}