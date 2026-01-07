package view;

import model.*;
import controller.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

class ReferralDialog extends JDialog {
    private Referral referral;
    private boolean confirmed = false;
    private PatientController patientController;
    private ClinicianController clinicianController;
    private FacilityController facilityController;
    private ReferralController referralController;

    // Form fields
    private JTextField referralIdField;
    private JComboBox<String> patientCombo;
    private JComboBox<String> referringClinicianCombo;
    private JComboBox<String> referredToClinicianCombo;
    private JComboBox<String> referringFacilityCombo;
    private JComboBox<String> referredToFacilityCombo;
    private JComboBox<String> urgencyCombo;
    private JComboBox<String> statusCombo;
    private JTextField investigationsField;
    private JTextField appointmentField;
    private JTextArea reasonArea;
    private JTextArea clinicalSummaryArea;
    private JTextArea notesArea;

    public ReferralDialog(Frame owner, Referral referral,
                          PatientController patientController,
                          ClinicianController clinicianController,
                          FacilityController facilityController,
                          ReferralController referralController) {
        super(owner, referral == null ? "Add Referral" : "Edit Referral", true);
        this.referral = referral;
        this.patientController = patientController;
        this.clinicianController = clinicianController;
        this.facilityController = facilityController;
        this.referralController = referralController;

        setupUI();

        if (referral != null) {
            fillFields();
        } else {
            referralIdField.setText(generateNextReferralId());
        }

        pack();
        setSize(new Dimension(650, 800));
        setLocationRelativeTo(owner);
    }

    private String generateNextReferralId() {
        if (referralController == null || referralController.getAllReferrals().isEmpty()) {
            return "R001";
        }

        int maxNumber = 0;
        for (Referral r : referralController.getAllReferrals()) {
            String id = r.getReferralId();
            if (id != null && id.startsWith("R")) {
                try {
                    String numPart = id.substring(1).replaceAll("[^0-9]", "");
                    if (!numPart.isEmpty()) {
                        int num = Integer.parseInt(numPart);
                        if (num > maxNumber) {
                            maxNumber = num;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                }
            }
        }

        return String.format("R%03d", maxNumber + 1);
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Initialize fields
        referralIdField = new JTextField(20);
        referralIdField.setEditable(false);
        referralIdField.setBackground(new Color(240, 240, 240));

        patientCombo = new JComboBox<>();
        populatePatientCombo();

        referringClinicianCombo = new JComboBox<>();
        referredToClinicianCombo = new JComboBox<>();
        populateClinicianCombos();

        referringFacilityCombo = new JComboBox<>();
        referredToFacilityCombo = new JComboBox<>();
        populateFacilityCombos();

        String[] urgencyLevels = {"Routine", "Urgent", "Emergency"};
        urgencyCombo = new JComboBox<>(urgencyLevels);

        String[] statuses = {"Pending", "In Progress", "Completed", "Cancelled"};
        statusCombo = new JComboBox<>(statuses);

        investigationsField = new JTextField(20);
        investigationsField.setToolTipText("Separate multiple investigations with | (e.g., MRI Brain|CT Scan)");

        appointmentField = new JTextField(20);
        appointmentField.setToolTipText("Appointment ID (e.g., A001)");

        int row = 0;

        // Referral ID
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Referral ID:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(referralIdField, gbc);

        // Patient
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Patient:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(patientCombo, gbc);

        // Referring Clinician
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Referring Clinician:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(referringClinicianCombo, gbc);

        // Referred To Clinician
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Referred To Clinician:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(referredToClinicianCombo, gbc);

        // Referring Facility
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Referring Facility:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(referringFacilityCombo, gbc);

        // Referred To Facility
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Referred To Facility:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(referredToFacilityCombo, gbc);

        // Urgency Level
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Urgency Level:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(urgencyCombo, gbc);

        // Status
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(statusCombo, gbc);

        // Requested Investigations
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Investigations:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(investigationsField, gbc);

        // Appointment ID
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Appointment ID:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(appointmentField, gbc);

        add(formPanel, BorderLayout.NORTH);

        // Text areas panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // Referral Reason
        JPanel reasonPanel = new JPanel(new BorderLayout());
        reasonPanel.setBorder(BorderFactory.createTitledBorder("Referral Reason"));
        reasonArea = new JTextArea(3, 40);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonPanel.add(new JScrollPane(reasonArea), BorderLayout.CENTER);
        textPanel.add(reasonPanel);

        // Clinical Summary
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Clinical Summary"));
        clinicalSummaryArea = new JTextArea(4, 40);
        clinicalSummaryArea.setLineWrap(true);
        clinicalSummaryArea.setWrapStyleWord(true);
        summaryPanel.add(new JScrollPane(clinicalSummaryArea), BorderLayout.CENTER);
        textPanel.add(summaryPanel);

        // Notes
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBorder(BorderFactory.createTitledBorder("Notes"));
        notesArea = new JTextArea(3, 40);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesPanel.add(new JScrollPane(notesArea), BorderLayout.CENTER);
        textPanel.add(notesPanel);

        add(textPanel, BorderLayout.CENTER);

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

    private void populatePatientCombo() {
        for (Patient p : patientController.getAllPatients()) {
            patientCombo.addItem(p.getPatientId() + " - " + p.getFullName());
        }
    }

    private void populateClinicianCombos() {
        for (Clinician c : clinicianController.getAllClinicians()) {
            String item = c.getClinicianId() + " - " + c.getFullName();
            referringClinicianCombo.addItem(item);
            referredToClinicianCombo.addItem(item);
        }
    }

    private void populateFacilityCombos() {
        for (Facility f : facilityController.getAllFacilities()) {
            String item = f.getFacilityId() + " - " + f.getFacilityName();
            referringFacilityCombo.addItem(item);
            referredToFacilityCombo.addItem(item);
        }
    }

    // Extract ID from "ID - Name" format
    private String extractId(String comboItem) {
        if (comboItem == null || comboItem.startsWith("--")) {
            return null;
        }
        int dashIndex = comboItem.indexOf(" - ");
        if (dashIndex > 0) {
            return comboItem.substring(0, dashIndex).trim();
        }
        return comboItem.trim();
    }

    // Select combo item by ID
    private void selectComboById(JComboBox<String> combo, String id) {
        if (id == null || id.isEmpty()) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item != null && item.startsWith(id + " -")) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void fillFields() {
        referralIdField.setText(referral.getReferralId());

        selectComboById(patientCombo, referral.getPatientId());
        selectComboById(referringClinicianCombo, referral.getReferringClinicianId());
        selectComboById(referredToClinicianCombo, referral.getReferredToClinicianId());
        selectComboById(referringFacilityCombo, referral.getReferringFacilityId());
        selectComboById(referredToFacilityCombo, referral.getReferredToFacilityId());

        if (referral.getUrgencyLevel() != null) {
            urgencyCombo.setSelectedItem(referral.getUrgencyLevel());
        }
        if (referral.getStatus() != null) {
            statusCombo.setSelectedItem(referral.getStatus());
        }

        investigationsField.setText(referral.getRequestedInvestigations() != null ?
                referral.getRequestedInvestigations() : "");
        appointmentField.setText(referral.getAppointmentId() != null ?
                referral.getAppointmentId() : "");
        reasonArea.setText(referral.getReferralReason() != null ?
                referral.getReferralReason() : "");
        clinicalSummaryArea.setText(referral.getClinicalSummary() != null ?
                referral.getClinicalSummary() : "");
        notesArea.setText(referral.getNotes() != null ?
                referral.getNotes() : "");
    }

    private void save() {
        try {
            // Extract IDs from combo selections
            String patientId = extractId((String) patientCombo.getSelectedItem());
            String referringClinicianId = extractId((String) referringClinicianCombo.getSelectedItem());
            String referredToClinicianId = extractId((String) referredToClinicianCombo.getSelectedItem());
            String referringFacilityId = extractId((String) referringFacilityCombo.getSelectedItem());
            String referredToFacilityId = extractId((String) referredToFacilityCombo.getSelectedItem());

            // Validate required fields
            if (patientId == null) {
                showValidationError("Please select a Patient.");
                return;
            }
            if (referringClinicianId == null) {
                showValidationError("Please select a Referring Clinician.");
                return;
            }
            if (referredToFacilityId == null) {
                showValidationError("Please select a Referred To Facility.");
                return;
            }
            if (reasonArea.getText().trim().isEmpty()) {
                showValidationError("Please enter the Referral Reason.");
                return;
            }

            String id = referralIdField.getText().trim();
            LocalDate now = LocalDate.now();

            // Create referral object
            referral = new Referral(
                    id,
                    patientId,
                    referringClinicianId,
                    referredToClinicianId,
                    referringFacilityId,
                    referredToFacilityId,
                    referral == null ? now : referral.getReferralDate(),
                    (String) urgencyCombo.getSelectedItem(),
                    reasonArea.getText().trim(),
                    clinicalSummaryArea.getText().trim(),
                    investigationsField.getText().trim(),
                    (String) statusCombo.getSelectedItem(),
                    appointmentField.getText().trim(),
                    notesArea.getText().trim(),
                    referral == null ? now : referral.getCreatedDate(),
                    now
            );

            confirmed = true;
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving referral: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Referral getReferral() {
        return referral;
    }
}