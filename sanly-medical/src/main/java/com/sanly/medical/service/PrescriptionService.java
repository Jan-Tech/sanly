package com.sanly.medical.service;

import com.sanly.medical.dto.request.DispensePrescriptionRequest;
import com.sanly.medical.dto.request.IssuePrescriptionRequest;
import com.sanly.medical.dto.response.DispensingResponse;
import com.sanly.medical.dto.response.PrescriptionResponse;

import java.util.List;

public interface PrescriptionService {
    PrescriptionResponse issue(IssuePrescriptionRequest req, Long doctorId, Long clinicId);
    PrescriptionResponse getByCode(String prescriptionCode);
    List<PrescriptionResponse> getByCitizen(String nationalId);
    PrescriptionResponse cancel(String prescriptionCode, Long requestingDoctorId);
    DispensingResponse dispense(String prescriptionCode, String pharmacyCode,
                                DispensePrescriptionRequest req);
}
