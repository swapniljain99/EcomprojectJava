package com.shop.credit;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CreditAccountRepository {

    private final JdbcTemplate jdbc;

    public CreditAccountRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Integer> findCreditLimitCents(String customerId) {
        return jdbc.query(
                "select credit_limit_cents from credit_accounts where customer_id = ?",
                (rs, rowNum) -> rs.getInt("credit_limit_cents"),
                customerId).stream().findFirst();
    }
}
