package com.sanly.customs.controller;

import com.sanly.customs.dto.request.AddCargoItemRequest;
import com.sanly.customs.dto.response.CargoItemResponse;
import com.sanly.customs.service.CargoItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customs/declarations/{declarationCode}/items")
@RequiredArgsConstructor
public class CargoItemController {
    private final CargoItemService cargoItemService;

    @PostMapping
    public ResponseEntity<CargoItemResponse> addItem(@PathVariable String declarationCode,
                                                      @RequestBody AddCargoItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cargoItemService.addItem(declarationCode, req));
    }

    @GetMapping
    public ResponseEntity<List<CargoItemResponse>> listItems(@PathVariable String declarationCode) {
        return ResponseEntity.ok(cargoItemService.findByDeclaration(declarationCode));
    }
}
