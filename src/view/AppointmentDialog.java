package view;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import controller.*;
import model.*;

class AppointmentDialog extends JDialog {

    private Appointment appt;
    private boolean confirmed = false;

    // Controllers
    private AppointmentController apptCtrl;
    private PatientController patCtrl;
    private ClinicianController clinCtrl;
    private FacilityController facCtrl;

    // Form fields
    private JTextField idField, dateField, timeField, durationField, reasonField;
    private JComboBox<String> patientBox, clinicianBox, facilityBox, typeBox, statusBox;
    private JTextArea notesArea;

    public AppointmentDialog(Frame owner, Appointment appt,
                             AppointmentController ac,
                             PatientController pc,
                             ClinicianController cc,
                             FacilityController fc) {
        super(owner, appt == null ? "Add Appointment" : "Edit Appointment", true);
        this.appt = appt;
        apptCtrl = ac;
        patCtrl = pc;
        clinCtrl = cc;
        facCtrl = fc;

        buildForm();

        if (appt != null) {
            loadData();
        } else {
            // Generate ID for new appointment
            idField.setText(generateNextAppointmentId());
        }
    }

    private String generateNextAppointmentId() {
        if (apptCtrl == null || apptCtrl.getAllAppointments().isEmpty()) {
            return "A001";
        }

        int maxNumber = 0;
        for (Appointment a : apptCtrl.getAllAppointments()) {
            String id = a.getAppointmentId();
            if (id != null && id.startsWith("A")) {
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

        return String.format("A%03d", maxNumber + 1);
    }

    private void buildForm() {
        setLayout(new BorderLayout(10, 10));
        setSize(550, 600);
        setLocationRelativeTo(getOwner());

        // Main form panel
        JPanel form = new JPanel(new GridLayout(10, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ID field (read-only)
        idField = new JTextField();
        idField.setEditable(false);
        idField.setBackground(Color.LIGHT_GRAY);

        // Populate patient dropdown
        patientBox = new JComboBox<>();
        for (Patient p : patCtrl.getAllPatients()) {
            patientBox.addItem(p.getPatientId() + " - " + p.getFullName() + " (NHS: " + p.getNhsNumber() + ")");
        }

        // Populate clinician dropdown
        clinicianBox = new JComboBox<>();
        for (Clinician c : clinCtrl.getAllClinicians()) {
            clinicianBox.addItem(c.getClinicianId() + " - Dr. " + c.getFullName() + " (" + c.getSpecialization() + ")");
        }

        // Populate facility dropdown
        facilityBox = new JComboBox<>();
        for (Facility f : facCtrl.getAllFacilities()) {
            facilityBox.addItem(f.getFacilityId() + " - " + f.getFacilityName());
        }

        // Date/time fields with default values
        dateField = new JTextField(LocalDate.now().toString());
        timeField = new JTextField("09:00");
        durationField = new JTextField("30");

        String[] types = {"Consultation", "Follow-up", "Emergency", "Routine", "Check-up", "Specialist"};
        String[] statuses = {"Scheduled", "Cancelled"};
        typeBox = new JComboBox<>(types);
        statusBox = new JComboBox<>(statuses);

        reasonField = new JTextField();

        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        // Add all fields to form
        form.add(new JLabel("Appointment ID:"));
        form.add(idField);
        form.add(new JLabel("Patient:"));
        form.add(patientBox);
        form.add(new JLabel("Clinician:"));
        form.add(clinicianBox);
        form.add(new JLabel("Facility:"));
        form.add(facilityBox);
        form.add(new JLabel("Date (yyyy-mm-dd):"));
        form.add(dateField);
        form.add(new JLabel("Time (HH:MM):"));
        form.add(timeField);
        form.add(new JLabel("Duration (mins):"));
        form.add(durationField);
        form.add(new JLabel("Type:"));
        form.add(typeBox);
        form.add(new JLabel("Status:"));
        form.add(statusBox);
        form.add(new JLabel("Reason:"));
        form.add(reasonField);

        add(form, BorderLayout.NORTH);

        // Notes section
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBorder(BorderFactory.createTitledBorder("Notes"));
        notesPanel.add(new JScrollPane(notesArea), BorderLayout.CENTER);
        add(notesPanel, BorderLayout.CENTER);

        // Action buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> onSave());

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        idField.setText(appt.getAppointmentId());
        dateField.setText(appt.getAppointmentDate().toString());
        timeField.setText(appt.getAppointmentTime());
        durationField.setText(String.valueOf(appt.getDurationMinutes()));
        typeBox.setSelectedItem(appt.getAppointmentType());
        statusBox.setSelectedItem(appt.getStatus());
        reasonField.setText(appt.getReasonForVisit());
        notesArea.setText(appt.getNotes());

        // Select matching items in dropdowns
        selectComboItem(patientBox, appt.getPatientId());
        selectComboItem(clinicianBox, appt.getClinicianId());
        selectComboItem(facilityBox, appt.getFacilityId());
    }

    // Find and select combo item by ID prefix
    private void selectComboItem(JComboBox<String> combo, String id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).startsWith(id)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    // Extract ID from combo selection (e.g., "P001 - John Doe" -> "P001")
    private String extractId(JComboBox<String> combo) {
        String selected = (String) combo.getSelectedItem();
        return selected.split(" - ")[0];
    }

    private void onSave() {
        try {
            // Use the auto-generated or existing ID
            String id = idField.getText().trim();

            appt = new Appointment(
                    id,
                    extractId(patientBox),
                    extractId(clinicianBox),
                    extractId(facilityBox),
                    LocalDate.parse(dateField.getText().trim()),
                    timeField.getText().trim(),
                    Integer.parseInt(durationField.getText().trim()),
                    (String) typeBox.getSelectedItem(),
                    (String) statusBox.getSelectedItem(),
                    reasonField.getText().trim(),
                    notesArea.getText().trim()
            );

            confirmed = true;
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save appointment. Please check:\n" +
                            "- Date format (yyyy-mm-dd)\n" +
                            "- Time format (HH:MM)\n" +
                            "- Duration is a valid number\n\n" +
                            "Error: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Appointment getAppointment() {
        return appt;
    }
}