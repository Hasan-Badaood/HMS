package view;

import controller.StaffController;
import model.Staff;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class StaffPanel extends JPanel {

    private StaffController ctrl;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchBox;
    private JLabel countLabel;

    public StaffPanel(StaffController controller) {
        this.ctrl = controller;
        initComponents();
        loadTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top section
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));

        // Search bar row
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

        // Filter button in search row
        searchRow.add(new JSeparator(JSeparator.VERTICAL));
        JButton filterBtn = new JButton("Filter by Role");
        filterBtn.addActionListener(e -> filterByRole());
        searchRow.add(filterBtn);

        topSection.add(searchRow);

        // Action buttons row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Staff");
        JButton editBtn = new JButton("Edit Staff");
        JButton delBtn = new JButton("Delete Staff");
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
        String[] cols = {"Staff ID", "Name", "Role", "Department",
                "Facility ID", "Phone", "Email", "Status", "Start Date"};

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
        countLabel = new JLabel("Total Staff: 0");
        bottom.add(countLabel);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadTable() {
        model.setRowCount(0);
        List<Staff> list = ctrl.getAllStaff();
        for (Staff s : list) {
            addRowToTable(s);
        }
        updateCount();
    }

    private void addRowToTable(Staff s) {
        Object[] row = {
                s.getStaffId(),
                s.getFullName(),
                s.getRole(),
                s.getDepartment(),
                s.getFacilityId(),
                s.getPhoneNumber(),
                s.getEmail(),
                s.getEmploymentStatus(),
                s.getStartDate()
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
        for (Staff s : ctrl.getAllStaff()) {
            // Null-safe search logic consistent with ReferralPanel
            String id = s.getStaffId() != null ? s.getStaffId().toLowerCase() : "";
            String name = s.getFullName() != null ? s.getFullName().toLowerCase() : "";
            String role = s.getRole() != null ? s.getRole().toLowerCase() : "";
            String dept = s.getDepartment() != null ? s.getDepartment().toLowerCase() : "";

            if (id.contains(q) || name.contains(q) || role.contains(q) || dept.contains(q)) {
                addRowToTable(s);
            }
        }
        updateCount();
    }

    private void onAdd() {
        StaffDialog dlg = new StaffDialog((Frame) SwingUtilities.getWindowAncestor(this), null, ctrl);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            ctrl.addStaff(dlg.getStaff());
            loadTable();
            showInfo("Staff member added successfully!");
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a staff member first.");
            return;
        }
        String id = (String) model.getValueAt(row, 0);
        Staff staff = ctrl.getStaffById(id);

        StaffDialog dlg = new StaffDialog((Frame) SwingUtilities.getWindowAncestor(this), staff, ctrl);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            ctrl.updateStaff(dlg.getStaff());
            loadTable();
            showInfo("Staff details updated!");
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a staff member first.");
            return;
        }
        String id = (String) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete: " + name + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            ctrl.deleteStaff(id);
            loadTable();
            showInfo("Staff member removed.");
        }
    }

    private void filterByRole() {
        Set<String> roles = new HashSet<>();
        for (Staff s : ctrl.getAllStaff()) {
            if (s.getRole() != null) roles.add(s.getRole());
        }
        String[] roleArray = roles.toArray(new String[0]);
        Arrays.sort(roleArray);

        if (roleArray.length == 0) {
            showInfo("No roles available to filter.");
            return;
        }

        String selected = (String) JOptionPane.showInputDialog(this,
                "Select role:", "Filter by Role",
                JOptionPane.QUESTION_MESSAGE, null, roleArray, roleArray[0]);

        if (selected != null) {
            List<Staff> filtered = ctrl.getStaffByRole(selected);
            model.setRowCount(0);
            for (Staff s : filtered) {
                addRowToTable(s);
            }
            updateCount();
        }
    }

    private void showDetails() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Select a staff member first.");
            return;
        }
        String id = (String) model.getValueAt(row, 0);
        Staff s = ctrl.getStaffById(id);

        if (s == null) return;

        StringBuilder details = new StringBuilder();
        details.append("=====================================\n");
        details.append("           STAFF DETAILS             \n");
        details.append("=====================================\n\n");

        details.append(">> PERSONAL INFO\n");
        details.append("Staff ID:    ").append(s.getStaffId()).append("\n");
        details.append("Full Name:   ").append(s.getFullName()).append("\n");
        details.append("Email:       ").append(s.getEmail()).append("\n");
        details.append("Phone:       ").append(s.getPhoneNumber()).append("\n\n");

        details.append(">> EMPLOYMENT INFO\n");
        details.append("Role:        ").append(s.getRole()).append("\n");
        details.append("Department:  ").append(s.getDepartment()).append("\n");
        details.append("Facility ID: ").append(s.getFacilityId()).append("\n");
        details.append("Status:      ").append(s.getEmploymentStatus()).append("\n");
        details.append("Start Date:  ").append(s.getStartDate()).append("\n");
        details.append("Manager:     ").append(nullSafe(s.getLineManager())).append("\n");
        details.append("Access Lvl:  ").append(s.getAccessLevel()).append("\n");

        details.append("\n=====================================\n");

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        textArea.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(450, 450));

        JOptionPane.showMessageDialog(this, scroll, "Staff Member Details", JOptionPane.PLAIN_MESSAGE);
    }

    private String nullSafe(String val) {
        return (val == null || val.isEmpty()) ? "N/A" : val;
    }

    private void updateCount() {
        countLabel.setText("Total Staff: " + model.getRowCount());
    }

    private void showInfo(String msg) { JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE); }
    private void showWarning(String msg) { JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE); }
    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
}