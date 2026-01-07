package view;

import controller.ClinicianController;
import controller.PatientController;
import controller.PrescriptionController;
import model.Clinician;
import model.Patient;
import model.Prescription;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class PrescriptionDialog extends JDialog {
    private Prescription prescription;
    private boolean confirmed = false;
    private PatientController patientController;
    private ClinicianController clinicianController;
    private PrescriptionController prescriptionController;

    // UI Components
    private JTextField prescriptionIdField;
    private JComboBox<String> patientCombo, clinicianCombo, statusCombo;
    private JTextField medicationField, dosageField, frequencyField;
    private JTextField durationField, quantityField, pharmacyField;
    private JTextArea instructionsArea;

    public PrescriptionDialog(Frame owner, Prescription prescription,
                              PatientController patientController,
                              ClinicianController clinicianController,
                              PrescriptionController prescriptionController) {
        super(owner, prescription == null ? "Add Prescription" : "Edit Prescription", true);
        this.prescription = prescription;
        this.patientController = patientController;
        this.clinicianController = clinicianController;
        this.prescriptionController = prescriptionController;

        setupUI();

        if (prescription != null) {
            fillFields();
        } else {
            // Generate ID for new prescription
            prescriptionIdField.setText(generateNextPrescriptionId());
        }

        // Set dialog size and position
        pack();
        setSize(new Dimension(500, 650));
        setLocationRelativeTo(owner);
    }

    private String generateNextPrescriptionId() {
        if (prescriptionController == null || prescriptionController.getAllPrescriptions().isEmpty()) {
            return "RX001";
        }

        int maxNumber = 0;
        for (Prescription p : prescriptionController.getAllPrescriptions()) {
            String id = p.getPrescriptionId();
            if (id != null && id.startsWith("RX")) {
                try {
                    // Handle both "RX001" and "RX-001" formats
                    String numPart = id.substring(2);
                    if (numPart.startsWith("-")) {
                        numPart = numPart.substring(1);
                    }
                    int num = Integer.parseInt(numPart);
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                }
            }
        }

        return String.format("RX%03d", maxNumber + 1);
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(10, 2, 5, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Initialize components
        prescriptionIdField = new JTextField();
        prescriptionIdField.setEditable(false);
        prescriptionIdField.setBackground(Color.LIGHT_GRAY);

        patientCombo = new JComboBox<>();
        populatePatientCombo();

        clinicianCombo = new JComboBox<>();
        populateClinicianCombo();

        medicationField = new JTextField();
        dosageField = new JTextField();
        frequencyField = new JTextField();
        durationField = new JTextField();
        quantityField = new JTextField();
        pharmacyField = new JTextField();

        String[] statuses = {"Issued", "Collected"};
        statusCombo = new JComboBox<>(statuses);

        // Add fields to form
        formPanel.add(new JLabel("Prescription ID:"));
        formPanel.add(prescriptionIdField);

        formPanel.add(new JLabel("Patient:"));
        formPanel.add(patientCombo);

        formPanel.add(new JLabel("Clinician:"));
        formPanel.add(clinicianCombo);

        formPanel.add(new JLabel("Medication Name:"));
        formPanel.add(medicationField);

        formPanel.add(new JLabel("Dosage (e.g., 500mg):"));
        formPanel.add(dosageField);

        formPanel.add(new JLabel("Frequency (e.g., Twice daily):"));
        formPanel.add(frequencyField);

        formPanel.add(new JLabel("Duration (Days):"));
        formPanel.add(durationField);

        formPanel.add(new JLabel("Quantity (Units):"));
        formPanel.add(quantityField);

        formPanel.add(new JLabel("Pharmacy Name:"));
        formPanel.add(pharmacyField);

        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusCombo);

        add(formPanel, BorderLayout.NORTH);

        // Instructions section
        JPanel instructionsPanel = new JPanel(new BorderLayout());
        instructionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 0, 10),
                BorderFactory.createTitledBorder("Instructions / Notes")
        ));

        instructionsArea = new JTextArea(5, 20);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsPanel.add(new JScrollPane(instructionsArea), BorderLayout.CENTER);

        add(instructionsPanel, BorderLayout.CENTER);

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

    private void populateClinicianCombo() {
        for (Clinician c : clinicianController.getAllClinicians()) {
            clinicianCombo.addItem(c.getClinicianId() + " - " + c.getFullName());
        }
    }

    private void fillFields() {
        prescriptionIdField.setText(prescription.getPrescriptionId());

        // Select patient from combo
        String pId = prescription.getPatientId();
        for (int i = 0; i < patientCombo.getItemCount(); i++) {
            if (patientCombo.getItemAt(i).startsWith(pId)) {
                patientCombo.setSelectedIndex(i);
                break;
            }
        }

        // Select clinician from combo
        String cId = prescription.getClinicianId();
        for (int i = 0; i < clinicianCombo.getItemCount(); i++) {
            if (clinicianCombo.getItemAt(i).startsWith(cId)) {
                clinicianCombo.setSelectedIndex(i);
                break;
            }
        }

        medicationField.setText(prescription.getMedicationName());
        dosageField.setText(prescription.getDosage());
        frequencyField.setText(prescription.getFrequency());
        durationField.setText(String.valueOf(prescription.getDurationDays()));
        quantityField.setText(String.valueOf(prescription.getQuantity()));
        pharmacyField.setText(prescription.getPharmacyName());
        statusCombo.setSelectedItem(prescription.getStatus());
        instructionsArea.setText(prescription.getInstructions());
    }

    private void save() {
        try {
            // Check required fields
            if (medicationField.getText().trim().isEmpty() ||
                    durationField.getText().trim().isEmpty() ||
                    quantityField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please fill in Medication, Duration, and Quantity.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Parse numbers
            int duration = Integer.parseInt(durationField.getText().trim());
            String quantity = quantityField.getText().trim();

            // Extract IDs from combo selections
            String selectedPatient = (String) patientCombo.getSelectedItem();
            String selectedClinician = (String) clinicianCombo.getSelectedItem();

            if (selectedPatient == null || selectedClinician == null) {
                JOptionPane.showMessageDialog(this, "Please select a Patient and Clinician.");
                return;
            }

            String patientId = selectedPatient.split(" - ")[0];
            String clinicianId = selectedClinician.split(" - ")[0];

            // Use generated or existing ID
            String id = prescriptionIdField.getText().trim();

            // Create prescription object
            prescription = new Prescription(
                    id,
                    patientId,
                    clinicianId,
                    null,
                    LocalDate.now(),
                    medicationField.getText().trim(),
                    dosageField.getText().trim(),
                    frequencyField.getText().trim(),
                    duration,
                    quantity,
                    instructionsArea.getText().trim(),
                    pharmacyField.getText().trim(),
                    (String) statusCombo.getSelectedItem(),
                    LocalDate.now(),
                    null
            );

            confirmed = true;
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Duration and Quantity must be valid numbers.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving prescription: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Prescription getPrescription() {
        return prescription;
    }
}