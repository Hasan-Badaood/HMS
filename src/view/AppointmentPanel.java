package view;

import controller.*;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class AppointmentPanel extends JPanel {

    // controllers we need
    private AppointmentController apptCtrl;
    private PatientController patCtrl;
    private ClinicianController clinCtrl;
    private FacilityController facCtrl;

    // table stuff
    private JTable table;
    private DefaultTableModel model;

    // ui components
    private JTextField searchBox;
    private JLabel countLabel;

    public AppointmentPanel(AppointmentController ac, PatientController pc,
                            ClinicianController cc, FacilityController fc) {
        this.apptCtrl = ac;
        this.patCtrl = pc;
        this.clinCtrl = cc;
        this.facCtrl = fc;

        initComponents();
        loadTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // top section
        JPanel topSection = new JPanel(new BorderLayout(5, 5));

        // search bar
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

        topSection.add(searchBar, BorderLayout.NORTH);

        // buttons row
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        String[] btnNames = {"Add Appointment", "Edit", "Delete", "Reschedule",
                "Cancel Appointment", "View Upcoming", "View All", "View Details"};

        JButton addBtn = new JButton(btnNames[0]);
        JButton editBtn = new JButton(btnNames[1]);
        JButton delBtn = new JButton(btnNames[2]);
        JButton reschedBtn = new JButton(btnNames[3]);
        JButton cancelBtn = new JButton(btnNames[4]);
        JButton upcomingBtn = new JButton(btnNames[5]);
        JButton allBtn = new JButton(btnNames[6]);
        JButton detailsBtn = new JButton(btnNames[7]);

        // hook up actions
        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        delBtn.addActionListener(e -> onDelete());
        reschedBtn.addActionListener(e -> onReschedule());
        cancelBtn.addActionListener(e -> onCancel());
        upcomingBtn.addActionListener(e -> showUpcoming());
        allBtn.addActionListener(e -> loadTable());
        detailsBtn.addActionListener(e -> showDetails());

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(reschedBtn);
        btnPanel.add(cancelBtn);
        btnPanel.add(upcomingBtn);
        btnPanel.add(allBtn);
        btnPanel.add(detailsBtn);

        topSection.add(btnPanel, BorderLayout.SOUTH);
        add(topSection, BorderLayout.NORTH);

        // table
        String[] cols = {"ID", "Patient", "Clinician", "Date", "Time",
                "Duration", "Type", "Status", "Reason"};

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false; // no editing directly in table
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // set some reasonable column widths
        int[] widths = {80, 120, 120, 90, 60, 60, 100, 80, 150};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        add(new JScrollPane(table), BorderLayout.CENTER);

        // === BOTTOM ===
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countLabel = new JLabel("Total Appointments: 0");
        bottom.add(countLabel);
        add(bottom, BorderLayout.SOUTH);
    }

    // load all appointments into table
    private void loadTable() {
        model.setRowCount(0); // clear first

        List<Appointment> list = apptCtrl.getAllAppointments();
        for (Appointment a : list) {
            addRowToTable(a);
        }
        updateCount();
    }

    // helper to add single row
    private void addRowToTable(Appointment a) {
        Patient p = patCtrl.getPatientById(a.getPatientId());
        Clinician c = clinCtrl.getClinicianById(a.getClinicianId());

        Object[] row = {
                a.getAppointmentId(),
                p != null ? p.getFullName() : a.getPatientId(),
                c != null ? c.getFullName() : a.getClinicianId(),
                a.getAppointmentDate(),
                a.getAppointmentTime(),
                a.getDurationMinutes() + " min",
                a.getAppointmentType(),
                a.getStatus(),
                a.getReasonForVisit()
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

        for (Appointment a : apptCtrl.getAllAppointments()) {
            Patient p = patCtrl.getPatientById(a.getPatientId());
            Clinician c = clinCtrl.getClinicianById(a.getClinicianId());

            String patName = (p != null) ? p.getFullName().toLowerCase() : "";
            String clinName = (c != null) ? c.getFullName().toLowerCase() : "";
            String id = a.getAppointmentId().toLowerCase();

            // check if query matches any of these
            if (patName.contains(q) || clinName.contains(q) || id.contains(q)) {
                addRowToTable(a);
            }
        }
        updateCount();
    }

    private void onAdd() {
        AppointmentDialog dlg = new AppointmentDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                null, apptCtrl, patCtrl, clinCtrl, facCtrl);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            Appointment newAppt = dlg.getAppointment();
            apptCtrl.addAppointment(newAppt);
            loadTable();

            JOptionPane.showMessageDialog(this,
                    "Appointment added!\nID: " + newAppt.getAppointmentId(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();

        if (row == -1) {
            showWarning("Please select an appointment to edit.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        Appointment appt = apptCtrl.getAppointmentById(id);

        AppointmentDialog dlg = new AppointmentDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                appt, apptCtrl, patCtrl, clinCtrl, facCtrl);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            apptCtrl.updateAppointment(dlg.getAppointment());
            loadTable();
            showInfo("Appointment updated!");
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();

        if (row == -1) {
            showWarning("Please select an appointment to delete.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        String patient = (String) model.getValueAt(row, 1);
        String date = model.getValueAt(row, 3).toString();
        String time = model.getValueAt(row, 4).toString();

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete this appointment?\n\nPatient: " + patient +
                        "\nDate: " + date + " at " + time,
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            apptCtrl.deleteAppointment(id);
            loadTable();
            showInfo("Deleted!");
        }
    }

    private void onCancel() {
        int row = table.getSelectedRow();

        if (row == -1) {
            showWarning("Select an appointment first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        String status = (String) model.getValueAt(row, 7);

        // validation checks
        if (status.equals("CANCELLED")) {
            showInfo("Already cancelled.");
            return;
        }
        if (status.equals("COMPLETED")) {
            showWarning("Can't cancel a completed appointment.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Cancel this appointment?", "Confirm",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            if (apptCtrl.cancelAppointment(id)) {
                loadTable();
                showInfo("Cancelled!");
            } else {
                showError("Couldn't cancel. Something went wrong.");
            }
        }
    }

    private void onReschedule() {
        int row = table.getSelectedRow();

        if (row == -1) {
            showWarning("Select an appointment first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        String status = (String) model.getValueAt(row, 7);

        if (status.equals("CANCELLED") || status.equals("COMPLETED")) {
            showWarning("Can't reschedule this appointment.");
            return;
        }

        Appointment appt = apptCtrl.getAppointmentById(id);

        // simple dialog for new date/time
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField dateField = new JTextField(appt.getAppointmentDate().toString());
        JTextField timeField = new JTextField(appt.getAppointmentTime());

        panel.add(new JLabel("New Date (yyyy-mm-dd):"));
        panel.add(dateField);
        panel.add(new JLabel("New Time (HH:MM):"));
        panel.add(timeField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Reschedule", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                LocalDate newDate = LocalDate.parse(dateField.getText().trim());
                String newTime = timeField.getText().trim();

                if (apptCtrl.rescheduleAppointment(id, newDate, newTime)) {
                    loadTable();
                    showInfo("Rescheduled to " + newDate + " at " + newTime);
                } else {
                    showError("Reschedule failed.");
                }
            } catch (Exception ex) {
                showError("Bad format! Use yyyy-mm-dd and HH:MM");
            }
        }
    }

    private void showUpcoming() {
        model.setRowCount(0);

        List<Appointment> upcoming = apptCtrl.getUpcomingAppointments();
        for (Appointment a : upcoming) {
            addRowToTable(a);
        }

        updateCount();
        showInfo("Showing " + upcoming.size() + " upcoming appointments");
    }

    private void showDetails() {
        int row = table.getSelectedRow();

        if (row == -1) {
            showWarning("Select an appointment first.");
            return;
        }

        String id = (String) model.getValueAt(row, 0);
        Appointment appt = apptCtrl.getAppointmentById(id);

        if (appt == null) {
            showError("Appointment not found!");
            return;
        }

        // gather related info
        Patient pat = patCtrl.getPatientById(appt.getPatientId());
        Clinician clin = clinCtrl.getClinicianById(appt.getClinicianId());
        Facility fac = facCtrl.getFacilityById(appt.getFacilityId());

        // build the details string
        StringBuilder sb = new StringBuilder();

        sb.append("=====================================\n");
        sb.append("       APPOINTMENT DETAILS\n");
        sb.append("=====================================\n\n");

        sb.append(">> APPOINTMENT INFO\n");
        sb.append("ID: ").append(appt.getAppointmentId()).append("\n");
        sb.append("Date: ").append(appt.getAppointmentDate()).append("\n");
        sb.append("Time: ").append(appt.getAppointmentTime()).append("\n");
        sb.append("Duration: ").append(appt.getDurationMinutes()).append(" mins\n");
        sb.append("Type: ").append(appt.getAppointmentType()).append("\n");
        sb.append("Status: ").append(appt.getStatus()).append("\n\n");

        sb.append(">> PATIENT\n");
        if (pat != null) {
            sb.append("Name: ").append(pat.getFullName()).append("\n");
            sb.append("NHS#: ").append(pat.getNhsNumber()).append("\n");
            sb.append("Phone: ").append(pat.getPhoneNumber()).append("\n");
        } else {
            sb.append("(not found)\n");
        }
        sb.append("\n");

        sb.append(">> CLINICIAN\n");
        if (clin != null) {
            sb.append("Dr. ").append(clin.getFullName()).append("\n");
            sb.append("Specialty: ").append(clin.getSpecialization()).append("\n");
            sb.append("License: ").append(clin.getLicenseNumber()).append("\n");
        } else {
            sb.append("(not found)\n");
        }
        sb.append("\n");

        sb.append(">> FACILITY\n");
        if (fac != null) {
            sb.append("Name: ").append(fac.getFacilityName()).append("\n");
            sb.append("Address: ").append(fac.getAddress()).append("\n");
            sb.append("Phone: ").append(fac.getPhoneNumber()).append("\n");
        } else {
            sb.append("(not found)\n");
        }
        sb.append("\n");

        sb.append(">> VISIT INFO\n");
        sb.append("Reason: ").append(appt.getReasonForVisit()).append("\n");
        String notes = appt.getNotes();
        sb.append("Notes: ").append(notes != null ? notes : "None").append("\n\n");

        sb.append(">> TIMESTAMPS\n");
        sb.append("Created: ").append(appt.getCreatedDate()).append("\n");
        sb.append("Modified: ").append(appt.getLastModified()).append("\n");
        sb.append("=====================================\n");

        // show in scrollable text area
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(450, 500));

        JOptionPane.showMessageDialog(this, scroll, "Details", JOptionPane.PLAIN_MESSAGE);
    }

    // update the count label at bottom
    private void updateCount() {
        countLabel.setText("Total Appointments: " + model.getRowCount());
    }

    // quick helper methods for dialogs
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}