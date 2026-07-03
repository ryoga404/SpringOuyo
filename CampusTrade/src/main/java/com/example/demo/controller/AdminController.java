package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.repository.ProductRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductRepository productRepository;

    // 管理者による不適切商品の非表示（禁止化）
    @PostMapping("/products/{id}/ban")
    public String banProduct(@PathVariable Long id) {
        productRepository.findById(id).ifPresent(product -> {
            product.setStatus("BANNED");
            product.setProductName("[利用規約違反により禁止] " + product.getProductName());
            productRepository.save(product);
        });
        return "redirect:/products";
    }
}