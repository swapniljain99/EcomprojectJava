package com.shop.order;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    public record OrderLine(long productId, int quantity, int unitPriceCents) {}

    public record OrderSummary(long id, long totalCents, String status, Instant createdAt) {}

    private final JdbcTemplate jdbc;

    public OrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long save(String customerId, long totalCents, List<OrderLine> lines) {
        Long orderId = jdbc.queryForObject(
                "insert into orders (customer_id, total_cents, status) values (?, ?, 'PLACED') returning id",
                Long.class,
                customerId,
                totalCents);
        for (OrderLine line : lines) {
            jdbc.update(
                    "insert into order_items (order_id, product_id, quantity, unit_price_cents) values (?, ?, ?, ?)",
                    orderId,
                    line.productId(),
                    line.quantity(),
                    line.unitPriceCents());
        }
        return orderId;
    }

    public List<OrderSummary> findRecentByCustomer(String customerId) {
        return jdbc.query(
                "select id, total_cents, status, created_at from orders where customer_id = ? order by created_at desc limit 50",
                OrderRepository::map,
                customerId);
    }

    private static OrderSummary map(ResultSet rs, int rowNum) throws SQLException {
        return new OrderSummary(
                rs.getLong("id"),
                rs.getLong("total_cents"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toInstant());
    }
}
