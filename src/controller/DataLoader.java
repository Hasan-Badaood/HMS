package controller;

import model.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataLoader {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Helper to handle CSV parsing with quoted fields
    private static List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|(?<=,|^)([^,]*)(?=,|$)");
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                fields.add(matcher.group(1));
            } else {
                fields.add(matcher.group(2));
            }
        }
        return fields;
    }

    // Load patients from CSV file
    public static List<Patient> loadPatients(String filename) {
        List<Patient> patients = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // Skip header line
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                try {
                    List<String> data = parseCSVLine(line);

                    if (data.size() < 13) {
                        System.err.println("Line " + lineNumber + " missing columns - expected 13+, got " + data.size());
                        continue;
                    }

                    Patient patient = new Patient(
                            data.get(0).trim(),
                            data.get(1).trim(),
                            data.get(2).trim(),
                            data.get(7).trim(),
                            data.get(6).trim(),
                            data.get(0).trim(),
                            parseDate(data.get(3).trim()),
                            data.get(4).trim(),
                            data.get(5).trim(),
                            data.get(8).trim(),
                            data.get(9).trim(),
                            data.get(10).trim(),
                            data.get(11).trim(),
                            parseDate(data.get(12).trim()),
                            data.size() > 13 ? data.get(13).trim() : ""
                    );
                    patients.add(patient);

                } catch (Exception e) {
                    System.err.println("Error parsing line " + lineNumber + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("Successfully loaded " + patients.size() + " patients from " + filename);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }

        return patients;
    }

    // Save patients back to CSV
    public static void savePatients(String filename, List<Patient> patients) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write("patient_id,first_name,last_name,date_of_birth,nhs_number,gender," +
                    "phone_number,email,address,postcode,emergency_contact_name," +
                    "emergency_contact_phone,registration_date,gp_surgery_id");
            bw.newLine();

            for (Patient p : patients) {
                StringBuilder line = new StringBuilder();
                line.append(p.getPatientId()).append(",");
                line.append(p.getFirstName()).append(",");
                line.append(p.getLastName()).append(",");
                line.append(p.getDateOfBirth()).append(",");
                line.append(p.getNhsNumber()).append(",");
                line.append(p.getGender()).append(",");
                line.append(p.getPhoneNumber()).append(",");
                line.append(p.getEmail()).append(",");
                line.append("\"").append(p.getAddress()).append("\",");
                line.append(p.getPostcode()).append(",");
                line.append("\"").append(p.getEmergencyContactName()).append("\",");
                line.append(p.getEmergencyContactPhone()).append(",");
                line.append(p.getRegistrationDate()).append(",");
                line.append(p.getGpSurgeryId());

                bw.write(line.toString());
                bw.newLine();
            }
            System.out.println("Saved " + patients.size() + " patients to " + filename);

        } catch (IOException e) {
            System.err.println("Error saving patients: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load staff members
    public static List<Staff> loadStaff(String filename) {
        List<Staff> staffList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                try {
                    List<String> data = parseCSVLine(line);

                    if (data.size() < 12) {
                        System.err.println("Line " + lineNumber + " incomplete - needs 12 fields, got " + data.size());
                        continue;
                    }

                    Staff staff = new Staff(
                            data.get(0).trim(),
                            data.get(1).trim(),
                            data.get(2).trim(),
                            data.get(7).trim(),
                            data.get(6).trim(),
                            data.get(0).trim(),
                            data.get(3).trim(),
                            data.get(4).trim(),
                            data.get(5).trim(),
                            data.get(8).trim(),
                            parseDate(data.get(9).trim()),
                            data.get(10).trim(),
                            data.get(11).trim()
                    );
                    staffList.add(staff);

                } catch (Exception e) {
                    System.err.println("Error on line " + lineNumber + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("Loaded " + staffList.size() + " staff members from " + filename);

        } catch (IOException e) {
            System.err.println("File reading error: " + e.getMessage());
            e.printStackTrace();
        }

        return staffList;
    }

    public static void saveStaff(String filename, List<Staff> staffList) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write("staff_id,first_name,last_name,role,department,facility_id," +
                    "phone_number,email,employment_status,start_date,line_manager,access_level");
            bw.newLine();

            for (Staff s : staffList) {
                StringBuilder line = new StringBuilder();
                line.append(s.getStaffId()).append(",");
                line.append(s.getFirstName()).append(",");
                line.append(s.getLastName()).append(",");
                line.append(s.getRole()).append(",");
                line.append("\"").append(s.getDepartment()).append("\",");
                line.append(s.getFacilityId()).append(",");
                line.append(s.getPhoneNumber()).append(",");
                line.append(s.getEmail()).append(",");
                line.append(s.getEmploymentStatus()).append(",");
                line.append(s.getStartDate()).append(",");
                line.append("\"").append(s.getLineManager()).append("\",");
                line.append(s.getAccessLevel());

                bw.write(line.toString());
                bw.newLine();
            }
            System.out.println("Successfully saved " + staffList.size() + " staff members");

        } catch (IOException e) {
            System.err.println("Failed to save staff data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load clinicians from file
    public static List<Clinician> loadClinicians(String filename) {
        List<Clinician> clinicians = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                try {
                    List<String> data = parseCSVLine(line);

                    if (data.size() < 12) {
                        System.err.println("Line " + lineNumber + " has insufficient data");
                        continue;
                    }

                    Clinician clinician = new Clinician(
                            data.get(0).trim(),
                            data.get(1).trim(),
                            data.get(2).trim(),
                            data.get(3).trim(),
                            data.get(4).trim(),
                            data.get(5).trim(),
                            data.get(6).trim(),
                            data.get(7).trim(),
                            data.get(8).trim(),
                            data.get(9).trim(),
                            data.get(10).trim(),
                            parseDate(data.get(11).trim())
                    );
                    clinicians.add(clinician);

                } catch (Exception e) {
                    System.err.println("Error at line " + lineNumber + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("Loaded " + clinicians.size() + " clinicians");

        } catch (IOException e) {
            System.err.println("Cannot read file: " + e.getMessage());
            e.printStackTrace();
        }

        return clinicians;
    }

    public static void saveClinicians(String filename, List<Clinician> clinicians) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write("clinician_id,first_name,last_name,title,specialization,license_number," +
                    "phone_number,email,facility_id,workplace_type,employment_status,start_date");
            bw.newLine();

            for (Clinician c : clinicians) {
                StringBuilder line = new StringBuilder();
                line.append(c.getClinicianId()).append(",");
                line.append(c.getFirstName()).append(",");
                line.append(c.getLastName()).append(",");
                line.append("\"").append(c.getTitle()).append("\",");
                line.append("\"").append(c.getSpecialization()).append("\",");
                line.append(c.getLicenseNumber()).append(",");
                line.append(c.getPhoneNumber()).append(",");
                line.append(c.getEmail()).append(",");
                line.append(c.getFacilityId()).append(",");
                line.append("\"").append(c.getWorkplaceType()).append("\",");
                line.append(c.getEmploymentStatus()).append(",");
                line.append(c.getStartDate());

                bw.write(line.toString());
                bw.newLine();
            }
            System.out.println("Saved " + clinicians.size() + " clinicians");

        } catch (IOException e) {
            System.err.println("Error saving clinicians: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load facilities
    public static List<Facility> loadFacilities(String filename) {
        List<Facility> facilities = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                try {
                    List<String> data = parseCSVLine(line);

                    if (data.size() < 10) {
                        System.err.println("Line " + lineNumber + " is missing required fields");
                        continue;
                    }

                    Facility facility = new Facility(
                            data.get(0).trim(),
                            data.get(1).trim(),
                            data.get(2).trim(),
                            data.get(3).trim(),
                            data.get(4).trim(),
                            data.get(5).trim(),
                            data.get(6).trim(),
                            data.get(7).trim(),
                            data.get(8).trim(),
                            parseInt(data.get(9).trim())
                    );

                    // Parse specialities (semicolon separated)
                    if (data.size() > 10 && !data.get(10).trim().isEmpty()) {
                        String[] specialities = data.get(10).trim().split(";");
                        for (String spec : specialities) {
                            if (!spec.trim().isEmpty()) {
                                facility.addSpeciality(spec.trim());
                            }
                        }
                    }

                    facilities.add(facility);

                } catch (Exception e) {
                    System.err.println("Problem on line " + lineNumber + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("Loaded " + facilities.size() + " facilities from file");

        } catch (IOException e) {
            System.err.println("Error loading facilities: " + e.getMessage());
            e.printStackTrace();
        }

        return facilities;
    }

    public static void saveFacilities(String filename, List<Facility> facilities) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write("facility_id,facility_name,facility_type,address,postcode," +
                    "phone_number,email,opening_hours,manager_name,capacity,specialities");
            bw.newLine();

            for (Facility f : facilities) {
                StringBuilder line = new StringBuilder();
                line.append(f.getFacilityId()).append(",");
                line.append(f.getFacilityName()).append(",");
                line.append(f.getFacilityType()).append(",");
                line.append("\"").append(f.getAddress()).append("\",");
                line.append(f.getPostcode()).append(",");
                line.append(f.getPhoneNumber()).append(",");
                line.append(f.getEmail()).append(",");
                line.append("\"").append(f.getOpeningHours()).append("\",");
                line.append(f.getManagerName()).append(",");
                line.append(f.getCapacity()).append(",");

                // Combine specialities with semicolons
                List<String> specs = f.getSpecialitiesOffered();
                if (specs.isEmpty()) {
                    line.append("");
                } else {
                    line.append("\"").append(String.join(";", specs)).append("\"");
                }

                bw.write(line.toString());
                bw.newLine();
            }
            System.out.println("Facilities saved successfully - " + facilities.size() + " records");

        } catch (IOException e) {
            System.err.println("Failed saving facilities: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load appointments
    public static List<Appointment> loadAppointments(String filename) {
        List<Appointment> appointments = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                if (data.length >= 10) {
                    Appointment appt = new Appointment(
                            data[0].trim(),
                            data[1].trim(),
                            data[2].trim(),
                            data[3].trim(),
                            parseDate(data[4].trim()),
                            data[5].trim(),
                            parseInt(data[6].trim()),
                            data[7].trim(),
                            data[8].trim(),
                            data[9].trim(),
                            data.length > 10 ? data[10].trim() : ""
                    );
                    appointments.add(appt);
                }
            }
            System.out.println("Loaded " + appointments.size() + " appointments");

        } catch (IOException e) {
            System.err.println("Error loading appointments: " + e.getMessage());
        }

        return appointments;
    }

    public static void saveAppointments(String filename, List<Appointment> appointments) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write("appointment_id,patient_id,clinician_id,facility_id,appointment_date," +
                    "appointment_time,duration_minutes,appointment_type,status,reason_for_visit,notes");
            bw.newLine();

            for (Appointment a : appointments) {
                StringBuilder line = new StringBuilder();
                line.append(a.getAppointmentId()).append(",");
                line.append(a.getPatientId()).append(",");
                line.append(a.getClinicianId()).append(",");
                line.append(a.getFacilityId()).append(",");
                line.append(a.getAppointmentDate()).append(",");
                line.append(a.getAppointmentTime()).append(",");
                line.append(a.getDurationMinutes()).append(",");
                line.append(a.getAppointmentType()).append(",");
                line.append(a.getStatus()).append(",");
                line.append(a.getReasonForVisit()).append(",");

                String notes = a.getNotes();
                line.append(notes != null ? notes : "");

                bw.write(line.toString());
                bw.newLine();
            }
            System.out.println("Saved " + appointments.size() + " appointments");

        } catch (IOException e) {
            System.err.println("Appointment save failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load prescriptions - supports multiple CSV formats
    // Load prescriptions
    public static List<Prescription> loadPrescriptions(String filename) {
        List<Prescription> prescriptions = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // Skip header
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                try {
                    List<String> data = parseCSVLine(line);
                    if (data.size() >= 15) {
                        Prescription rx = new Prescription(
                                data.get(0).trim(),
                                data.get(1).trim(),
                                data.get(2).trim(),
                                data.get(3).trim(),
                                parseDate(data.get(4).trim()),
                                data.get(5).trim(),
                                data.get(6).trim(),
                                data.get(7).trim(),
                                parseInt(data.get(8).trim()),
                                data.get(9).trim(),
                                data.get(10).trim(),
                                data.get(11).trim(),
                                data.get(12).trim(),
                                parseDate(data.get(13).trim()),
                                parseDate(data.get(14).trim())
                        );
                        prescriptions.add(rx);
                    } else {
                        System.err.println("Line " + lineNumber + " has incorrect format. Expected 15 columns, got " + data.size());
                    }

                } catch (Exception e) {
                    System.err.println("Error parsing line " + lineNumber + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("Successfully loaded " + prescriptions.size() + " prescriptions");

        } catch (IOException e) {
            System.err.println("Error reading prescriptions file: " + e.getMessage());
            e.printStackTrace();
        }

        return prescriptions;
    }

    public static void savePrescriptions(String filename, List<Prescription> prescriptions) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write("prescription_id,patient_id,clinician_id,appointment_id,prescription_date," +
                    "medication_name,dosage,frequency,duration_days,quantity,instructions," +
                    "pharmacy_name,status,issue_date,collection_date");
            bw.newLine();

            for (Prescription p : prescriptions) {
                StringBuilder line = new StringBuilder();
                line.append(p.getPrescriptionId()).append(",");
                line.append(p.getPatientId()).append(",");
                line.append(p.getClinicianId()).append(",");

                String apptId = p.getAppointmentId();
                line.append(apptId != null ? apptId : "").append(",");
                line.append(p.getPrescriptionDate()).append(",");

                line.append(p.getMedicationName()).append(",");
                line.append(p.getDosage()).append(",");
                line.append(p.getFrequency()).append(",");

                line.append(p.getDurationDays()).append(",");
                line.append(p.getQuantity()).append(",");

                line.append(p.getInstructions()).append(",");
                line.append(p.getPharmacyName()).append(",");

                line.append(p.getStatus()).append(",");

                LocalDate issueDate = p.getIssueDate();
                LocalDate collectionDate = p.getCollectionDate();
                line.append(issueDate != null ? issueDate.toString() : "").append(",");
                line.append(collectionDate != null ? collectionDate.toString() : "");

                bw.write(line.toString());
                bw.newLine();
            }
            System.out.println("Saved " + prescriptions.size() + " prescriptions");

        } catch (IOException e) {
            System.err.println("Failed to save prescriptions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load referrals from CSV
    public static List<Referral> loadReferrals(String filename) {
        List<Referral> referrals = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                List<String> partsList = parseCSVLine(line);
                String[] parts = partsList.toArray(new String[0]);

                if (parts.length >= 16) {
                    Referral ref = new Referral(
                            parts[0].trim(),
                            parts[1].trim(),
                            parts[2].trim(),
                            parts[3].trim(),
                            parts[4].trim(),
                            parts[5].trim(),
                            parseDate(parts[6].trim()),
                            parts[7].trim(),
                            parts[8].trim(),
                            parts[9].trim(),
                            parts[10].trim(),
                            parts[11].trim(),
                            parts[12].trim(),
                            parts[13].trim(),
                            parseDate(parts[14].trim()),
                            parseDate(parts[15].trim())
                    );
                    referrals.add(ref);
                }
            }
            System.out.println("Loaded " + referrals.size() + " referrals");
        } catch (IOException e) {
            System.err.println("Error loading referrals: " + e.getMessage());
        }

        return referrals;
    }

    public static void saveReferrals(String filename, List<Referral> referrals) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("referral_id,patient_id,referring_clinician_id,referred_to_clinician_id," +
                    "referring_facility_id,referred_to_facility_id,referral_date,urgency_level," +
                    "referral_reason,clinical_summary,requested_investigations,status," +
                    "appointment_id,notes,created_date,last_updated");

            for (Referral r : referrals) {
                String referralDate = r.getReferralDate() != null ? r.getReferralDate().toString() : "";
                String createdDate = r.getCreatedDate() != null ? r.getCreatedDate().toString() : "";
                String lastUpdated = r.getLastUpdated() != null ? r.getLastUpdated().toString() : "";

                pw.println(String.join(",",
                        escapeCSV(r.getReferralId()),
                        escapeCSV(r.getPatientId()),
                        escapeCSV(r.getReferringClinicianId()),
                        escapeCSV(r.getReferredToClinicianId()),
                        escapeCSV(r.getReferringFacilityId()),
                        escapeCSV(r.getReferredToFacilityId()),
                        referralDate,
                        escapeCSV(r.getUrgencyLevel()),
                        escapeCSV(r.getReferralReason()),
                        escapeCSV(r.getClinicalSummary()),
                        escapeCSV(r.getRequestedInvestigations()),
                        escapeCSV(r.getStatus()),
                        escapeCSV(r.getAppointmentId()),
                        escapeCSV(r.getNotes()),
                        createdDate,
                        lastUpdated
                ));
            }
            System.out.println("Referrals saved - " + referrals.size() + " records written");
        } catch (IOException e) {
            System.err.println("Error saving referrals: " + e.getMessage());
        }
    }

    // Handle CSV escaping for special characters
    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // Parse dates with fallback handling
    private static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr.trim());
            } catch (Exception ex) {
                System.err.println("Invalid date format: " + dateStr);
                return LocalDate.now();
            }
        }
    }

    // Safe integer parsing
    private static int parseInt(String str) {
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}