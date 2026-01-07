package view;

import model.Facility;
import controller.FacilityController;

import javax.swing.*;
import java.awt.*;

class FacilityDialog extends JDialog {
    private Facility facility;
    private FacilityController controller;
    private boolean confirmed = false;

    private JTextField facilityIdField, facilityNameField, addressField;
    private JTextField postcodeField, phoneField, emailField;
    private JTextField openingHoursField, managerNameField, capacityField;
    private JComboBox<String> facilityTypeCombo;

    public FacilityDialog(Frame owner, Facility facility, FacilityController controller) {
        super(owner, facility == null ? "Add Facility" : "Edit Facility", true);
        this.facility = facility;
        this.controller = controller;
        setupUI();
        if (facility != null) {
            fillFields();
        } else {
            // New facility - ID generated after type selection
            facilityIdField.setText("Select type first");
        }
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 550);
        setLocationRelativeTo(getOwner());

        // Main form
        JPanel formPanel = new JPanel(new GridLayout(10, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        facilityIdField = new JTextField();
        facilityIdField.setEditable(false);
        facilityIdField.setBackground(Color.LIGHT_GRAY);

        facilityNameField = new JTextField();
        addressField = new JTextField();
        postcodeField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();
        openingHoursField = new JTextField();
        managerNameField = new JTextField();
        capacityField = new JTextField();

        // Facility type dropdown
        String[] types = {"Hospital", "GP Surgery"};
        facilityTypeCombo = new JComboBox<>(types);

        // Update ID when type changes (for new facilities only)
        if (facility == null) {
            facilityTypeCombo.addActionListener(e -> updateFacilityId());
        }

        formPanel.add(new JLabel("Facility Type:"));
        formPanel.add(facilityTypeCombo);
        formPanel.add(new JLabel("Facility ID:"));
        formPanel.add(facilityIdField);
        formPanel.add(new JLabel("Facility Name:"));
        formPanel.add(facilityNameField);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(addressField);
        formPanel.add(new JLabel("Postcode:"));
        formPanel.add(postcodeField);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Opening Hours:"));
        formPanel.add(openingHoursField);
        formPanel.add(new JLabel("Manager Name:"));
        formPanel.add(managerNameField);
        formPanel.add(new JLabel("Capacity:"));
        formPanel.add(capacityField);

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

    private void updateFacilityId() {
        String selectedType = (String) facilityTypeCombo.getSelectedItem();
        if (selectedType != null) {
            String generatedId = generateNextFacilityId(selectedType);
            facilityIdField.setText(generatedId);
        }
    }

    private String generateNextFacilityId(String facilityType) {
        if (controller == null || controller.getAllFacilities().isEmpty()) {
            // First facility of this type
            return facilityType.equals("Hospital") ? "H001" : "S001";
        }

        // Use H for Hospital, S for GP Surgery
        String prefix = facilityType.equals("Hospital") ? "H" : "S";

        int maxNumber = 0;
        for (Facility f : controller.getAllFacilities()) {
            String id = f.getFacilityId();
            if (id != null && id.startsWith(prefix)) {
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

        return String.format("%s%03d", prefix, maxNumber + 1);
    }

    private void fillFields() {
        facilityIdField.setText(facility.getFacilityId());
        facilityNameField.setText(facility.getFacilityName());
        addressField.setText(facility.getAddress());
        postcodeField.setText(facility.getPostcode());
        phoneField.setText(facility.getPhoneNumber());
        emailField.setText(facility.getEmail());
        openingHoursField.setText(facility.getOpeningHours());
        managerNameField.setText(facility.getManagerName());
        capacityField.setText(String.valueOf(facility.getCapacity()));

        // Select current facility type
        String type = facility.getFacilityType();
        if (type != null && !type.isEmpty()) {
            facilityTypeCombo.setSelectedItem(type);
        }

        // Can't change type when editing (ID prefix must stay the same)
        facilityTypeCombo.setEnabled(false);
    }

    private void save() {
        try {
            String facilityId = facilityIdField.getText().trim();

            // Check if ID has been generated
            if (facilityId.equals("Select type first")) {
                JOptionPane.showMessageDialog(this,
                        "Please select a facility type first",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Parse capacity
            int capacity;
            try {
                capacity = Integer.parseInt(capacityField.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Capacity must be a valid number",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            facility = new Facility(
                    facilityId,
                    facilityNameField.getText().trim(),
                    (String) facilityTypeCombo.getSelectedItem(),
                    addressField.getText().trim(),
                    postcodeField.getText().trim(),
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    openingHoursField.getText().trim(),
                    managerNameField.getText().trim(),
                    capacity
            );

            confirmed = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving facility: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Facility getFacility() {
        return facility;
    }
}