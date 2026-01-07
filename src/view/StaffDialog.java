package view;

import model.Staff;
import controller.StaffController;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.regex.Pattern;

class StaffDialog extends JDialog {
    private Staff staff;
    private StaffController controller;
    private boolean confirmed = false;

    private JTextField staffIdField, firstNameField, lastNameField;
    private JTextField emailField, phoneField, facilityIdField;
    private JTextField lineManagerField, startDateField, roleField;
    private JComboBox<String> statusCombo, accessCombo, departmentCombo;

    // Validation patterns
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s'-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\s()-]{10,15}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public StaffDialog(Frame owner, Staff staff, StaffController controller) {
        super(owner, staff == null ? "Add Staff" : "Edit Staff", true);
        this.staff = staff;
        this.controller = controller;
        setupUI();
        if (staff != null) {
            fillFields();
        } else {
            // Generate ID for new staff
            staffIdField.setText(generateNextStaffId());
            startDateField.setText(LocalDate.now().toString());
        }
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 650);
        setLocationRelativeTo(getOwner());

        // Main form
        JPanel formPanel = new JPanel(new GridLayout(12, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        staffIdField = new JTextField();
        staffIdField.setEditable(false);
        staffIdField.setBackground(Color.LIGHT_GRAY);

        firstNameField = new JTextField();
        lastNameField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        facilityIdField = new JTextField();
        lineManagerField = new JTextField();
        startDateField = new JTextField();
        roleField = new JTextField();

        // Employment status options
        String[] statuses = {"Full-time", "Part-time"};
        statusCombo = new JComboBox<>(statuses);

        // Access level options
        String[] accessLevels = {"Basic", "Manager", "Standard"};
        accessCombo = new JComboBox<>(accessLevels);

        // Department options
        String[] departments = {"Administration", "Front Desk", "Clinical Support"};
        departmentCombo = new JComboBox<>(departments);

        formPanel.add(new JLabel("Staff ID:"));
        formPanel.add(staffIdField);
        formPanel.add(new JLabel("First Name:"));
        formPanel.add(firstNameField);
        formPanel.add(new JLabel("Last Name:"));
        formPanel.add(lastNameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(roleField);
        formPanel.add(new JLabel("Department:"));
        formPanel.add(departmentCombo);
        formPanel.add(new JLabel("Facility ID:"));
        formPanel.add(facilityIdField);
        formPanel.add(new JLabel("Employment Status:"));
        formPanel.add(statusCombo);
        formPanel.add(new JLabel("Start Date (yyyy-mm-dd):"));
        formPanel.add(startDateField);
        formPanel.add(new JLabel("Line Manager:"));
        formPanel.add(lineManagerField);
        formPanel.add(new JLabel("Access Level:"));
        formPanel.add(accessCombo);

        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> save());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private String generateNextStaffId() {
        if (controller == null || controller.getAllStaff().isEmpty()) {
            return "ST001";
        }

        int maxNumber = 0;
        for (Staff s : controller.getAllStaff()) {
            String id = s.getStaffId();
            if (id != null && id.startsWith("ST")) {
                try {
                    int num = Integer.parseInt(id.substring(2));
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                }
            }
        }

        return String.format("ST%03d", maxNumber + 1);
    }

    private void fillFields() {
        staffIdField.setText(staff.getStaffId());
        firstNameField.setText(staff.getFirstName());
        lastNameField.setText(staff.getLastName());
        emailField.setText(staff.getEmail());
        phoneField.setText(staff.getPhoneNumber());
        facilityIdField.setText(staff.getFacilityId());
        lineManagerField.setText(staff.getLineManager());
        startDateField.setText(staff.getStartDate().toString());

        // Set role field
        String role = staff.getRole();
        if (role != null && !role.isEmpty()) {
            roleField.setText(role);
        }

        // Select department
        String department = staff.getDepartment();
        if (department != null && !department.isEmpty()) {
            departmentCombo.setSelectedItem(department);
        }

        // Select employment status
        String status = staff.getEmploymentStatus();
        if (status != null && !status.isEmpty()) {
            statusCombo.setSelectedItem(status);
        }

        // Select access level
        String access = staff.getAccessLevel();
        if (access != null && !access.isEmpty()) {
            accessCombo.setSelectedItem(access);
        }
    }

    private boolean validateFields() {
        // Check first name
        String firstName = firstNameField.getText().trim();
        if (firstName.isEmpty() || !NAME_PATTERN.matcher(firstName).matches()) {
            showValidationError("First Name must contain only letters, spaces, hyphens, or apostrophes.");
            return false;
        }

        // Check last name
        String lastName = lastNameField.getText().trim();
        if (lastName.isEmpty() || !NAME_PATTERN.matcher(lastName).matches()) {
            showValidationError("Last Name must contain only letters, spaces, hyphens, or apostrophes.");
            return false;
        }

        // Check email
        String email = emailField.getText().trim();
        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            showValidationError("Please enter a valid email address (e.g., user@example.com).");
            return false;
        }

        // Check phone number
        String phone = phoneField.getText().trim();
        if (phone.isEmpty() || !PHONE_PATTERN.matcher(phone).matches()) {
            showValidationError("Phone number must be 10-15 digits and may contain +, -, (, ), or spaces.");
            return false;
        }

        // Check role
        if (roleField.getText().trim().isEmpty()) {
            showValidationError("Role is required.");
            return false;
        }

        // Check facility ID
        if (facilityIdField.getText().trim().isEmpty()) {
            showValidationError("Facility ID is required.");
            return false;
        }

        // Check start date
        String startDate = startDateField.getText().trim();
        if (startDate.isEmpty()) {
            showValidationError("Start Date is required.");
            return false;
        }
        try {
            LocalDate.parse(startDate);
        } catch (Exception e) {
            showValidationError("Start Date must be in format yyyy-mm-dd (e.g., 2024-01-15).");
            return false;
        }

        // Check line manager (optional field)
        String lineManager = lineManagerField.getText().trim();
        if (!lineManager.isEmpty() && !NAME_PATTERN.matcher(lineManager).matches()) {
            showValidationError("Line Manager name must contain only letters, spaces, hyphens, or apostrophes.");
            return false;
        }

        return true;
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void save() {
        // First validate all inputs
        if (!validateFields()) {
            return;
        }

        try {
            String staffId = staffIdField.getText().trim();

            staff = new Staff(
                    staffId,
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    staffId,
                    roleField.getText().trim(),
                    (String) departmentCombo.getSelectedItem(),
                    facilityIdField.getText().trim(),
                    (String) statusCombo.getSelectedItem(),
                    LocalDate.parse(startDateField.getText().trim()),
                    lineManagerField.getText().trim(),
                    (String) accessCombo.getSelectedItem()
            );

            confirmed = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving staff: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Staff getStaff() {
        return staff;
    }
}