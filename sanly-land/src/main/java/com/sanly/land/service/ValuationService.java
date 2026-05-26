package com.sanly.land.service;

import com.sanly.land.config.UserDetailsImpl;
import com.sanly.land.dto.request.AddValuationRequest;
import com.sanly.land.dto.response.ValuationResponse;
import com.sanly.land.entity.PropertyValuation;
import com.sanly.land.exception.RecordNotFoundException;
import com.sanly.land.repository.PropertyRepository;
import com.sanly.land.repository.PropertyValuationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ValuationService {
    private final PropertyValuationRepository valuationRepository;
    private final PropertyRepository propertyRepository;

    @Transactional
    public ValuationResponse add(AddValuationRequest req, Authentication auth) {
        if (!propertyRepository.existsByCadastralNumber(req.cadastralNumber())) {
            throw new RecordNotFoundException("Property not found: " + req.cadastralNumber());
        }
        PropertyValuation v = PropertyValuation.builder()
                .cadastralNumber(req.cadastralNumber())
                .valuationAmount(req.valuationAmount())
                .valuationDate(req.valuationDate())
                .purpose(req.purpose())
                .valuedByOfficerId(auth != null
                        ? ((UserDetailsImpl) auth.getPrincipal()).getOfficerId() : null)
                .build();
        return ValuationResponse.from(valuationRepository.save(v));
    }

    public List<ValuationResponse> findByProperty(String cadastralNumber) {
        return valuationRepository.findByCadastralNumberOrderByValuationDateDesc(cadastralNumber)
                .stream().map(ValuationResponse::from).toList();
    }
}
