package controller;

import model.*;
import java.util.List;

public class ReferralController {

    private ReferralManager manager;
    private PatientController patCtrl;
    private ClinicianController clinCtrl;
    private FacilityController facCtrl;
    private String dataFilename;

    public ReferralController(PatientController pc, ClinicianController cc,
                              FacilityController fc) {
        manager = ReferralManager.getInstance();
        patCtrl = pc;
        clinCtrl = cc;
        facCtrl = fc;
    }

    public void loadReferrals(String filename) {
        dataFilename = filename;
        List<Referral> refs = DataLoader.loadReferrals(filename);

        for (Referral ref : refs) {
            manager.addExistingReferral(ref);
        }

        System.out.println("Loaded " + refs.size() + " referrals");
    }

    private void saveToFile() {
        if (dataFilename != null) {
            DataLoader.saveReferrals(dataFilename, manager.getAllReferrals());
        }
    }

    public int getReferralCount() {
        return manager.getAllReferrals().size();
    }

    // Create a complete referral with all details
    public Referral createReferral(String patientId, String referringClinicianId,
                                   String referredToClinicianId, String referringFacilityId,
                                   String referredToFacilityId, String urgencyLevel,
                                   String referralReason, String clinicalSummary,
                                   String requestedInvestigations, String notes) {
        Referral ref = manager.createReferral(
                patientId,
                referringClinicianId,
                referredToClinicianId,
                referringFacilityId,
                referredToFacilityId,
                urgencyLevel,
                referralReason,
                clinicalSummary,
                requestedInvestigations,
                notes
        );
        saveToFile();
        return ref;
    }

    // Simpler version for quick referrals
    public Referral createSimpleReferral(String patientId, String referringClinicianId,
                                         String referredToFacilityId, String urgencyLevel,
                                         String referralReason, String clinicalSummary,
                                         String notes) {
        Referral ref = manager.createSimpleReferral(
                patientId,
                referringClinicianId,
                referredToFacilityId,
                urgencyLevel,
                referralReason,
                clinicalSummary,
                notes
        );
        saveToFile();
        return ref;
    }

    public void addReferral(Referral referral) {
        manager.addExistingReferral(referral);
        saveToFile();
    }

    public boolean updateReferral(Referral referral) {
        Referral existing = manager.getReferralById(referral.getReferralId());
        if (existing != null) {
            manager.removeReferral(existing.getReferralId());
            manager.addExistingReferral(referral);
            saveToFile();
            return true;
        }
        return false;
    }

    public boolean deleteReferral(String referralId) {
        boolean removed = manager.removeReferral(referralId);
        if (removed) {
            saveToFile();
            System.out.println("Referral deleted successfully: " + referralId);
            return true;
        }
        System.err.println("Referral not found for deletion: " + referralId);
        return false;
    }

    public boolean sendReferral(String refId, String facilityId) {
        Referral ref = manager.getReferralById(refId);
        if (ref == null) {
            System.err.println("Referral not found: " + refId);
            return false;
        }

        Patient pat = patCtrl.getPatientById(ref.getPatientId());
        Clinician clin = clinCtrl.getClinicianById(ref.getReferringClinicianId());
        Facility fac = facCtrl.getFacilityById(facilityId);

        // Validate all entities exist
        if (pat == null) {
            System.err.println("Patient not found: " + ref.getPatientId());
            return false;
        }
        if (clin == null) {
            System.err.println("Clinician not found: " + ref.getReferringClinicianId());
            return false;
        }
        if (fac == null) {
            System.err.println("Facility not found: " + facilityId);
            return false;
        }

        boolean sent = manager.sendReferral(ref, pat, clin, fac);
        if (sent) {
            saveToFile();
        }
        return sent;
    }

    public List<Referral> getAllReferrals() {
        return manager.getAllReferrals();
    }

    public Referral getReferralById(String id) {
        return manager.getReferralById(id);
    }

    public List<Referral> getPendingReferrals() {
        return manager.getPendingReferrals();
    }

    public List<Referral> getReferralsByPatient(String patientId) {
        return manager.getReferralsByPatient(patientId);
    }

    public List<Referral> getReferralsByFacility(String facilityId) {
        return manager.getReferralsByFacility(facilityId);
    }

    public List<Referral> getReferralsByClinician(String clinicianId) {
        return manager.getReferralsByClinician(clinicianId);
    }

    public List<Referral> getReferralsByStatus(String status) {
        return manager.getReferralsByStatus(status);
    }

    public List<String> getAuditLog() {
        return manager.getAuditLog();
    }
}