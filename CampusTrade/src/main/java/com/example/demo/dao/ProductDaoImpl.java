package com.example.demo.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
        seller.setEmail(rs.getString("seller_email")); 
        product.setSeller(seller);
        
        return product;
    };

    // 💡 既存の findAll メソッドを検索対応に拡張
    @Override
    public List<Product> findAll() {
        // 条件なしの全件検索（初期表示用など）
        return findByConditions(null, null, null, null, null, null);
    }

    // 💡 動的検索を行うための新しいメソッド
    // keyword: 商品名・説明のキーワード検索
    // tagCategory: 人気タグ用。大・中・小分類のいずれかに一致するものを検索
    // categoryId: 3連選択ボックス（大→中→小）で確定したカテゴリID
    @Override
    public List<Product> findByConditions(String keyword, String tagCategory, Integer categoryId, String price, String delivery, String sort) {
        StringBuilder sql = new StringBuilder(
            "SELECT p.*, c.BigCategory, c.MidCategory, c.SmallCategory, u.email AS seller_email " +
            "FROM products p " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "LEFT JOIN users u ON p.seller_id = u.id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        // 1. キーワード検索 (商品名または商品説明に含む)
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.product_name LIKE ? OR p.description LIKE ?) ");
            String likeParam = "%" + keyword.trim() + "%";
            params.add(likeParam);
            params.add(likeParam);
        }

        // 2. 人気タグからのカテゴリ検索（大・中・小分類のいずれかに一致すればヒット）
        if (tagCategory != null && !tagCategory.trim().isEmpty()) {
            sql.append("AND (c.BigCategory = ? OR c.MidCategory = ? OR c.SmallCategory = ?) ");
            params.add(tagCategory.trim());
            params.add(tagCategory.trim());
            params.add(tagCategory.trim());
        }

        // 2-2. 3連選択ボックスで確定したカテゴリIDによる絞り込み
        if (categoryId != null) {
            sql.append("AND c.id = ? ");
            params.add(categoryId);
        }

        // 3. 価格帯抽出 (～1000円、～3000円、～5000円)
        if (price != null && !price.trim().isEmpty()) {
            try {
                int maxPrice = Integer.parseInt(price);
                sql.append("AND p.price <= ? ");
                params.add(maxPrice);
            } catch (NumberFormatException e) {
                // 不正な値は無視
            }
        }

        // 4. 受渡方法 (現状のproductsテーブルにカラムがない場合は適宜スキップ、または拡張用)
        // ※ 画面の仕様上、今回は description等に部分一致させるか、カラム追加まで一旦条件分岐のみ用意
        if (delivery != null && !delivery.trim().isEmpty()) {
            if ("hand".equals(delivery)) {
                sql.append("AND p.description LIKE ? ");
                params.add("%手渡し%");
            } else if ("delivery".equals(delivery)) {
                sql.append("AND p.description LIKE ? ");
                params.add("%配送%");
            }
        }

        // 5. 並び替え (新着順、価格が安い順、高い順)
        if (sort != null && !sort.trim().isEmpty()) {
            if ("new".equals(sort)) {
                sql.append("ORDER BY p.id DESC ");
            } else if ("priceAsc".equals(sort)) {
                sql.append("ORDER BY p.price ASC ");
            } else if ("priceDesc".equals(sort)) {
                sql.append("ORDER BY p.price DESC ");
            }
        } else {
            // デフォルトはIDの降順（新着順）
            sql.append("ORDER BY p.id DESC ");
        }

        return jdbcTemplate.query(sql.toString(), productRowMapper, params.toArray());
    }

    // 💡 大・中・小の3連選択ボックスをJS側で構築するための元データ一覧を取得
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
}