package com.shop.payment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbc;

    public PaymentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long save(String customerId, long amountCents, String status) {
        Long id = jdbc.queryForObject(
                "insert into payments (customer_id, amount_cents, status) values (?, ?, ?) returning id",
                Long.class,
                customerId,
                amountCents,
                status);
        return id;
    }
}
