package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.dao.ProductDao;
import com.example.demo.model.Product;

@Controller
public class ProductController {

    private final ProductDao productDao;

    // 💡 コンストラクタ注入でProductDaoを利用可能にする
    public ProductController(ProductDao productDao) {
        this.productDao = productDao;
    }

    // HTML側のリンク起点に合わせ、「/products」でも一覧を開けるようにマッピングを追加
    @GetMapping({"/", "/home", "/products"})
    public String home(Model model) {
        // DBから商品一覧を取得
        List<Product> products = productDao.findAll();
        
        // Thymeleafの th:each="product : ${products}" にデータを渡す
        model.addAttribute("products", products);
        
        return "product/list"; 
    }
}