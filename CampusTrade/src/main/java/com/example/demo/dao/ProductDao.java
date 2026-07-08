package com.example.demo.dao;

import java.util.List;

import com.example.demo.model.Product;

public interface ProductDao {
    List<Product> findAll();
 // ProductDao.java 内に追記
    List<Product> findByConditions(String keyword, String mainCategory, String price, String delivery, String sort);
}