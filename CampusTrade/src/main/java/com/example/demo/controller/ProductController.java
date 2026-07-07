package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductController {

    // ホーム画面（商品一覧画面：templates/product/list.html を表示）
    @GetMapping({"/", "/home"})
    public String home() {
        return "product/list"; 
    }
}