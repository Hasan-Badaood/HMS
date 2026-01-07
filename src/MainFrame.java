import javax.swing.*;
import java.awt.*;
import controller.*;
import view.*;

public class MainFrame extends JFrame {

    // controllers
    private PatientController patCtrl;
    private ClinicianController clinCtrl;
    private StaffController staffCtrl;
    private AppointmentController apptCtrl;
    private PrescriptionController rxCtrl;
    private ReferralController refCtrl;
    private FacilityController facCtrl;

    // panels
    private JTabbedPane tabs;
    private PatientPanel patientPanel;
    private ClinicianPanel clinicianPanel;
    private AppointmentPanel appointmentPanel;
    private PrescriptionPanel prescriptionPanel;
    private ReferralPanel referralPanel;
    private FacilityPanel facilityPanel;
    private StaffPanel staffPanel;

    public MainFrame() {
        initControllers();
        loadData();
        setupUI();
    }

    private void initControllers() {
        patCtrl = new PatientController();
        clinCtrl = new ClinicianController();
        staffCtrl = new StaffController();
        facCtrl = new FacilityController();

        apptCtrl = new AppointmentController(patCtrl, clinCtrl);
        rxCtrl = new PrescriptionController(patCtrl, clinCtrl);
        refCtrl = new ReferralController(patCtrl, clinCtrl, facCtrl);
    }

    private void loadData() {
        try {
            patCtrl.loadPatients("patients.csv");
            clinCtrl.loadClinicians("clinicians.csv");
            staffCtrl.loadStaff("staff.csv");
            facCtrl.loadFacilities("facilities.csv");
            apptCtrl.loadAppointments("appointments.csv");
            rxCtrl.loadPrescriptions("prescriptions.csv");
            refCtrl.loadReferrals("referrals.csv");

            // show success message with counts
            String msg = String.format(
                    "Data loaded!\n" +
                            "Patients: %d\n" +
                            "Clinicians: %d\n" +
                            "Staff: %d\n" +
                            "Facilities: %d\n" +
                            "Appointments: %d\n" +
                            "Prescriptions: %d\n" +
                            "Referrals: %d",
                    patCtrl.getPatientCount(),
                    clinCtrl.getClinicianCount(),
                    staffCtrl.getStaffCount(),
                    facCtrl.getFacilityCount(),
                    apptCtrl.getAppointmentCount(),
                    rxCtrl.getPrescriptionCount(),
                    refCtrl.getReferralCount()
            );

            showInfo(msg);

        } catch (Exception ex) {
            showError("Error loading data: " + ex.getMessage() +
                    "\nMake sure CSV files are in the right place.");
        }
    }

    private void setupUI() {
        setTitle("Healthcare Management System");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // menu bar
        createMenu();

        // tabs
        tabs = new JTabbedPane();

        // create all panels
        patientPanel = new PatientPanel(patCtrl);
        clinicianPanel = new ClinicianPanel(clinCtrl);
        staffPanel = new StaffPanel(staffCtrl);
        facilityPanel = new FacilityPanel(facCtrl);
        appointmentPanel = new AppointmentPanel(apptCtrl, patCtrl, clinCtrl, facCtrl);
        prescriptionPanel = new PrescriptionPanel(rxCtrl, patCtrl, clinCtrl);
        referralPanel = new ReferralPanel(refCtrl, patCtrl, clinCtrl, facCtrl);

        // add tabs
        tabs.addTab("Patients", patientPanel);
        tabs.addTab("Clinicians", clinicianPanel);
        tabs.addTab("Staff", staffPanel);
        tabs.addTab("Facilities", facilityPanel);
        tabs.addTab("Appointments", appointmentPanel);
        tabs.addTab("Prescriptions", prescriptionPanel);
        tabs.addTab("Referrals", referralPanel);

        add(tabs, BorderLayout.CENTER);

        // status bar at bottom
        createStatusBar();
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        // FILE menu
        JMenu fileMenu = new JMenu("File");

        JMenuItem refreshItem = new JMenuItem("Refresh Data");
        refreshItem.addActionListener(e -> loadData());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // VIEW menu
        JMenu viewMenu = new JMenu("View");

        String[] tabNames = {"Patients", "Clinicians", "Staff", "Facilities",
                "Appointments", "Prescriptions", "Referrals"};

        for (int i = 0; i < tabNames.length; i++) {
            final int index = i;
            JMenuItem item = new JMenuItem(tabNames[i]);
            item.addActionListener(e -> tabs.setSelectedIndex(index));
            viewMenu.add(item);
        }

        // HELP menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.add(new JLabel("Healthcare Management System - Ready"));
        add(statusBar, BorderLayout.SOUTH);
    }

    private void showAbout() {
        String msg =
                "Healthcare Management System\n" +
                        "Version 1.0\n\n" +
                        "Manage patients, appointments, prescriptions, and referrals.\n\n" +
                        "Built with Java Swing using MVC pattern.";

        showInfo(msg);
    }

    // helper methods
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        // system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // launch

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}