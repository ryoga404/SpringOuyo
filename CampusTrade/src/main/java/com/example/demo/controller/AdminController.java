package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dao.OfferDao;
import com.example.demo.dao.ProductDao;
import com.example.demo.dao.ReportDao;
import com.example.demo.model.Product;
import com.example.demo.model.Report;

// 💡 管理者機能：商品一覧（状態ごと）・不適切な出品の非表示化（「禁止」ステータスへの変更）
//    ⭕ 10. あわせて統計情報（ステータス別件数・保留中の通報/オファー件数）と通報対応画面を追加
//    アクセス制御は SecurityConfig 側で /admin/** を ROLE_ADMIN 限定にしている
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductDao productDao;
    private final ReportDao reportDao;
    private final OfferDao offerDao;

    public AdminController(ProductDao productDao, ReportDao reportDao, OfferDao offerDao) {
        this.productDao = productDao;
        this.reportDao = reportDao;
        this.offerDao = offerDao;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Product> allProducts = productDao.findAllForAdmin();
        model.addAttribute("products", allProducts);

        // ⭕ 10. 統計情報
        Map<String, Integer> statusCounts = productDao.countByStatus();
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("totalProducts", allProducts.size());
        model.addAttribute("pendingReportCount", reportDao.countPending());
        model.addAttribute("pendingOfferCount", offerDao.countPending());

        return "admin/dashboard";
    }

    // ⭕ 利用規約違反（学内禁止物など）の出品を非表示にし「禁止」ステータスへ変更
    @PostMapping("/products/{id}/ban")
    public String ban(@PathVariable Long id) {
        productDao.banProduct(id);
        return "redirect:/admin/dashboard";
    }

    // ====================== 6. 通報対応 ======================

    @GetMapping("/reports")
    public String reports(Model model) {
        List<Report> reports = reportDao.findAll();
        model.addAttribute("reports", reports);
        model.addAttribute("pendingReportCount", reportDao.countPending());
        return "admin/reports";
    }

    @PostMapping("/reports/{id}/resolve")
    public String resolveReport(@PathVariable Long id, @RequestParam("status") String status) {
        // status には "REVIEWED"（対応済み）または "DISMISSED"（却下）を想定
        reportDao.updateStatus(id, status);
        return "redirect:/admin/reports";
    }
}
