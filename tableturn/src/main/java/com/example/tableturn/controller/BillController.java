package com.example.tableturn.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tableturn.dto.BillResponse;
import com.example.tableturn.dto.CreateBillRequest;
import com.example.tableturn.dto.TableResponse;
import com.example.tableturn.service.TableTurnService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bills")
@Tag(name = "5. Bills")
public class BillController {

    private final TableTurnService service;

    public BillController(TableTurnService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Generate bill for table", description = "Bill total is calculated only by the service from all unbilled order items in the current sitting.")
    public BillResponse generate(@Valid @RequestBody CreateBillRequest request) {
        return service.generateBill(request);
    }

    @GetMapping
    @Operation(summary = "Get all bills", description = "No parameters required. Click Try it out, then Execute.")
    public List<BillResponse> getAll() {
        return service.getBills();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bill by id")
    public BillResponse getById(@PathVariable Long id) {
        return service.getBillById(id);
    }

    @PostMapping("/tables/{tableId}/vacate")
    @Operation(summary = "Vacate billed table and make it FREE")
    public TableResponse vacate(@PathVariable Long tableId) {
        return service.vacateTable(tableId);
    }
}
