package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dao.ProductDaoImpl;
import com.example.demo.model.Category;
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
            @RequestParam(name = "category", required = false) String tagCategory, // 人気タグ用（大・中・小分類のいずれかに一致）
            @RequestParam(name = "categoryId", required = false) Integer categoryId, // 3連選択ボックス用
            @RequestParam(name = "price", required = false) String price,
            @RequestParam(name = "delivery", required = false) String delivery,
            @RequestParam(name = "sort", required = false) String sort,
            Model model) {

        // 条件付きでDBから検索・抽出
        List<Product> products = productDao.findByConditions(keyword, tagCategory, categoryId, price, delivery, sort);

        // 3連選択ボックス（大・中・小分類）をJS側で組み立てるためのカテゴリ一覧
        List<Category> categories = productDao.findAllCategories();

        // 画面のオブジェクトに詰めて返却
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword); // 検索欄の入力値を検索後も保持するため
        model.addAttribute("selectedCategoryId", categoryId); // 選択ボックスの選択状態を復元するため

        return "product/list"; 
    }
}