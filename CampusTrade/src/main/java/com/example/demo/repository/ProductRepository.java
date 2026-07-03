package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // 💡 エラー原因1の解消：カテゴリでの絞り込み検索
    List<Product> findByCategory(String category);
    
    // 💡 エラー原因2の解消：商品名または商品説明にキーワードが含まれるものを検索
    List<Product> findByProductNameContainingOrDescriptionContaining(String nameKeyword, String descKeyword);
}