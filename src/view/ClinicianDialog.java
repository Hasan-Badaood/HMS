package view;

import model.Clinician;
import controller.ClinicianController;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.regex.Pattern;

class ClinicianDialog extends JDialog {
    private Clinician clinician;
    private ClinicianController controller;
    private boolean confirmed = false;

    private JTextField clinicianIdField, firstNameField, lastNameField;
    private JTextField titleField, specializationField, licenseField;
    private JTextField phoneField, emailField, facilityIdField;
    private JTextField startDateField;
    private JComboBox<String> workplaceCombo, statusCombo;

    // Validation patterns
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s'-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\s()-]{10,15}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public ClinicianDialog(Frame owner, Clinician clinician, ClinicianController controller) {
        super(owner, clinician == null ? "Add Clinician" : "Edit Clinician", true);
        this.clinician = clinician;
        this.controller = controller;
        setupUI();
        if (clinician != null) {
            fillFields();
        } else {
            // Generate ID for new clinician
            clinicianIdField.setText(generateNextClinicianId());
            startDateField.setText(LocalDate.now().toString());
        }
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 600);
        setLocationRelativeTo(getOwner());

        // Main form panel
        JPanel formPanel = new JPanel(new GridLayout(12, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        clinicianIdField = new JTextField();
        clinicianIdField.setEditable(false);
        clinicianIdField.setBackground(Color.LIGHT_GRAY);

        firstNameField = new JTextField();
        lastNameField = new JTextField();
        titleField = new JTextField();
        specializationField = new JTextField();
        licenseField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();
        facilityIdField = new JTextField();
        startDateField = new JTextField();

        // Workplace options
        String[] workplaces = {"Hospital", "Clinic", "Private Practice", "Community Health Center"};
        workplaceCombo = new JComboBox<>(workplaces);

        // Employment status options
        String[] statuses = {"Full-time", "Part-time"};
        statusCombo = new JComboBox<>(statuses);

        formPanel.add(new JLabel("Clinician ID:"));
        formPanel.add(clinicianIdField);
        formPanel.add(new JLabel("First Name:"));
        formPanel.add(firstNameField);
        formPanel.add(new JLabel("Last Name:"));
        formPanel.add(lastNameField);
        formPanel.add(new JLabel("Title (e.g., Dr., Prof.):"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Specialization:"));
        formPanel.add(specializationField);
        formPanel.add(new JLabel("License Number:"));
        formPanel.add(licenseField);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Facility ID:"));
        formPanel.add(facilityIdField);
        formPanel.add(new JLabel("Workplace Type:"));
        formPanel.add(workplaceCombo);
        formPanel.add(new JLabel("Employment Status:"));
        formPanel.add(statusCombo);
        formPanel.add(new JLabel("Start Date (yyyy-mm-dd):"));
        formPanel.add(startDateField);

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

    private String generateNextClinicianId() {
        if (controller == null || controller.getAllClinicians().isEmpty()) {
            return "C001";
        }

        int maxNumber = 0;
        for (Clinician c : controller.getAllClinicians()) {
            String id = c.getClinicianId();
            if (id != null && id.startsWith("C")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                }
            }
        }

        return String.format("C%03d", maxNumber + 1);
    }

    private void fillFields() {
        clinicianIdField.setText(clinician.getClinicianId());
        firstNameField.setText(clinician.getFirstName());
        lastNameField.setText(clinician.getLastName());
        titleField.setText(clinician.getTitle());
        specializationField.setText(clinician.getSpecialization());
        licenseField.setText(clinician.getLicenseNumber());
        phoneField.setText(clinician.getPhoneNumber());
        emailField.setText(clinician.getEmail());
        facilityIdField.setText(clinician.getFacilityId());
        startDateField.setText(clinician.getStartDate().toString());

        // Select combo box values
        String workplace = clinician.getWorkplaceType();
        if (workplace != null && !workplace.isEmpty()) {
            workplaceCombo.setSelectedItem(workplace);
        }

        String status = clinician.getEmploymentStatus();
        if (status != null && !status.isEmpty()) {
            statusCombo.setSelectedItem(status);
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

        // Check title
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showValidationError("Title is required (e.g., Dr., Prof.).");
            return false;
        }

        // Check specialization
        if (specializationField.getText().trim().isEmpty()) {
            showValidationError("Specialization is required.");
            return false;
        }

        // Check license number
        if (licenseField.getText().trim().isEmpty()) {
            showValidationError("License Number is required.");
            return false;
        }

        // Check phone number format
        String phone = phoneField.getText().trim();
        if (phone.isEmpty() || !PHONE_PATTERN.matcher(phone).matches()) {
            showValidationError("Phone number must be 10-15 digits and may contain +, -, (, ), or spaces.");
            return false;
        }

        // Check email format
        String email = emailField.getText().trim();
        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            showValidationError("Please enter a valid email address (e.g., user@example.com).");
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
            String clinicianId = clinicianIdField.getText().trim();

            clinician = new Clinician(
                    clinicianId,
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    titleField.getText().trim(),
                    specializationField.getText().trim(),
                    licenseField.getText().trim(),
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    facilityIdField.getText().trim(),
                    (String) workplaceCombo.getSelectedItem(),
                    (String) statusCombo.getSelectedItem(),
                    LocalDate.parse(startDateField.getText().trim())
            );

            confirmed = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving clinician: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Clinician getClinician() {
        return clinician;
    }
}