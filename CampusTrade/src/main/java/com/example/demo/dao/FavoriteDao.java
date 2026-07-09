package com.example.demo.dao;

import java.util.List;

import com.example.demo.model.Product;

public interface FavoriteDao {
    boolean exists(Long userId, Long productId);
    void add(Long userId, Long productId);
    void remove(Long userId, Long productId);
    // ⭕ マイページの「お気に入り一覧」表示用：ユーザーがお気に入り登録した商品一覧を取得
    List<Product> findProductsByUserId(Long userId);
}
