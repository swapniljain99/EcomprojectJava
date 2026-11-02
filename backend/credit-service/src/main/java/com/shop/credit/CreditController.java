package com.shop.credit;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/credit")
public class CreditController {

    public record CreditCheckRequest(String customerId, long amountCents) {}

    public record CreditDecision(boolean approved, long creditLimitCents) {}

    private final CreditAccountRepository accounts;

    public CreditController(CreditAccountRepository accounts) {
        this.accounts = accounts;
    }

    @PostMapping("/check")
    public CreditDecision check(@RequestBody CreditCheckRequest request) {
        if (request.customerId() == null || request.customerId().isBlank() || request.amountCents() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId and a positive amountCents are required");
        }
        int limit = accounts.findCreditLimitCents(request.customerId()).orElse(0);
        return new CreditDecision(limit > 0 && request.amountCents() <= limit, limit);
    }
}
