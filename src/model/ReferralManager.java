package model;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Singleton class for managing referrals, emails, and EHR updates
 */
public class ReferralManager {

    private static ReferralManager instance;

    private List<Referral> referrals;
    private Map<String, Referral> registry;
    private Queue<Referral> pending;
    private List<String> auditLog;

    // Output file paths
    private static final String REFERRAL_FILE = "output_referrals.txt";
    private static final String EMAIL_FILE = "output_emails.txt";
    private static final String EHR_FILE = "output_ehr_updates.txt";
    private static final String AUDIT_FILE = "referral_audit_log.txt";

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ReferralManager() {
        referrals = new ArrayList<>();
        registry = new HashMap<>();
        pending = new LinkedList<>();
        auditLog = new ArrayList<>();
        log("ReferralManager initialized");
    }

    public static synchronized ReferralManager getInstance() {
        if (instance == null) {
            instance = new ReferralManager();
        }
        return instance;
    }

    // Create a new referral with all details
    public Referral createReferral(String patientId, String referringClinicianId,
                                   String referredToClinicianId, String referringFacilityId,
                                   String referredToFacilityId, String urgencyLevel,
                                   String referralReason, String clinicalSummary,
                                   String requestedInvestigations, String notes) {
        String id = generateId();
        LocalDate now = LocalDate.now();

        Referral ref = new Referral(
                id,
                patientId,
                referringClinicianId,
                referredToClinicianId,
                referringFacilityId,
                referredToFacilityId,
                now,
                urgencyLevel,
                referralReason,
                clinicalSummary,
                requestedInvestigations,
                "Pending",
                null,
                notes,
                now,
                now
        );

        referrals.add(ref);
        registry.put(id, ref);
        pending.offer(ref);

        log("Referral created: " + id + " for patient: " + patientId);
        return ref;
    }

    // Simplified version for basic referrals
    public Referral createSimpleReferral(String patientId, String referringClinicianId,
                                         String referredToFacilityId, String urgencyLevel,
                                         String referralReason, String clinicalSummary, String notes) {
        return createReferral(
                patientId,
                referringClinicianId,
                null,
                null,
                referredToFacilityId,
                urgencyLevel,
                referralReason,
                clinicalSummary,
                null,
                notes
        );
    }

    // Add existing referral (for loading from CSV)
    public void addExistingReferral(Referral ref) {
        if (ref == null || registry.containsKey(ref.getReferralId())) {
            return;
        }

        referrals.add(ref);
        registry.put(ref.getReferralId(), ref);

        String status = ref.getStatus();
        if ("Pending".equalsIgnoreCase(status) || "New".equalsIgnoreCase(status)) {
            pending.offer(ref);
        }

        log("Loaded referral: " + ref.getReferralId() +
                " patient: " + ref.getPatientId() +
                " status: " + ref.getStatus());
    }

    // Send referral and generate all documentation
    public boolean sendReferral(Referral ref, Patient patient,
                                Clinician clinician, Facility facility) {
        if (ref == null || "Sent".equalsIgnoreCase(ref.getStatus())) {
            return false;
        }

        try {
            ref.setStatus("Sent");
            ref.setReferredToFacilityId(facility.getFacilityId());
            ref.setLastUpdated(LocalDate.now());

            writeReferralDoc(ref, patient, clinician, facility);
            writeEmail(ref, patient, clinician, facility);
            writeEhrUpdate(ref, patient);

            pending.remove(ref);
            log("Referral sent: " + ref.getReferralId() + " to: " + facility.getFacilityName());
            return true;

        } catch (Exception e) {
            log("ERROR sending " + ref.getReferralId() + ": " + e.getMessage());
            return false;
        }
    }

    private void writeReferralDoc(Referral ref, Patient pat,
                                  Clinician clin, Facility fac) {
        StringBuilder doc = new StringBuilder();

        doc.append("=====================================\n");
        doc.append("     MEDICAL REFERRAL DOCUMENT\n");
        doc.append("=====================================\n\n");

        doc.append(">> REFERRAL INFO\n");
        doc.append("ID:       ").append(ref.getReferralId()).append("\n");
        doc.append("Date:     ").append(ref.getReferralDate()).append("\n");
        doc.append("Urgency:  ").append(ref.getUrgencyLevel()).append("\n");
        doc.append("Status:   ").append(ref.getStatus()).append("\n\n");

        doc.append(">> PATIENT\n");
        doc.append("Name:     ").append(pat.getFullName()).append("\n");
        doc.append("NHS#:     ").append(pat.getNhsNumber()).append("\n");
        doc.append("DOB:      ").append(pat.getDateOfBirth()).append("\n");
        doc.append("Age:      ").append(pat.getAge()).append(" years\n");
        doc.append("Gender:   ").append(pat.getGender()).append("\n");
        doc.append("Phone:    ").append(pat.getPhoneNumber()).append("\n");
        doc.append("Email:    ").append(pat.getEmail()).append("\n\n");

        doc.append(">> REFERRING CLINICIAN\n");
        doc.append("Name:     Dr. ").append(clin.getFullName()).append("\n");
        doc.append("License:  ").append(clin.getLicenseNumber()).append("\n");
        doc.append("Specialty: ").append(clin.getSpecialization()).append("\n");
        doc.append("Email:    ").append(clin.getEmail()).append("\n\n");

        doc.append(">> DESTINATION FACILITY\n");
        doc.append("Facility: ").append(fac.getFacilityName()).append("\n");
        doc.append("Type:     ").append(fac.getFacilityType()).append("\n");
        doc.append("Address:  ").append(fac.getAddress()).append("\n");
        doc.append("Phone:    ").append(fac.getPhoneNumber()).append("\n");
        doc.append("Email:    ").append(fac.getEmail()).append("\n\n");

        doc.append(">> CLINICAL INFO\n");
        doc.append("Reason:\n").append(nullSafe(ref.getReferralReason())).append("\n\n");
        doc.append("Clinical Summary:\n").append(nullSafe(ref.getClinicalSummary())).append("\n\n");
        doc.append("Requested Investigations:\n").append(nullSafe(ref.getRequestedInvestigations())).append("\n\n");
        doc.append("Additional Notes:\n").append(nullSafe(ref.getNotes())).append("\n\n");

        doc.append(">> MEDICAL HISTORY\n");
        PatientRecord rec = pat.getPatientRecord();
        if (rec != null) {
            doc.append("Allergies:   ").append(listOrNone(rec.getAllergies())).append("\n");
            doc.append("Conditions:  ").append(listOrNone(rec.getConditions())).append("\n");
            doc.append("Medications: ").append(listOrNone(rec.getMedications())).append("\n\n");
        } else {
            doc.append("No medical history available.\n\n");
        }

        doc.append("=====================================\n");
        doc.append("Generated: ").append(now()).append("\n");
        doc.append("=====================================\n\n\n");

        appendToFile(REFERRAL_FILE, doc.toString());
    }

    private void writeEmail(Referral ref, Patient pat,
                            Clinician clin, Facility fac) {
        StringBuilder email = new StringBuilder();

        email.append("=====================================\n");
        email.append("      EMAIL - REFERRAL NOTICE\n");
        email.append("=====================================\n\n");

        email.append("To:      ").append(fac.getEmail()).append("\n");
        email.append("From:    ").append(clin.getEmail()).append("\n");
        email.append("Subject: New Patient Referral - ").append(ref.getUrgencyLevel()).append(" Priority\n");
        email.append("Date:    ").append(now()).append("\n\n");

        email.append("Dear Specialist Team,\n\n");
        email.append("I am referring the following patient for consultation:\n\n");

        email.append("Patient:   ").append(pat.getFullName()).append("\n");
        email.append("NHS#:      ").append(pat.getNhsNumber()).append("\n");
        email.append("Ref ID:    ").append(ref.getReferralId()).append("\n");
        email.append("Urgency:   ").append(ref.getUrgencyLevel()).append("\n");
        email.append("Facility:  ").append(fac.getFacilityName()).append("\n\n");

        email.append("Reason for Referral:\n").append(nullSafe(ref.getReferralReason())).append("\n\n");
        email.append("Clinical Summary:\n").append(nullSafe(ref.getClinicalSummary())).append("\n\n");

        if (ref.getRequestedInvestigations() != null && !ref.getRequestedInvestigations().isEmpty()) {
            email.append("Requested Investigations:\n").append(ref.getRequestedInvestigations()).append("\n\n");
        }

        email.append("Full documentation attached.\n");
        email.append("Patient contact: ").append(pat.getPhoneNumber()).append("\n\n");

        email.append("Best regards,\n");
        email.append("Dr. ").append(clin.getFullName()).append("\n");
        email.append(clin.getSpecialization()).append("\n");
        email.append("License: ").append(clin.getLicenseNumber()).append("\n\n");

        email.append("=====================================\n\n\n");

        appendToFile(EMAIL_FILE, email.toString());
    }

    private void writeEhrUpdate(Referral ref, Patient pat) {
        StringBuilder update = new StringBuilder();

        update.append("=====================================\n");
        update.append("        NHS EHR SYSTEM UPDATE\n");
        update.append("=====================================\n\n");

        update.append("Type:      REFERRAL_SENT\n");
        update.append("Timestamp: ").append(now()).append("\n");
        update.append("NHS#:      ").append(pat.getNhsNumber()).append("\n");
        update.append("Ref ID:    ").append(ref.getReferralId()).append("\n\n");

        update.append("ACTION:    Add referral to patient EHR\n");
        update.append("Facility:  ").append(ref.getReferredToFacilityId()).append("\n");
        update.append("Urgency:   ").append(ref.getUrgencyLevel()).append("\n");
        update.append("Status:    ").append(ref.getStatus()).append("\n");
        update.append("Date:      ").append(ref.getReferralDate()).append("\n");
        update.append("Reason:    ").append(nullSafe(ref.getReferralReason())).append("\n\n");

        update.append("SYNC: SUCCESS\n");
        update.append("=====================================\n\n\n");

        appendToFile(EHR_FILE, update.toString());
    }

    public List<Referral> getAllReferrals() {
        return new ArrayList<>(referrals);
    }

    public Referral getReferralById(String id) {
        return registry.get(id);
    }

    public List<Referral> getPendingReferrals() {
        return new ArrayList<>(pending);
    }

    public List<Referral> getReferralsByPatient(String patientId) {
        List<Referral> result = new ArrayList<>();
        for (Referral r : referrals) {
            if (r.getPatientId().equals(patientId)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Referral> getReferralsByFacility(String facilityId) {
        List<Referral> result = new ArrayList<>();
        for (Referral r : referrals) {
            if (facilityId.equals(r.getReferredToFacilityId()) ||
                    facilityId.equals(r.getReferringFacilityId())) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Referral> getReferralsByClinician(String clinicianId) {
        List<Referral> result = new ArrayList<>();
        for (Referral r : referrals) {
            if (clinicianId.equals(r.getReferringClinicianId()) ||
                    clinicianId.equals(r.getReferredToClinicianId())) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Referral> getReferralsByStatus(String status) {
        List<Referral> result = new ArrayList<>();
        for (Referral r : referrals) {
            if (status.equalsIgnoreCase(r.getStatus())) {
                result.add(r);
            }
        }
        return result;
    }

    public List<String> getAuditLog() {
        return new ArrayList<>(auditLog);
    }

    // Delete a referral
    public boolean removeReferral(String referralId) {
        Referral ref = registry.get(referralId);
        if (ref != null) {
            referrals.remove(ref);
            registry.remove(referralId);
            pending.remove(ref);
            log("Referral deleted: " + referralId);
            return true;
        }
        return false;
    }

    private String generateId() {
        return "R" + String.format("%03d", referrals.size() + 1);
    }

    private String now() {
        return LocalDateTime.now().format(DTF);
    }

    private String nullSafe(String value) {
        return (value != null && !value.isEmpty()) ? value : "N/A";
    }

    private String listOrNone(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "None";
        }
        return String.join(", ", list);
    }

    private void log(String msg) {
        String entry = "[" + now() + "] " + msg;
        auditLog.add(entry);
        appendToFile(AUDIT_FILE, entry + "\n");
    }

    private void appendToFile(String file, String content) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
            out.print(content);
        } catch (IOException e) {
            System.err.println("Write error " + file + ": " + e.getMessage());
        }
    }

    // Clear all data (for testing)
    public void clearAll() {
        referrals.clear();
        registry.clear();
        pending.clear();
        auditLog.clear();
        log("All data cleared");
    }
}