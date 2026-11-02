package com.shop.cart;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CartRepository {

    public record CartLine(long productId, int quantity) {}

    private final JdbcTemplate jdbc;

    public CartRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CartLine> findLines(String customerId) {
        return jdbc.query(
                "select product_id, quantity from cart_items where customer_id = ? order by product_id",
                CartRepository::map,
                customerId);
    }

    public void upsertQuantity(String customerId, long productId, int quantity) {
        jdbc.update(
                """
                insert into cart_items (customer_id, product_id, quantity) values (?, ?, ?)
                on conflict (customer_id, product_id) do update set quantity = excluded.quantity
                """,
                customerId,
                productId,
                quantity);
    }

    public void removeLine(String customerId, long productId) {
        jdbc.update("delete from cart_items where customer_id = ? and product_id = ?", customerId, productId);
    }

    public void clear(String customerId) {
        jdbc.update("delete from cart_items where customer_id = ?", customerId);
    }

    private static CartLine map(ResultSet rs, int rowNum) throws SQLException {
        return new CartLine(rs.getLong("product_id"), rs.getInt("quantity"));
    }
}
