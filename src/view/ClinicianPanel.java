package view;

import controller.ClinicianController;
import model.Clinician;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ClinicianPanel extends JPanel {

    private ClinicianController ctrl;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchBox;
    private JLabel countLabel;

    public ClinicianPanel(ClinicianController ctrl) {
        this.ctrl = ctrl;
        buildUI();
        loadTable();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // top section with two rows
        JPanel topSection = new JPanel(new BorderLayout(5, 5));

        // Left side panel for search controls
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        // First row: search bar
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

        // Second row: filter button
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton filterBtn = new JButton("Filter by Specialization");
        filterBtn.addActionListener(e -> filterBySpec());
        filterRow.add(filterBtn);

        leftPanel.add(searchRow);
        leftPanel.add(filterRow);

        topSection.add(leftPanel, BorderLayout.WEST);

        // buttons on right
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton addBtn = new JButton("Add Clinician");
        JButton editBtn = new JButton("Edit Clinician");
        JButton delBtn = new JButton("Delete Clinician");
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

        // table setup
        String[] cols = {"Clinician ID", "Name", "Specialization",
                "License No", "Department", "Facility ID", "Status"};

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

        // bottom counter
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countLabel = new JLabel("Total Clinicians: 0");
        bottom.add(countLabel);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadTable() {
        populateTable(ctrl.getAllClinicians());
    }

    private void populateTable(List<Clinician> list) {
        model.setRowCount(0);

        for (Clinician c : list) {
            model.addRow(new Object[]{
                    c.getClinicianId(),
                    c.getFullName(),
                    c.getSpecialization(),
                    c.getLicenseNumber(),
                    c.getDepartment(),
                    c.getFacilityId(),
                    c.getEmploymentStatus()
            });
        }
        updateCount();
    }

    private void doSearch() {
        String q = searchBox.getText().trim().toLowerCase();

        if (q.isEmpty()) {
            loadTable();
            return;
        }

        model.setRowCount(0);

        for (Clinician c : ctrl.getAllClinicians()) {
            // check name, id, specialization, or license
            boolean match = c.getFullName().toLowerCase().contains(q) ||
                    c.getClinicianId().toLowerCase().contains(q) ||
                    c.getSpecialization().toLowerCase().contains(q) ||
                    c.getLicenseNumber().toLowerCase().contains(q);

            if (match) {
                model.addRow(new Object[]{
                        c.getClinicianId(),
                        c.getFullName(),
                        c.getSpecialization(),
                        c.getLicenseNumber(),
                        c.getDepartment(),
                        c.getFacilityId(),
                        c.getEmploymentStatus()
                });
            }
        }
        updateCount();
    }

    private void onAdd() {
        ClinicianDialog dlg = new ClinicianDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null, ctrl);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            ctrl.addClinician(dlg.getClinician());
            loadTable();
            showMsg("Clinician added!");
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a clinician first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        Clinician clinician = ctrl.getClinicianById(id);

        ClinicianDialog dlg = new ClinicianDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), clinician, ctrl);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            ctrl.updateClinician(dlg.getClinician());
            loadTable();
            showMsg("Updated!");
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a clinician first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete clinician: " + name + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            ctrl.deleteClinician(id);
            loadTable();
            showMsg("Deleted!");
        }
    }

    private void filterBySpec() {
        // get unique specializations
        Set<String> specs = new HashSet<>();
        for (Clinician c : ctrl.getAllClinicians()) {
            specs.add(c.getSpecialization());
        }

        String[] specArr = specs.toArray(new String[0]);
        Arrays.sort(specArr);

        if (specArr.length == 0) {
            showMsg("No specializations found.");
            return;
        }

        // show picker dialog
        String picked = (String) JOptionPane.showInputDialog(
                this,
                "Pick a specialization:",
                "Filter",
                JOptionPane.QUESTION_MESSAGE,
                null,
                specArr,
                specArr[0]
        );

        if (picked != null) {
            List<Clinician> filtered = ctrl.getCliniciansBySpecialization(picked);
            populateTable(filtered);
            showMsg("Found " + filtered.size() + " clinicians in: " + picked);
        }
    }

    private void showDetails() {
        int row = table.getSelectedRow();

        if (row == -1) {
            showWarning("Select a clinician first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        Clinician c = ctrl.getClinicianById(id);

        if (c == null) {
            showWarning("Clinician not found!");
            return;
        }

        // build details text
        StringBuilder sb = new StringBuilder();

        sb.append("=====================================\n");
        sb.append("        CLINICIAN DETAILS\n");
        sb.append("=====================================\n\n");

        sb.append(">> PROFESSIONAL INFO\n");
        sb.append("ID:             ").append(c.getClinicianId()).append("\n");
        sb.append("Name:           ").append(c.getFullName()).append("\n");
        sb.append("Title:          ").append(c.getTitle()).append("\n");
        sb.append("Specialization: ").append(c.getSpecialization()).append("\n");
        sb.append("License:        ").append(c.getLicenseNumber()).append("\n");
        sb.append("Department:     ").append(c.getDepartment()).append("\n\n");

        sb.append(">> CONTACT & WORK\n");
        sb.append("Facility ID:    ").append(c.getFacilityId()).append("\n");
        sb.append("Workplace:      ").append(c.getWorkplaceType()).append("\n");
        sb.append("Email:          ").append(c.getEmail()).append("\n");
        sb.append("Phone:          ").append(c.getPhoneNumber()).append("\n");
        sb.append("Status:         ").append(c.getEmploymentStatus()).append("\n");
        sb.append("Start Date:     ").append(c.getStartDate()).append("\n");
        sb.append("=====================================\n");

        // display in scrollable area
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(420, 380));

        JOptionPane.showMessageDialog(this, scroll, "Clinician Details", JOptionPane.PLAIN_MESSAGE);
    }

    private void updateCount() {
        countLabel.setText("Total Clinicians: " + model.getRowCount());
    }

    // quick helpers
    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}