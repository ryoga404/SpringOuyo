package com.example.demo.dao;

import java.util.List;

import com.example.demo.model.Product;

public interface ProductDao {
    List<Product> findAll();
}