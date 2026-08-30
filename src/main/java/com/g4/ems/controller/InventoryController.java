package com.g4.ems.controller;

import com.g4.ems.dto.ConsumeInventoryRequest;
import com.g4.ems.service.ConstructionFinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final ConstructionFinanceService constructionFinanceService;

    @PostMapping("/consume")
    @ResponseStatus(HttpStatus.OK)
    public void consumeInventory(@Valid @RequestBody ConsumeInventoryRequest request) {
        constructionFinanceService.consumeInventory(request);
    }
}
