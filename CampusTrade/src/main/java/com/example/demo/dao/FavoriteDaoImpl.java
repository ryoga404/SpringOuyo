package com.example.demo.dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Product;
import com.example.demo.model.User;

@Repository
public class FavoriteDaoImpl implements FavoriteDao {

    private final JdbcTemplate jdbcTemplate;

    public FavoriteDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 💡 実際のDBテーブル名は user_favorite（user_id, product_id の複合主キーのみ）
    @Override
    public boolean exists(Long userId, Long productId) {
        String sql = "SELECT COUNT(*) FROM user_favorite WHERE user_id = ? AND product_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, productId);
        return count != null && count > 0;
    }

    @Override
    public void add(Long userId, Long productId) {
        String sql = "INSERT IGNORE INTO user_favorite (user_id, product_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, productId);
    }

    @Override
    public void remove(Long userId, Long productId) {
        String sql = "DELETE FROM user_favorite WHERE user_id = ? AND product_id = ?";
        jdbcTemplate.update(sql, userId, productId);
    }

    private final RowMapper<Product> productRowMapper = (rs, rowNum) -> {
        Product product = new Product(
                rs.getLong("id"),
                rs.getString("product_name"),
                rs.getString("description"),
                rs.getInt("price"),
                rs.getInt("category_id"),
                rs.getString("status"),
                rs.getLong("seller_id"),
                rs.getObject("buyer_id") != null ? rs.getLong("buyer_id") : null
        );
        String big = rs.getString("BigCategory");
        String mid = rs.getString("MidCategory");
        String small = rs.getString("SmallCategory");
        StringBuilder catBuilder = new StringBuilder();
        if (big != null) catBuilder.append(big);
        if (mid != null) catBuilder.append(" > ").append(mid);
        if (small != null) catBuilder.append(" > ").append(small);
        product.setCategory(catBuilder.toString().isEmpty() ? "未分類" : catBuilder.toString());

        User seller = new User();
        seller.setId(rs.getLong("seller_id"));
        seller.setEmail(rs.getString("seller_email"));
        seller.setNickname(rs.getString("seller_nickname"));
        product.setSeller(seller);

        return product;
    };

    @Override
    public List<Product> findProductsByUserId(Long userId) {
        String sql = "SELECT p.*, c.BigCategory, c.MidCategory, c.SmallCategory, " +
                "u.email AS seller_email, u.nickname AS seller_nickname " +
                "FROM user_favorite f " +
                "JOIN products p ON f.product_id = p.id " +
                "LEFT JOIN category c ON p.category_id = c.id " +
                "LEFT JOIN users u ON p.seller_id = u.id " +
                "WHERE f.user_id = ? " +
                "ORDER BY p.id DESC";
        return jdbcTemplate.query(sql, productRowMapper, userId);
    }
}
