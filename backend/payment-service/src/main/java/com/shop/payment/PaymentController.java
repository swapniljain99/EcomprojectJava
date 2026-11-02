package com.shop.payment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    public record PaymentRequest(String customerId, long amountCents) {}

    public record PaymentResult(long id, String status) {}

    private final PaymentRepository payments;

    public PaymentController(PaymentRepository payments) {
        this.payments = payments;
    }

    @PostMapping
    public PaymentResult charge(@RequestBody PaymentRequest request) {
        if (request.customerId() == null || request.customerId().isBlank() || request.amountCents() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId and a positive amountCents are required");
        }
        String status = "APPROVED";
        long id = payments.save(request.customerId(), request.amountCents(), status);
        return new PaymentResult(id, status);
    }
}
