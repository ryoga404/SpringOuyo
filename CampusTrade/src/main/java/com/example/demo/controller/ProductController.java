package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dao.ProductDaoImpl;
import com.example.demo.model.Product;

@Controller
public class ProductController {

    private final ProductDaoImpl productDao;

    public ProductController(ProductDaoImpl productDao) {
        this.productDao = productDao;
    }

    // HTMLの検索用パラメータをすべて受け取れるように拡張
    @GetMapping({"/", "/home", "/products"})
    public String home(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "mainCategory", required = false) String mainCategory,
            @RequestParam(name = "category", required = false) String tagCategory, // 人気タグ用
            @RequestParam(name = "price", required = false) String price,
            @RequestParam(name = "delivery", required = false) String delivery,
            @RequestParam(name = "sort", required = false) String sort,
            Model model) {

        // 下部の「人気カテゴリ」リンク（?category=教科書）が踏まれた場合、大分類として扱う
        if (mainCategory == null || mainCategory.isEmpty()) {
            if (tagCategory != null && !tagCategory.isEmpty()) {
                mainCategory = tagCategory;
            }
        }

        // 条件付きでDBから検索・抽出
        List<Product> products = productDao.findByConditions(keyword, mainCategory, price, delivery, sort);
        
        // 画面のオブジェクトに詰めて返却
        model.addAttribute("products", products);
        
        return "product/list"; 
    }
}