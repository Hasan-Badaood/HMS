package view;

import controller.*;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PrescriptionPanel extends JPanel {

    private PrescriptionController ctrl;
    private PatientController patCtrl;
    private ClinicianController clinCtrl;

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchBox;
    private JLabel countLabel;

    public PrescriptionPanel(PrescriptionController pc,
                             PatientController patc,
                             ClinicianController cc) {
        ctrl = pc;
        patCtrl = patc;
        clinCtrl = cc;
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

        JButton addBtn = new JButton("Add Prescription");
        JButton editBtn = new JButton("Edit Prescription");
        JButton delBtn = new JButton("Delete Prescription");
        JButton viewBtn = new JButton("View Details");

        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        delBtn.addActionListener(e -> onDelete());
        viewBtn.addActionListener(e -> showDetails());

        btnRow.add(addBtn);
        btnRow.add(editBtn);
        btnRow.add(delBtn);
        btnRow.add(viewBtn);

        topSection.add(btnRow);
        add(topSection, BorderLayout.NORTH);

        // Table setup
        String[] cols = {"Prescription ID", "Patient", "Clinician", "Medication",
                "Dosage", "Frequency", "Status", "Date"};

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
        countLabel = new JLabel("Total Prescriptions: 0");
        bottom.add(countLabel);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadTable() {
        model.setRowCount(0);
        List<Prescription> list = ctrl.getAllPrescriptions();

        for (Prescription p : list) {
            addRowToTable(p);
        }
        updateCount();
    }

    private void addRowToTable(Prescription p) {
        Patient pat = patCtrl.getPatientById(p.getPatientId());
        Clinician clin = clinCtrl.getClinicianById(p.getClinicianId());

        Object[] row = {
                p.getPrescriptionId(),
                pat != null ? pat.getFullName() : p.getPatientId(),
                clin != null ? clin.getFullName() : p.getClinicianId(),
                p.getMedicationName(),
                p.getDosage(),
                p.getFrequency(),
                p.getStatus(),
                p.getPrescriptionDate()
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

        for (Prescription p : ctrl.getAllPrescriptions()) {
            Patient pat = patCtrl.getPatientById(p.getPatientId());
            Clinician clin = clinCtrl.getClinicianById(p.getClinicianId());

            // Search across multiple fields
            boolean match = p.getPrescriptionId().toLowerCase().contains(q) ||
                    p.getMedicationName().toLowerCase().contains(q) ||
                    p.getStatus().toLowerCase().contains(q) ||
                    (pat != null && pat.getFullName().toLowerCase().contains(q)) ||
                    (clin != null && clin.getFullName().toLowerCase().contains(q));

            if (match) {
                addRowToTable(p);
            }
        }
        updateCount();
    }

    private void onAdd() {
        PrescriptionDialog dlg = new PrescriptionDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                null, patCtrl, clinCtrl, ctrl);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            Prescription newRx = dlg.getPrescription();
            ctrl.addPrescription(newRx);
            loadTable();
            showInfo("Prescription added!\nE-prescription saved to output_prescriptions.txt");
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a prescription first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        Prescription prescription = ctrl.getPrescriptionById(id);

        PrescriptionDialog dlg = new PrescriptionDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                prescription, patCtrl, clinCtrl, ctrl);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            ctrl.updatePrescription(dlg.getPrescription());
            loadTable();
            showInfo("Prescription updated!");
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a prescription first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        String medication = (String) model.getValueAt(row, 3);
        String patient = (String) model.getValueAt(row, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete prescription for " + medication + "\nPatient: " + patient + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            ctrl.deletePrescription(id);
            loadTable();
            showInfo("Prescription deleted!");
        }
    }

    private void showDetails() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a prescription first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        Prescription rx = ctrl.getPrescriptionById(id);

        JTextArea textArea = new JTextArea(rx.generateEPrescription());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scroll,
                "Prescription Details", JOptionPane.PLAIN_MESSAGE);
    }

    private void updateCount() {
        countLabel.setText("Total Prescriptions: " + model.getRowCount());
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
}