package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import controller.FacilityController;
import model.Facility;

public class FacilityPanel extends JPanel {

    private FacilityController ctrl;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchBox;
    private JLabel countLabel;

    public FacilityPanel(FacilityController ctrl) {
        this.ctrl = ctrl;
        buildUI();
        loadTable();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel
        JPanel topSection = new JPanel(new BorderLayout());

        // Search controls
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

        JButton addBtn = new JButton("Add Facility");
        JButton editBtn = new JButton("Edit Facility");
        JButton delBtn = new JButton("Delete Facility");
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
        String[] cols = {"Facility ID", "Name", "Type", "Address",
                "Postcode", "Phone", "Email", "Manager", "Capacity"};

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

        // Counter at bottom
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countLabel = new JLabel("Total Facilities: 0");
        bottom.add(countLabel);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadTable() {
        populateTable(ctrl.getAllFacilities());
    }

    private void populateTable(List<Facility> list) {
        model.setRowCount(0);

        for (Facility f : list) {
            model.addRow(new Object[]{
                    f.getFacilityId(),
                    f.getFacilityName(),
                    f.getFacilityType(),
                    f.getAddress(),
                    f.getPostcode(),
                    f.getPhoneNumber(),
                    f.getEmail(),
                    f.getManagerName(),
                    f.getCapacity()
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

        for (Facility f : ctrl.getAllFacilities()) {
            // Search in name, type, ID, or address
            boolean match = f.getFacilityName().toLowerCase().contains(q) ||
                    f.getFacilityType().toLowerCase().contains(q) ||
                    f.getFacilityId().toLowerCase().contains(q) ||
                    f.getAddress().toLowerCase().contains(q);

            if (match) {
                model.addRow(new Object[]{
                        f.getFacilityId(),
                        f.getFacilityName(),
                        f.getFacilityType(),
                        f.getAddress(),
                        f.getPostcode(),
                        f.getPhoneNumber(),
                        f.getEmail(),
                        f.getManagerName(),
                        f.getCapacity()
                });
            }
        }
        updateCount();
    }

    private void onAdd() {
        FacilityDialog dlg = new FacilityDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null, ctrl);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            ctrl.addFacility(dlg.getFacility());
            loadTable();
            showMsg("Facility added!");
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a facility first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        Facility facility = ctrl.getFacilityById(id);

        FacilityDialog dlg = new FacilityDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), facility, ctrl);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            ctrl.updateFacility(dlg.getFacility());
            loadTable();
            showMsg("Updated!");
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a facility first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete facility: " + name + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            ctrl.deleteFacility(id);
            loadTable();
            showMsg("Deleted!");
        }
    }

    private void showDetails() {
        int row = table.getSelectedRow();

        if (row == -1) {
            showWarning("Select a facility first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        Facility f = ctrl.getFacilityById(id);

        if (f == null) {
            showWarning("Facility not found!");
            return;
        }

        // Build details display
        StringBuilder details = new StringBuilder();

        details.append("=====================================\n");
        details.append("        FACILITY DETAILS\n");
        details.append("=====================================\n\n");

        details.append(">> BASIC INFO\n");
        details.append("ID:       ").append(f.getFacilityId()).append("\n");
        details.append("Name:     ").append(f.getFacilityName()).append("\n");
        details.append("Type:     ").append(f.getFacilityType()).append("\n\n");

        details.append(">> LOCATION\n");
        details.append("Address:  ").append(f.getAddress()).append("\n");
        details.append("Postcode: ").append(f.getPostcode()).append("\n\n");

        details.append(">> CONTACT\n");
        details.append("Phone:    ").append(f.getPhoneNumber()).append("\n");
        details.append("Email:    ").append(f.getEmail()).append("\n\n");

        details.append(">> MANAGEMENT\n");
        details.append("Manager:  ").append(f.getManagerName()).append("\n");
        details.append("Capacity: ").append(f.getCapacity()).append("\n");
        details.append("Hours:    ").append(f.getOpeningHours()).append("\n\n");

        details.append(">> SPECIALTIES\n");
        List<String> specs = f.getSpecialitiesOffered();
        if (specs.isEmpty()) {
            details.append("None listed\n");
        } else {
            for (String s : specs) {
                details.append("- ").append(s).append("\n");
            }
        }
        details.append("=====================================\n");

        // Display in scrollable text area
        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        textArea.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(450, 450));

        JOptionPane.showMessageDialog(this, scroll, "Facility Details", JOptionPane.PLAIN_MESSAGE);
    }

    private void updateCount() {
        countLabel.setText("Total Facilities: " + model.getRowCount());
    }

    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}