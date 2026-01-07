package view;

import controller.PatientController;
import model.Patient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PatientPanel extends JPanel {

    private PatientController ctrl;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchBox;
    private JLabel countLabel;

    public PatientPanel(PatientController controller) {
        ctrl = controller;
        initComponents();
        loadTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top section with search and buttons
        JPanel topSection = new JPanel(new BorderLayout());

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(new JLabel("Search:"));
        searchBox = new JTextField(20);
        searchBox.addActionListener(e -> doSearch());
        searchBar.add(searchBox);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> doSearch());
        searchBar.add(searchBtn);

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            searchBox.setText("");
            loadTable();
        });
        searchBar.add(clearBtn);

        topSection.add(searchBar, BorderLayout.WEST);

        // Action buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton addBtn = new JButton("Add Patient");
        JButton editBtn = new JButton("Edit Patient");
        JButton delBtn = new JButton("Delete Patient");
        JButton detailsBtn = new JButton("View Details");

        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        delBtn.addActionListener(e -> onDelete());
        detailsBtn.addActionListener(e -> showDetails());

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(detailsBtn);

        topSection.add(btnPanel, BorderLayout.EAST);
        add(topSection, BorderLayout.NORTH);

        // Table setup
        String[] cols = {"Patient ID", "Name", "NHS Number", "DOB", "Gender",
                "Phone", "Email", "Address"};

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
        countLabel = new JLabel("Total Patients: 0");
        bottom.add(countLabel);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadTable() {
        model.setRowCount(0);
        List<Patient> list = ctrl.getAllPatients();

        for (Patient p : list) {
            addRowToTable(p);
        }
        updateCount();
    }

    private void addRowToTable(Patient p) {
        Object[] row = {
                p.getPatientId(),
                p.getFullName(),
                p.getNhsNumber(),
                p.getDateOfBirth(),
                p.getGender(),
                p.getPhoneNumber(),
                p.getEmail(),
                p.getAddress() + ", " + p.getPostcode()
        };
        model.addRow(row);
    }

    private void doSearch() {
        String q = searchBox.getText().trim();

        if (q.isEmpty()) {
            loadTable();
            return;
        }

        model.setRowCount(0);
        List<Patient> results = ctrl.searchPatients(q);

        for (Patient p : results) {
            addRowToTable(p);
        }
        updateCount();
    }

    private void onAdd() {
        PatientDialog dlg = new PatientDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null, ctrl);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            ctrl.addPatient(dlg.getPatient());
            loadTable();
            showInfo("Patient added!");
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a patient first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        Patient patient = ctrl.getPatientById(id);

        PatientDialog dlg = new PatientDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), patient, ctrl);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            ctrl.updatePatient(dlg.getPatient());
            loadTable();
            showInfo("Updated!");
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a patient first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete patient: " + name + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            ctrl.deletePatient(id);
            loadTable();
            showInfo("Deleted!");
        }
    }

    private void showDetails() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a patient first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        Patient p = ctrl.getPatientById(id);

        StringBuilder details = new StringBuilder();
        details.append("=====================================\n");
        details.append(" PATIENT DETAILS\n");
        details.append("=====================================\n\n");

        details.append(">> BASIC INFO\n");
        details.append("ID: ").append(p.getPatientId()).append("\n");
        details.append("Name: ").append(p.getFullName()).append("\n");
        details.append("NHS#: ").append(p.getNhsNumber()).append("\n");
        details.append("DOB: ").append(p.getDateOfBirth()).append("\n");
        details.append("Age: ").append(p.getAge()).append(" years\n");
        details.append("Gender: ").append(p.getGender()).append("\n\n");

        details.append(">> CONTACT\n");
        details.append("Phone: ").append(p.getPhoneNumber()).append("\n");
        details.append("Email: ").append(p.getEmail()).append("\n");
        details.append("Address: ").append(p.getAddress()).append("\n");
        details.append("Postcode: ").append(p.getPostcode()).append("\n\n");

        details.append(">> EMERGENCY CONTACT\n");
        details.append("Name: ").append(p.getEmergencyContactName()).append("\n");
        details.append("Phone: ").append(p.getEmergencyContactPhone()).append("\n\n");

        details.append(">> OTHER\n");
        details.append("GP Surgery: ").append(p.getGpSurgeryId()).append("\n");
        details.append("Registered: ").append(p.getRegistrationDate()).append("\n");
        details.append("=====================================\n");

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(450, 450));

        JOptionPane.showMessageDialog(this, scroll,
                "Details", JOptionPane.PLAIN_MESSAGE);
    }

    private void updateCount() {
        countLabel.setText("Total Patients: " + model.getRowCount());
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