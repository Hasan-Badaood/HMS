package controller;

import model.*;
import java.time.LocalDate;
import java.util.*;

public class AppointmentController {

    private List<Appointment> appointments;
    private PatientController patCtrl;
    private ClinicianController clinCtrl;
    private String dataFilename;

    public AppointmentController(PatientController pc, ClinicianController cc) {
        appointments = new ArrayList<>();
        patCtrl = pc;
        clinCtrl = cc;
    }

    public void loadAppointments(String filename) {
        dataFilename = filename;
        appointments = DataLoader.loadAppointments(filename);

        // Link appointments with their respective patients and clinicians
        for (Appointment apt : appointments) {
            Patient patient = patCtrl.getPatientById(apt.getPatientId());
            if (patient != null) {
                patient.addAppointment(apt);
            }

            Clinician clinician = clinCtrl.getClinicianById(apt.getClinicianId());
            if (clinician != null) {
                clinician.addAppointment(apt);
            }
        }
    }

    private void saveToFile() {
        if (dataFilename != null) {
            DataLoader.saveAppointments(dataFilename, appointments);
        }
    }

    public List<Appointment> getAllAppointments() {
        return new ArrayList<>(appointments);
    }

    public Appointment getAppointmentById(String id) {
        for (Appointment apt : appointments) {
            if (apt.getAppointmentId().equals(id)) {
                return apt;
            }
        }
        return null;
    }

    public List<Appointment> getAppointmentsByPatient(String patientId) {
        List<Appointment> patientAppointments = new ArrayList<>();

        for (Appointment apt : appointments) {
            if (apt.getPatientId().equals(patientId)) {
                patientAppointments.add(apt);
            }
        }

        // Sort by date in descending order (most recent first)
        patientAppointments.sort((a1, a2) ->
                a2.getAppointmentDate().compareTo(a1.getAppointmentDate())
        );

        return patientAppointments;
    }

    public List<Appointment> getAppointmentsByClinician(String clinicianId) {
        List<Appointment> clinicianAppointments = new ArrayList<>();

        for (Appointment apt : appointments) {
            if (apt.getClinicianId().equals(clinicianId)) {
                clinicianAppointments.add(apt);
            }
        }

        clinicianAppointments.sort((a1, a2) ->
                a2.getAppointmentDate().compareTo(a1.getAppointmentDate())
        );

        return clinicianAppointments;
    }

    public List<Appointment> getUpcomingAppointments() {
        LocalDate today = LocalDate.now();
        List<Appointment> upcomingApts = new ArrayList<>();

        for (Appointment apt : appointments) {
            // Include appointments from today onwards that aren't cancelled
            if ((apt.getAppointmentDate().isAfter(today) ||
                    apt.getAppointmentDate().equals(today)) &&
                    !apt.getStatus().equals("CANCELLED")) {
                upcomingApts.add(apt);
            }
        }

        upcomingApts.sort(Comparator.comparing(Appointment::getAppointmentDate));
        return upcomingApts;
    }

    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        List<Appointment> dateAppointments = new ArrayList<>();

        for (Appointment apt : appointments) {
            if (apt.getAppointmentDate().equals(date)) {
                dateAppointments.add(apt);
            }
        }

        dateAppointments.sort(Comparator.comparing(Appointment::getAppointmentTime));
        return dateAppointments;
    }

    public void addAppointment(Appointment apt) {
        appointments.add(apt);

        // Link the appointment to the patient's record
        Patient patient = patCtrl.getPatientById(apt.getPatientId());
        if (patient != null) {
            patient.addAppointment(apt);
        }

        // Link the appointment to the clinician's schedule
        Clinician clinician = clinCtrl.getClinicianById(apt.getClinicianId());
        if (clinician != null) {
            clinician.addAppointment(apt);
        }

        saveToFile();
    }

    public boolean updateAppointment(Appointment apt) {
        for (int i = 0; i < appointments.size(); i++) {
            if (appointments.get(i).getAppointmentId().equals(apt.getAppointmentId())) {
                appointments.set(i, apt);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public boolean deleteAppointment(String id) {
        for (int i = 0; i < appointments.size(); i++) {
            if (appointments.get(i).getAppointmentId().equals(id)) {
                appointments.remove(i);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public boolean rescheduleAppointment(String id, LocalDate newDate, String newTime) {
        Appointment apt = getAppointmentById(id);
        if (apt != null) {
            boolean success = apt.reschedule(newDate, newTime);
            if (success) {
                saveToFile();
            }
            return success;
        }
        return false;
    }

    public boolean cancelAppointment(String id) {
        Appointment apt = getAppointmentById(id);
        if (apt != null) {
            boolean success = apt.cancel();
            if (success) {
                saveToFile();
            }
            return success;
        }
        return false;
    }

    public int getAppointmentCount() {
        return appointments.size();
    }
}