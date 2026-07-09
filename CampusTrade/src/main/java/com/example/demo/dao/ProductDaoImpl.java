package com.example.demo.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.model.User;

@Repository
public class ProductDaoImpl implements ProductDao {

    private final JdbcTemplate jdbcTemplate;

    public ProductDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

        // 大分類・中分類・小分類を結合したものを仮のカテゴリ文字列として設定
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

    private static final String BASE_SELECT =
            "SELECT p.*, c.BigCategory, c.MidCategory, c.SmallCategory, " +
            "u.email AS seller_email, u.nickname AS seller_nickname " +
            "FROM products p " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "LEFT JOIN users u ON p.seller_id = u.id ";

    @Override
    public List<Product> findAll() {
        return findByConditions(null, null, null, null, null, null);
    }

    @Override
    public List<Product> findByConditions(String keyword, String tagCategory, Integer categoryId, String price, String delivery, String sort) {
        // 要件定義書 3.2：一覧には「販売中（OPEN）」の商品のみを表示する
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE p.status = 'OPEN' ");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.product_name LIKE ? OR p.description LIKE ?) ");
            String likeParam = "%" + keyword.trim() + "%";
            params.add(likeParam);
            params.add(likeParam);
        }

        if (tagCategory != null && !tagCategory.trim().isEmpty()) {
            sql.append("AND (c.BigCategory = ? OR c.MidCategory = ? OR c.SmallCategory = ?) ");
            params.add(tagCategory.trim());
            params.add(tagCategory.trim());
            params.add(tagCategory.trim());
        }

        if (categoryId != null) {
            sql.append("AND c.id = ? ");
            params.add(categoryId);
        }

        if (price != null && !price.trim().isEmpty()) {
            try {
                int maxPrice = Integer.parseInt(price);
                sql.append("AND p.price <= ? ");
                params.add(maxPrice);
            } catch (NumberFormatException e) {
                // 不正な値は無視
            }
        }

        if (delivery != null && !delivery.trim().isEmpty()) {
            if ("hand".equals(delivery)) {
                sql.append("AND p.description LIKE ? ");
                params.add("%手渡し%");
            } else if ("delivery".equals(delivery)) {
                sql.append("AND p.description LIKE ? ");
                params.add("%配送%");
            }
        }

        if (sort != null && !sort.trim().isEmpty()) {
            if ("new".equals(sort)) {
                sql.append("ORDER BY p.id DESC ");
            } else if ("priceAsc".equals(sort)) {
                sql.append("ORDER BY p.price ASC ");
            } else if ("priceDesc".equals(sort)) {
                sql.append("ORDER BY p.price DESC ");
            }
        } else {
            sql.append("ORDER BY p.id DESC ");
        }

        return jdbcTemplate.query(sql.toString(), productRowMapper, params.toArray());
    }

    @Override
    public List<Category> findAllCategories() {
        String sql = "SELECT id, BigCategory, MidCategory, SmallCategory FROM category ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Category(
                rs.getInt("id"),
                rs.getString("BigCategory"),
                rs.getString("MidCategory"),
                rs.getString("SmallCategory")
        ));
    }

    @Override
    public Product findById(Long id) {
        String sql = BASE_SELECT + "WHERE p.id = ?";
        List<Product> results = jdbcTemplate.query(sql, productRowMapper, id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public void updateStatus(Long id, String status, Long buyerId) {
        String sql = "UPDATE products SET status = ?, buyer_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, buyerId, id);
    }

    // ⭕ 商品出品：新規登録
    @Override
    public Long save(Product product) {
        String sql = "INSERT INTO products (product_name, description, price, category_id, status, seller_id, buyer_id) " +
                "VALUES (?, ?, ?, ?, 'OPEN', ?, NULL)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, product.getProductName());
            ps.setString(2, product.getDescription());
            ps.setInt(3, product.getPrice());
            ps.setInt(4, product.getCategoryId());
            ps.setLong(5, product.getSellerId());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    // ⭕ 商品編集：出品者本人の商品情報を更新（ステータス・出品者・購入者は変更しない）
    @Override
    public void update(Product product) {
        String sql = "UPDATE products SET product_name = ?, description = ?, price = ?, category_id = ? " +
                "WHERE id = ? AND seller_id = ?";
        jdbcTemplate.update(sql,
                product.getProductName(), product.getDescription(), product.getPrice(), product.getCategoryId(),
                product.getId(), product.getSellerId());
    }

    // ⭕ 商品削除：外部キー制約があるため、紐づく messages / user_favorite を先に削除する
    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM messages WHERE product_id = ?", id);
        jdbcTemplate.update("DELETE FROM user_favorite WHERE product_id = ?", id);
        jdbcTemplate.update("DELETE FROM products WHERE id = ?", id);
    }

    @Override
    public List<Product> findBySellerId(Long sellerId) {
        String sql = BASE_SELECT + "WHERE p.seller_id = ? ORDER BY p.id DESC";
        return jdbcTemplate.query(sql, productRowMapper, sellerId);
    }

    @Override
    public List<Product> findPurchaseHistory(Long buyerId) {
        String sql = BASE_SELECT + "WHERE p.buyer_id = ? AND p.status = 'CLOSED' ORDER BY p.id DESC";
        return jdbcTemplate.query(sql, productRowMapper, buyerId);
    }

    @Override
    public List<Product> findNegotiations(Long userId) {
        String sql = BASE_SELECT + "WHERE p.status = 'LOCKED' AND (p.seller_id = ? OR p.buyer_id = ?) ORDER BY p.id DESC";
        return jdbcTemplate.query(sql, productRowMapper, userId, userId);
    }

    @Override
    public List<Product> findAllForAdmin() {
        String sql = BASE_SELECT + "ORDER BY p.id DESC";
        return jdbcTemplate.query(sql, productRowMapper);
    }

    @Override
    public void banProduct(Long id) {
        String sql = "UPDATE products SET status = '禁止' WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
