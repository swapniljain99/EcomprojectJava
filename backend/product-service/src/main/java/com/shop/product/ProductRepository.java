package com.shop.product;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    public record Product(long id, String name, String description, int priceCents, int stock, String imageUrl) {}

    private static final String COLUMNS = "select id, name, description, price_cents, stock, image_url from products";

    private final JdbcTemplate jdbc;

    public ProductRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Product> findAll() {
        return jdbc.query(COLUMNS + " order by id", ProductRepository::map);
    }

    public Optional<Product> findById(long id) {
        return jdbc.query(COLUMNS + " where id = ?", ProductRepository::map, id).stream().findFirst();
    }

    private static Product map(ResultSet rs, int rowNum) throws SQLException {
        return new Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getInt("price_cents"),
                rs.getInt("stock"),
                rs.getString("image_url"));
    }
}
