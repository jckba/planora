package com.planora.backend.purchase.api.dto.request;

import com.planora.backend.purchase.application.create.CreatePurchaseCommand;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreatePurchaseRequest(
    Instant expectedDate,
    @Size(max = 500)
    String notes
) {
    public CreatePurchaseCommand toCommand() {
        return new CreatePurchaseCommand(expectedDate, notes);
    }
}
