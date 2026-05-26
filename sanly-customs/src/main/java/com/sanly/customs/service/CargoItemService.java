package com.sanly.customs.service;

import com.sanly.customs.dto.request.AddCargoItemRequest;
import com.sanly.customs.dto.response.CargoItemResponse;
import com.sanly.customs.entity.CargoItem;
import com.sanly.customs.entity.CustomsDeclaration;
import com.sanly.customs.exception.RecordNotFoundException;
import com.sanly.customs.repository.CargoItemRepository;
import com.sanly.customs.repository.CustomsDeclarationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CargoItemService {
    private final CargoItemRepository itemRepo;
    private final CustomsDeclarationRepository declarationRepo;

    @Transactional
    public CargoItemResponse addItem(String declarationCode, AddCargoItemRequest req) {
        CustomsDeclaration decl = declarationRepo.findByDeclarationCode(declarationCode)
                .orElseThrow(() -> new RecordNotFoundException("Declaration not found: " + declarationCode));

        CargoItem item = CargoItem.builder()
                .declarationCode(declarationCode)
                .itemDescription(req.itemDescription())
                .hsCode(req.hsCode())
                .quantity(req.quantity())
                .unit(req.unit())
                .unitValue(req.unitValue())
                .totalValue(req.totalValue())
                .countryOfOrigin(req.countryOfOrigin())
                .build();
        return CargoItemResponse.from(itemRepo.save(item));
    }

    public List<CargoItemResponse> findByDeclaration(String declarationCode) {
        return itemRepo.findByDeclarationCode(declarationCode)
                .stream().map(CargoItemResponse::from).toList();
    }
}
