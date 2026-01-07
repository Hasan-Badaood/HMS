package view;

import controller.*;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReferralPanel extends JPanel {

    private ReferralController ctrl;
    private PatientController patCtrl;
    private ClinicianController clinCtrl;
    private FacilityController facCtrl;

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchBox;
    private JLabel countLabel;

    public ReferralPanel(ReferralController rc, PatientController pc,
                         ClinicianController cc, FacilityController fc) {
        ctrl = rc;
        patCtrl = pc;
        clinCtrl = cc;
        facCtrl = fc;
        initComponents();
        loadTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top section
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));

        // Search bar
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchRow.add(new JLabel("Search:"));
        searchBox = new JTextField(20);
        searchBox.addActionListener(e -> doSearch());
        searchRow.add(searchBox);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> doSearch());
        searchRow.add(searchBtn);

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            searchBox.setText("");
            loadTable();
        });
        searchRow.add(clearBtn);

        topSection.add(searchRow);

        // Action buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addBtn = new JButton("Add Referral");
        JButton editBtn = new JButton("Edit Referral");
        JButton delBtn = new JButton("Delete Referral");
        JButton viewBtn = new JButton("View Details");
        JButton sendBtn = new JButton("Send Referral");

        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        delBtn.addActionListener(e -> onDelete());
        viewBtn.addActionListener(e -> showDetails());
        sendBtn.addActionListener(e -> onSendReferral());

        btnRow.add(addBtn);
        btnRow.add(editBtn);
        btnRow.add(delBtn);
        btnRow.add(viewBtn);
        btnRow.add(sendBtn);

        topSection.add(btnRow);
        add(topSection, BorderLayout.NORTH);

        // Table setup
        String[] cols = {"Referral ID", "Patient", "Referring Clinician",
                "Target Facility", "Urgency", "Status", "Date"};

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom counter
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countLabel = new JLabel("Total Referrals: 0");
        bottom.add(countLabel);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadTable() {
        model.setRowCount(0);
        List<Referral> list = ctrl.getAllReferrals();

        for (Referral r : list) {
            addRowToTable(r);
        }
        updateCount();
    }

    private void addRowToTable(Referral r) {
        Patient pat = patCtrl.getPatientById(r.getPatientId());
        Clinician clin = clinCtrl.getClinicianById(r.getReferringClinicianId());

        // Get facility name from ID
        String facilityDisplay = r.getReferredToFacilityId();
        Facility fac = facCtrl.getFacilityById(r.getReferredToFacilityId());
        if (fac != null) {
            facilityDisplay = fac.getFacilityName();
        }

        Object[] row = {
                r.getReferralId(),
                pat != null ? pat.getFullName() : r.getPatientId(),
                clin != null ? clin.getFullName() : r.getReferringClinicianId(),
                facilityDisplay,
                r.getUrgencyLevel(),
                r.getStatus(),
                r.getReferralDate()
        };
        model.addRow(row);
    }

    private void doSearch() {
        String q = searchBox.getText().trim().toLowerCase();

        if (q.isEmpty()) {
            loadTable();
            return;
        }

        model.setRowCount(0);

        for (Referral r : ctrl.getAllReferrals()) {
            Patient pat = patCtrl.getPatientById(r.getPatientId());
            Clinician clin = clinCtrl.getClinicianById(r.getReferringClinicianId());
            Facility fac = facCtrl.getFacilityById(r.getReferredToFacilityId());

            String patName = (pat != null) ? pat.getFullName().toLowerCase() : "";
            String clinName = (clin != null) ? clin.getFullName().toLowerCase() : "";
            String facName = (fac != null) ? fac.getFacilityName().toLowerCase() : "";

            // Null-safe field checks
            String refId = r.getReferralId() != null ? r.getReferralId().toLowerCase() : "";
            String status = r.getStatus() != null ? r.getStatus().toLowerCase() : "";
            String urgency = r.getUrgencyLevel() != null ? r.getUrgencyLevel().toLowerCase() : "";
            String reason = r.getReferralReason() != null ? r.getReferralReason().toLowerCase() : "";

            // Check if query matches any field
            if (refId.contains(q) ||
                    patName.contains(q) ||
                    clinName.contains(q) ||
                    facName.contains(q) ||
                    status.contains(q) ||
                    urgency.contains(q) ||
                    reason.contains(q)) {

                addRowToTable(r);
            }
        }
        updateCount();
    }

    private void onAdd() {
        ReferralDialog dlg = new ReferralDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                null, patCtrl, clinCtrl, facCtrl, ctrl);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            Referral newRef = dlg.getReferral();
            ctrl.addReferral(newRef);
            loadTable();
            showInfo("Referral created!\nID: " + newRef.getReferralId());
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a referral first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        Referral referral = ctrl.getReferralById(id);

        ReferralDialog dlg = new ReferralDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                referral, patCtrl, clinCtrl, facCtrl, ctrl);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            Referral updated = dlg.getReferral();
            ctrl.updateReferral(updated);
            loadTable();
            showInfo("Referral updated!");
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a referral first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        String patient = (String) model.getValueAt(row, 1);
        String facility = (String) model.getValueAt(row, 3);

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete referral for " + patient + "\nTarget Facility: " + facility + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            ctrl.deleteReferral(id);
            loadTable();
            showInfo("Referral deleted!");
        }
    }

    private void onSendReferral() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a referral first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        Referral ref = ctrl.getReferralById(id);

        if (ref == null) {
            showError("Referral not found!");
            return;
        }

        String status = ref.getStatus();
        if (!"Pending".equalsIgnoreCase(status) && !"New".equalsIgnoreCase(status)) {
            showWarning("Only Pending or New referrals can be sent.\nCurrent status: " + status);
            return;
        }

        // Get facility list
        List<Facility> facilities = facCtrl.getAllFacilities();
        String[] facilityNames = new String[facilities.size()];
        for (int i = 0; i < facilities.size(); i++) {
            facilityNames[i] = facilities.get(i).getFacilityId() + " - " + facilities.get(i).getFacilityName();
        }

        String selected = (String) JOptionPane.showInputDialog(this,
                "Select target facility:",
                "Send Referral",
                JOptionPane.QUESTION_MESSAGE,
                null,
                facilityNames,
                facilityNames.length > 0 ? facilityNames[0] : null);

        if (selected != null) {
            String facilityId = selected.split(" - ")[0];
            boolean success = ctrl.sendReferral(id, facilityId);

            if (success) {
                loadTable();
                showInfo("Referral sent successfully!\nDocuments generated:\n" +
                        "- output_referrals.txt\n" +
                        "- output_emails.txt\n" +
                        "- output_ehr_updates.txt");
            } else {
                showError("Failed to send referral.");
            }
        }
    }

    private void showDetails() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a referral first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        Referral ref = ctrl.getReferralById(id);

        if (ref == null) {
            showError("Referral not found!");
            return;
        }

        // Fetch related data
        Patient pat = patCtrl.getPatientById(ref.getPatientId());
        Clinician refClin = clinCtrl.getClinicianById(ref.getReferringClinicianId());
        Clinician targClin = ref.getReferredToClinicianId() != null ?
                clinCtrl.getClinicianById(ref.getReferredToClinicianId()) : null;
        Facility refFac = ref.getReferringFacilityId() != null ?
                facCtrl.getFacilityById(ref.getReferringFacilityId()) : null;
        Facility targFac = ref.getReferredToFacilityId() != null ?
                facCtrl.getFacilityById(ref.getReferredToFacilityId()) : null;

        StringBuilder details = new StringBuilder();
        details.append("=====================================\n");
        details.append("         REFERRAL DETAILS\n");
        details.append("=====================================\n\n");

        details.append(">> REFERRAL INFO\n");
        details.append("ID:          ").append(ref.getReferralId()).append("\n");
        details.append("Date:        ").append(ref.getReferralDate()).append("\n");
        details.append("Urgency:     ").append(ref.getUrgencyLevel()).append("\n");
        details.append("Status:      ").append(ref.getStatus()).append("\n");
        details.append("Created:     ").append(ref.getCreatedDate()).append("\n");
        details.append("Updated:     ").append(ref.getLastUpdated()).append("\n\n");

        details.append(">> PATIENT\n");
        if (pat != null) {
            details.append("Name:        ").append(pat.getFullName()).append("\n");
            details.append("NHS#:        ").append(pat.getNhsNumber()).append("\n");
            details.append("DOB:         ").append(pat.getDateOfBirth()).append("\n");
        } else {
            details.append("ID:          ").append(ref.getPatientId()).append("\n");
        }
        details.append("\n");

        details.append(">> REFERRING FROM\n");
        if (refClin != null) {
            details.append("Clinician:   Dr. ").append(refClin.getFullName()).append("\n");
            details.append("Specialty:   ").append(refClin.getSpecialization()).append("\n");
        } else {
            details.append("Clinician ID: ").append(ref.getReferringClinicianId()).append("\n");
        }
        if (refFac != null) {
            details.append("Facility:    ").append(refFac.getFacilityName()).append("\n");
        } else if (ref.getReferringFacilityId() != null) {
            details.append("Facility ID: ").append(ref.getReferringFacilityId()).append("\n");
        }
        details.append("\n");

        details.append(">> REFERRED TO\n");
        if (targFac != null) {
            details.append("Facility:    ").append(targFac.getFacilityName()).append("\n");
            details.append("Address:     ").append(targFac.getAddress()).append("\n");
            details.append("Phone:       ").append(targFac.getPhoneNumber()).append("\n");
        } else if (ref.getReferredToFacilityId() != null) {
            details.append("Facility ID: ").append(ref.getReferredToFacilityId()).append("\n");
        }
        if (targClin != null) {
            details.append("Clinician:   Dr. ").append(targClin.getFullName()).append("\n");
        } else if (ref.getReferredToClinicianId() != null && !ref.getReferredToClinicianId().isEmpty()) {
            details.append("Clinician ID: ").append(ref.getReferredToClinicianId()).append("\n");
        }
        details.append("\n");

        details.append(">> CLINICAL DETAILS\n");
        details.append("Reason:\n").append(nullSafe(ref.getReferralReason())).append("\n\n");
        details.append("Clinical Summary:\n").append(nullSafe(ref.getClinicalSummary())).append("\n\n");
        details.append("Requested Investigations:\n").append(nullSafe(ref.getRequestedInvestigations())).append("\n\n");
        details.append("Notes:\n").append(nullSafe(ref.getNotes())).append("\n");

        if (ref.getAppointmentId() != null && !ref.getAppointmentId().isEmpty()) {
            details.append("\n>> APPOINTMENT\n");
            details.append("Appointment ID: ").append(ref.getAppointmentId()).append("\n");
        }

        details.append("\n=====================================\n");

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        textArea.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(550, 600));

        JOptionPane.showMessageDialog(this, scroll,
                "Referral Details - " + ref.getReferralId(), JOptionPane.PLAIN_MESSAGE);
    }

    private String nullSafe(String value) {
        return (value != null && !value.isEmpty()) ? value : "N/A";
    }

    private void updateCount() {
        countLabel.setText("Total Referrals: " + model.getRowCount());
    }

    // Helper methods
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning",
                JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}