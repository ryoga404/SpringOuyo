package com.example.demo.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // 商品一覧（検索＆カテゴリ絞り込み対応）
 // 商品一覧（検索＆カテゴリ絞り込み対応）
    @GetMapping
    public String listProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            Model model) {
        
        List<Product> products;
        if (category != null && !category.isEmpty()) {
            products = productRepository.findByCategory(category);
        } else if (keyword != null && !keyword.isEmpty()) {
            products = productRepository.findByProductNameContainingOrDescriptionContaining(keyword, keyword);
        } else {
            products = productRepository.findAll();
        }
        
        // 💡 修正ポイント：取得した商品を確実にモデルに登録する
        model.addAttribute("products", products);
        
        return "product/list";
    }

    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "product/form";
    }

    @PostMapping("/save")
    public String saveProduct(HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        String productName = request.getParameter("productName");
        String category = request.getParameter("category");
        String priceStr = request.getParameter("price");
        String description = request.getParameter("description");

        int price = 0;
        try {
            price = Integer.parseInt(priceStr);
            if (price > 9999999 || price < 0) {
                return "redirect:/products/new?error=invalid_price";
            }
        } catch (NumberFormatException e) {
            return "redirect:/products/new?error=invalid_price";
        }

        User seller = userRepository.findByUserId(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません"));
                
        Product product = new Product();
        product.setProductName(productName);
        product.setCategory(category);
        product.setPrice(price);
        product.setDescription(description);
        product.setSeller(seller);
        product.setStatus("OPEN");
        
        productRepository.save(product);
        return "redirect:/products";
    }

    // 商品詳細
    @GetMapping("/{id}")
    public String productDetail(@PathVariable("id") Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("無効な商品ID:" + id));
        
        boolean isFavorite = false;
        if (userDetails != null) {
            User user = userRepository.findByUserId(userDetails.getUsername()).orElse(null);
            if (user != null && user.getFavoriteProducts() != null) {
                isFavorite = user.getFavoriteProducts().contains(product);
            }
        }

        model.addAttribute("product", product);
        model.addAttribute("isFavorite", isFavorite);
        return "product/detail";
    }

    // 取引ステータス変更
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("newStatus") String newStatus) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("無効な商品ID:" + id));
        product.setStatus(newStatus);
        productRepository.save(product);
        return "redirect:/products/" + id;
    }

    // お気に入り登録・解除
    @PostMapping("/{id}/favorite")
    public String toggleFavorite(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("無効な商品ID:" + id));
        User user = userRepository.findByUserId(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません"));

        if (user.getFavoriteProducts().contains(product)) {
            user.getFavoriteProducts().remove(product);
        } else {
            user.getFavoriteProducts().add(product);
        }
        userRepository.save(user);
        return "redirect:/products/" + id;
    }

    // マイページ
    @GetMapping("/mypage")
    public String mypage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUserId(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません"));
        
        List<Product> allProducts = productRepository.findAll();
        List<Product> myNegotiations = allProducts.stream()
                .filter(p -> "LOCKED".equals(p.getStatus()) && p.getMessages().stream().anyMatch(m -> m.getSender().getId().equals(user.getId())))
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("myProducts", allProducts.stream().filter(p -> p.getSeller().getId().equals(user.getId())).toList());
        model.addAttribute("myNegotiations", myNegotiations);
        return "product/mypage";
    }

    // 💡 変更：ニックネーム変更画面の表示
    @GetMapping("/nickname")
    public String nicknameForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUserId(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません"));
        model.addAttribute("user", user);
        return "product/nickname";
    }

    // 💡 変更：ニックネーム変更処理の実行
    @PostMapping("/nickname/update")
    public String updateNickname(@RequestParam("nickname") String nickname, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUserId(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません"));
        
        user.setNickname(nickname);
        userRepository.save(user);
        
        return "redirect:/products/mypage"; // 変更後はマイページへ戻す
    }
}