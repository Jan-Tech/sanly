package com.sanly.civil.service;

import com.sanly.civil.client.*;
import com.sanly.civil.entity.*;
import com.sanly.civil.repository.PendingEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Coordinates downstream life-event automation after civil registrations.
 *
 * All public methods are @Async("lifeEventExecutor") — they run in a separate thread
 * after the registration transaction commits, so a downstream failure never rolls back
 * or delays the original registration.
 *
 * Each downstream call is individually try/caught so one failure does not block others.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LifeEventPublisherService {

    private final TaxServiceClient          taxServiceClient;
    private final DmvServiceClient          dmvServiceClient;
    private final BusinessServiceClient     businessServiceClient;
    private final MedicalServiceClient      medicalServiceClient;
    private final EducationNotifier         educationNotifier;
    private final LandServiceClient         landServiceClient;
    private final SocialServiceClient       socialServiceClient;
    private final VehicleServiceClient      vehicleServiceClient;
    private final PensionServiceClient      pensionServiceClient;
    private final NotificationClient        notificationClient;
    private final PendingEnrollmentRepository enrollmentRepository;

    // ── BIRTH ─────────────────────────────────────────────────────────────────

    @Async("lifeEventExecutor")
    @Transactional
    public void onBirthRegistered(BirthRecord record) {
        log.info("Life event: BIRTH — child NIN={}", record.getChildNationalId());

        String childNin  = record.getChildNationalId();
        String motherNin = record.getMotherNationalId();
        String fatherNin = record.getFatherNationalId();
        String childName = record.getChildFirstName() + " " + record.getChildLastName();
        String birthDate = record.getDateOfBirth().toString();
        int schoolYear   = record.getDateOfBirth().getYear() + 6;

        // 1. Child benefit (tax side)
        try {
            taxServiceClient.triggerChildBenefit(childNin, motherNin, fatherNin,
                    record.getDateOfBirth());
        } catch (Exception ex) {
            log.warn("Child benefit trigger failed: {}", ex.getMessage());
        }

        // 1b. Social child benefit for mother
        if (motherNin != null) {
            try { socialServiceClient.autoTriggerBenefit(motherNin, "CHILD_BENEFIT",
                    "Birth of child " + childNin); }
            catch (Exception ex) { log.warn("Social child benefit trigger failed: {}", ex.getMessage()); }
        }

        // 2. Notify parents about automatic benefit registration
        Map<String, String> benefitMeta = Map.of(
                "childFullName",    childName,
                "childNationalId",  childNin,
                "birthDate",        birthDate
        );
        if (motherNin != null) {
            try { notificationClient.send(motherNin, "CHILD_BENEFIT_TRIGGERED", "EN", benefitMeta); }
            catch (Exception ex) { log.warn("Mother benefit notification failed: {}", ex.getMessage()); }
        }
        if (fatherNin != null) {
            try { notificationClient.send(fatherNin, "CHILD_BENEFIT_TRIGGERED", "EN", benefitMeta); }
            catch (Exception ex) { log.warn("Father benefit notification failed: {}", ex.getMessage()); }
        }

        // 3. Vaccination schedule
        try {
            medicalServiceClient.scheduleVaccinations(childNin, record.getDateOfBirth(),
                    motherNin, fatherNin);
        } catch (Exception ex) {
            log.warn("Vaccination schedule failed: {}", ex.getMessage());
        }

        // 4. School enrollment queue
        try {
            if (!enrollmentRepository.existsByChildNationalId(childNin)) {
                enrollmentRepository.save(PendingEnrollment.builder()
                        .childNationalId(childNin)
                        .motherNationalId(motherNin)
                        .fatherNationalId(fatherNin)
                        .childFullName(childName)
                        .expectedSchoolYear(schoolYear)
                        .status(EnrollmentStatus.QUEUED)
                        .build());
                log.info("Child NIN={} queued for school enrollment year {}", childNin, schoolYear);
            }
            educationNotifier.queueForEnrollment(childNin, schoolYear);
        } catch (Exception ex) {
            log.warn("Enrollment queue failed: {}", ex.getMessage());
        }
    }

    // ── DEATH ─────────────────────────────────────────────────────────────────

    @Async("lifeEventExecutor")
    public void onDeathRegistered(DeathRecord record) {
        log.info("Life event: DEATH — deceased NIN={}", record.getDeceasedNationalId());

        String deceasedNin  = record.getDeceasedNationalId();
        String deceasedName = record.getDeceasedFullName();
        String dateOfDeath  = record.getDateOfDeath().toString();

        // 1. Revoke driving licenses
        try { dmvServiceClient.revokeDeceasedLicenses(deceasedNin); }
        catch (Exception ex) { log.warn("License revocation failed: {}", ex.getMessage()); }

        // 2. Suspend businesses
        try { businessServiceClient.handleOwnerDeceased(deceasedNin, deceasedName, dateOfDeath); }
        catch (Exception ex) { log.warn("Business suspension failed: {}", ex.getMessage()); }

        // 3. Cancel benefits
        try { taxServiceClient.cancelBenefits(deceasedNin); }
        catch (Exception ex) { log.warn("Benefit cancellation failed: {}", ex.getMessage()); }

        // 4. Deregister taxpayer
        try { taxServiceClient.deregisterTaxpayer(deceasedNin); }
        catch (Exception ex) { log.warn("Taxpayer deregistration failed: {}", ex.getMessage()); }

        // 5. Transfer property ownerships to INHERITED state
        try { landServiceClient.handleOwnerDeceased(deceasedNin); }
        catch (Exception ex) { log.warn("Land ownership handling failed: {}", ex.getMessage()); }

        // 5b. Cancel all social benefits for deceased
        try { socialServiceClient.cancelAllBenefits(deceasedNin); }
        catch (Exception ex) { log.warn("Social benefit cancellation failed: {}", ex.getMessage()); }

        // 5c. Handle vehicle ownership inheritance
        try { vehicleServiceClient.handleOwnerDeceased(deceasedNin); }
        catch (Exception ex) { log.warn("Vehicle ownership handling failed: {}", ex.getMessage()); }

        // 5d. Close pension account
        try { pensionServiceClient.handleOwnerDeceased(deceasedNin); }
        catch (Exception ex) { log.warn("Pension account closure failed: {}", ex.getMessage()); }

        // 6. Notify — send to deceased NIN (in-app record even if citizen is deceased, for audit)
        try {
            notificationClient.send(deceasedNin, "BENEFITS_CANCELLED_DECEASED", "EN",
                    Map.of(
                            "deceasedFullName", deceasedName,
                            "deceasedNin",      deceasedNin,
                            "cancelledAt",      dateOfDeath
                    ));
        } catch (Exception ex) {
            log.warn("Death notification failed: {}", ex.getMessage());
        }
    }

    // ── MARRIAGE ──────────────────────────────────────────────────────────────

    @Async("lifeEventExecutor")
    public void onMarriageRegistered(MarriageRecord record) {
        log.info("Life event: MARRIAGE — spouses {} + {}",
                record.getSpouse1NationalId(), record.getSpouse2NationalId());

        String spouse1 = record.getSpouse1NationalId();
        String spouse2 = record.getSpouse2NationalId();
        String date    = record.getMarriageDate().toString();

        // Update marital status in tax for both spouses
        try { taxServiceClient.updateMaritalStatus(spouse1, "MARRIED", spouse2); }
        catch (Exception ex) { log.warn("Marital status update failed (spouse1): {}", ex.getMessage()); }

        try { taxServiceClient.updateMaritalStatus(spouse2, "MARRIED", spouse1); }
        catch (Exception ex) { log.warn("Marital status update failed (spouse2): {}", ex.getMessage()); }

        // Notify both spouses about tax implications
        Map<String, String> meta = Map.of("marriageDate", date);
        try { notificationClient.send(spouse1, "MARRIAGE_TAX_INFO", "EN", meta); }
        catch (Exception ex) { log.warn("Marriage tax notification failed (spouse1): {}", ex.getMessage()); }

        try { notificationClient.send(spouse2, "MARRIAGE_TAX_INFO", "EN", meta); }
        catch (Exception ex) { log.warn("Marriage tax notification failed (spouse2): {}", ex.getMessage()); }

        // Notify both spouses about potential social benefit eligibility
        try { notificationClient.send(spouse1, "MARRIAGE_BENEFIT_INFO", "TK", meta); }
        catch (Exception ex) { log.warn("Marriage benefit info notification failed (spouse1): {}", ex.getMessage()); }

        try { notificationClient.send(spouse2, "MARRIAGE_BENEFIT_INFO", "TK", meta); }
        catch (Exception ex) { log.warn("Marriage benefit info notification failed (spouse2): {}", ex.getMessage()); }
    }

    // ── DIVORCE ───────────────────────────────────────────────────────────────

    @Async("lifeEventExecutor")
    public void onMarriageDissolved(MarriageRecord record) {
        log.info("Life event: DIVORCE — spouses {} + {}",
                record.getSpouse1NationalId(), record.getSpouse2NationalId());

        String spouse1    = record.getSpouse1NationalId();
        String spouse2    = record.getSpouse2NationalId();
        String divorceDate = record.getDissolutionDate() != null
                ? record.getDissolutionDate().toString() : "today";

        try { taxServiceClient.updateMaritalStatus(spouse1, "DIVORCED", null); }
        catch (Exception ex) { log.warn("Marital status update failed (spouse1): {}", ex.getMessage()); }

        try { taxServiceClient.updateMaritalStatus(spouse2, "DIVORCED", null); }
        catch (Exception ex) { log.warn("Marital status update failed (spouse2): {}", ex.getMessage()); }

        Map<String, String> meta = Map.of("divorceDate", divorceDate);
        try { notificationClient.send(spouse1, "DIVORCE_TAX_INFO", "EN", meta); }
        catch (Exception ex) { log.warn("Divorce notification failed (spouse1): {}", ex.getMessage()); }

        try { notificationClient.send(spouse2, "DIVORCE_TAX_INFO", "EN", meta); }
        catch (Exception ex) { log.warn("Divorce notification failed (spouse2): {}", ex.getMessage()); }
    }
}
