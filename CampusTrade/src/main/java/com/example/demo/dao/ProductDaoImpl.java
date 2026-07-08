package com.example.demo.dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Product;
import com.example.demo.model.User;

@Repository
public class ProductDaoImpl implements ProductDao {

    private final JdbcTemplate jdbcTemplate;

    public ProductDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // テーブル結合して、カテゴリ名や出品者のメールアドレス（ニックネーム代わり）を同時に取得
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
        
        // HTMLの th:text="${product.category}" に対応させる
        product.setCategory(rs.getString("BigCategory")); // または MidCategory / SmallCategory
        
        // HTMLの th:text="${product.seller.nickname}" に対応させるためのダミーUserオブジェクト
        User seller = new User();
        // 現状Userにnicknameがないため、emailを表示用ニックネームとして代用
        seller.setEmail(rs.getString("seller_email")); 
        product.setSeller(seller);
        
        return product;
    };

    @Override
    public List<Product> findAll() {
        String sql = "SELECT p.*, c.BigCategory, u.email AS seller_email " +
                     "FROM products p " +
                     "LEFT JOIN category c ON p.category_id = c.id " +
                     "LEFT JOIN users u ON p.seller_id = u.id";
        return jdbcTemplate.query(sql, productRowMapper);
    }
}