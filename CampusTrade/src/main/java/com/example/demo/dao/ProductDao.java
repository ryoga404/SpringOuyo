package com.example.demo.dao;

import java.util.List;

import com.example.demo.model.Category;
import com.example.demo.model.Product;

public interface ProductDao {
    List<Product> findAll();
 // ProductDao.java 内に追記
    List<Product> findByConditions(String keyword, String tagCategory, Integer categoryId, String price, String delivery, String sort);

    // 3連選択ボックス（大・中・小分類）の元データ取得用
    List<Category> findAllCategories();
}