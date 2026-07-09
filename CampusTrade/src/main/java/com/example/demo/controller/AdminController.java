package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.dao.ProductDao;
import com.example.demo.model.Product;

// 💡 管理者機能：商品一覧（状態ごと）・不適切な出品の非表示化（「禁止」ステータスへの変更）
//    アクセス制御は SecurityConfig 側で /admin/** を ROLE_ADMIN 限定にしている
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductDao productDao;

    public AdminController(ProductDao productDao) {
        this.productDao = productDao;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Product> allProducts = productDao.findAllForAdmin();
        model.addAttribute("products", allProducts);
        return "admin/dashboard";
    }

    // ⭕ 利用規約違反（学内禁止物など）の出品を非表示にし「禁止」ステータスへ変更
    @PostMapping("/products/{id}/ban")
    public String ban(@PathVariable Long id) {
        productDao.banProduct(id);
        return "redirect:/admin/dashboard";
    }
}
