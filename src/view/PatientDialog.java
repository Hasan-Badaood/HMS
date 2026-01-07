package view;

import model.Patient;
import controller.PatientController;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.regex.Pattern;

class PatientDialog extends JDialog {
    private Patient patient;
    private PatientController controller;
    private boolean confirmed = false;

    private JTextField patientIdField, firstNameField, lastNameField, nhsNumberField;
    private JTextField dobField, phoneField, emailField;
    private JTextField addressField, postcodeField, emergencyNameField, emergencyPhoneField;
    private JComboBox<String> gpSurgeryCombo, genderCombo;

    // Validation patterns
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s'-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\s()-]{10,15}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public PatientDialog(Frame owner, Patient patient, PatientController controller) {
        super(owner, patient == null ? "Add Patient" : "Edit Patient", true);
        this.patient = patient;
        this.controller = controller;
        setupUI();
        if (patient != null) {
            fillFields();
        } else {
            // Generate ID for new patient
            patientIdField.setText(generateNextPatientId());
        }
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 600);
        setLocationRelativeTo(getOwner());

        // Main form
        JPanel formPanel = new JPanel(new GridLayout(13, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        patientIdField = new JTextField();
        patientIdField.setEditable(false);
        patientIdField.setBackground(Color.LIGHT_GRAY);

        firstNameField = new JTextField();
        lastNameField = new JTextField();
        nhsNumberField = new JTextField();
        dobField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();
        addressField = new JTextField();
        postcodeField = new JTextField();
        emergencyNameField = new JTextField();
        emergencyPhoneField = new JTextField();

        // Gender selection
        String[] genders = {"M", "F"};
        genderCombo = new JComboBox<>(genders);

        // GP Surgery selection
        String[] surgeries = {"S001", "S002", "S003"};
        gpSurgeryCombo = new JComboBox<>(surgeries);

        formPanel.add(new JLabel("Patient ID:"));
        formPanel.add(patientIdField);
        formPanel.add(new JLabel("First Name:"));
        formPanel.add(firstNameField);
        formPanel.add(new JLabel("Last Name:"));
        formPanel.add(lastNameField);
        formPanel.add(new JLabel("NHS Number:"));
        formPanel.add(nhsNumberField);
        formPanel.add(new JLabel("Date of Birth (yyyy-mm-dd):"));
        formPanel.add(dobField);
        formPanel.add(new JLabel("Gender:"));
        formPanel.add(genderCombo);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(addressField);
        formPanel.add(new JLabel("Postcode:"));
        formPanel.add(postcodeField);
        formPanel.add(new JLabel("Emergency Contact Name:"));
        formPanel.add(emergencyNameField);
        formPanel.add(new JLabel("Emergency Contact Phone:"));
        formPanel.add(emergencyPhoneField);
        formPanel.add(new JLabel("GP Surgery ID:"));
        formPanel.add(gpSurgeryCombo);

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

    private String generateNextPatientId() {
        if (controller == null || controller.getAllPatients().isEmpty()) {
            return "P001";
        }

        int maxNumber = 0;
        for (Patient p : controller.getAllPatients()) {
            String id = p.getPatientId();
            if (id != null && id.startsWith("P")) {
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

        return String.format("P%03d", maxNumber + 1);
    }

    private void fillFields() {
        patientIdField.setText(patient.getPatientId());
        firstNameField.setText(patient.getFirstName());
        lastNameField.setText(patient.getLastName());
        nhsNumberField.setText(patient.getNhsNumber());
        dobField.setText(patient.getDateOfBirth().toString());
        phoneField.setText(patient.getPhoneNumber());
        emailField.setText(patient.getEmail());
        addressField.setText(patient.getAddress());
        postcodeField.setText(patient.getPostcode());
        emergencyNameField.setText(patient.getEmergencyContactName());
        emergencyPhoneField.setText(patient.getEmergencyContactPhone());

        // Select gender
        String gender = patient.getGender();
        if (gender != null && !gender.isEmpty()) {
            genderCombo.setSelectedItem(gender);
        }

        // Select GP surgery
        String gpSurgery = patient.getGpSurgeryId();
        if (gpSurgery != null && !gpSurgery.isEmpty()) {
            gpSurgeryCombo.setSelectedItem(gpSurgery);
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

        // Check NHS number (10 digits)
        String nhsNumber = nhsNumberField.getText().trim();
        if (nhsNumber.isEmpty() || !nhsNumber.matches("^[0-9]{10}$")) {
            showValidationError("NHS Number must be exactly 10 digits.");
            return false;
        }

        // Check date of birth
        String dob = dobField.getText().trim();
        if (dob.isEmpty()) {
            showValidationError("Date of Birth is required.");
            return false;
        }
        try {
            LocalDate.parse(dob);
        } catch (Exception e) {
            showValidationError("Date of Birth must be in format yyyy-mm-dd (e.g., 1990-01-15).");
            return false;
        }

        // Check phone number
        String phone = phoneField.getText().trim();
        if (phone.isEmpty() || !PHONE_PATTERN.matcher(phone).matches()) {
            showValidationError("Phone number must be 10-15 digits and may contain +, -, (, ), or spaces.");
            return false;
        }

        // Check email
        String email = emailField.getText().trim();
        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            showValidationError("Please enter a valid email address (e.g., user@example.com).");
            return false;
        }

        // Check emergency contact name
        String emergencyName = emergencyNameField.getText().trim();
        if (emergencyName.isEmpty() || !NAME_PATTERN.matcher(emergencyName).matches()) {
            showValidationError("Emergency Contact Name must contain only letters, spaces, hyphens, or apostrophes.");
            return false;
        }

        // Check emergency contact phone
        String emergencyPhone = emergencyPhoneField.getText().trim();
        if (emergencyPhone.isEmpty() || !PHONE_PATTERN.matcher(emergencyPhone).matches()) {
            showValidationError("Emergency Contact Phone must be 10-15 digits and may contain +, -, (, ), or spaces.");
            return false;
        }

        // Check address
        if (addressField.getText().trim().isEmpty()) {
            showValidationError("Address is required.");
            return false;
        }

        // Check postcode
        if (postcodeField.getText().trim().isEmpty()) {
            showValidationError("Postcode is required.");
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
            String patientId = patientIdField.getText().trim();

            patient = new Patient(
                    patientId,
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    patientId,
                    LocalDate.parse(dobField.getText().trim()),
                    nhsNumberField.getText().trim(),
                    (String) genderCombo.getSelectedItem(),
                    addressField.getText().trim(),
                    postcodeField.getText().trim(),
                    emergencyNameField.getText().trim(),
                    emergencyPhoneField.getText().trim(),
                    LocalDate.now(),
                    (String) gpSurgeryCombo.getSelectedItem()
            );

            confirmed = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving patient: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Patient getPatient() {
        return patient;
    }
}