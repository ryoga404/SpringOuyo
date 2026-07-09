package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dao.FavoriteDao;
import com.example.demo.dao.MessageDao;
import com.example.demo.dao.ProductDaoImpl;
import com.example.demo.model.Category;
import com.example.demo.model.Message;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.service.UserService;

@Controller
public class ProductController {

    private final ProductDaoImpl productDao;
    private final MessageDao messageDao;
    private final FavoriteDao favoriteDao;
    private final UserService userService;

    public ProductController(ProductDaoImpl productDao, MessageDao messageDao, FavoriteDao favoriteDao, UserService userService) {
        this.productDao = productDao;
        this.messageDao = messageDao;
        this.favoriteDao = favoriteDao;
        this.userService = userService;
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null) return null;
        return userService.getUserByEmail(authentication.getName()).orElse(null);
    }

    // HTMLの検索用パラメータをすべて受け取れるように拡張
    @GetMapping({"/", "/home", "/products"})
    public String home(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "category", required = false) String tagCategory,
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "price", required = false) String price,
            @RequestParam(name = "delivery", required = false) String delivery,
            @RequestParam(name = "sort", required = false) String sort,
            Model model) {

        List<Product> products = productDao.findByConditions(keyword, tagCategory, categoryId, price, delivery, sort);
        List<Category> categories = productDao.findAllCategories();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);

        return "product/list";
    }

    // 💡 商品詳細画面
    @GetMapping("/products/{id}")
    public String detail(@PathVariable("id") Long id, Model model, Authentication authentication) {
        Product product = productDao.findById(id);
        if (product == null) {
            // ⭕ 存在しない商品IDが指定された場合は404エラーページを表示する
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "指定された商品が見つかりません");
        }

        User currentUser = currentUser(authentication);
        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        boolean isSeller = currentUserId != null && currentUserId.equals(product.getSellerId());
        boolean isBuyer = currentUserId != null && currentUserId.equals(product.getBuyerId());
        boolean isBuyerOrSeller = isSeller || isBuyer;

        // 買い手が確定した（LOCKED / CLOSED になった）後は、取引相手以外にはメッセージを非公開にする
        boolean isPrivateChat = product.getBuyerId() != null;
        boolean canViewMessages = !isPrivateChat || isBuyerOrSeller;

        List<Message> messages = canViewMessages ? messageDao.findByProductId(id) : new ArrayList<>();

        // 自分宛の未読メッセージがあれば既読にする
        if (currentUserId != null) {
            messageDao.markAsRead(id, currentUserId);
        }

        boolean isFavorite = currentUserId != null && favoriteDao.exists(currentUserId, id);

        model.addAttribute("product", product);
        model.addAttribute("messages", messages);
        model.addAttribute("canViewMessages", canViewMessages);
        model.addAttribute("isPrivateChat", isPrivateChat);
        model.addAttribute("isSeller", isSeller);
        model.addAttribute("isBuyerOrSeller", isBuyerOrSeller);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("isFavorite", isFavorite);

        return "product/detail";
    }

    // 💡 取引ステータス更新（購入申込 → LOCKED、取引完了 → CLOSED）
    @PostMapping("/products/{id}/status")
    public String updateStatus(@PathVariable("id") Long id, @RequestParam("newStatus") String newStatus, Authentication authentication) {
        Product product = productDao.findById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "指定された商品が見つかりません");
        }

        User currentUser = currentUser(authentication);
        if (currentUser == null) {
            return "redirect:/products/" + id;
        }

        if ("LOCKED".equals(newStatus)) {
            boolean isSeller = currentUser.getId().equals(product.getSellerId());
            if ("OPEN".equals(product.getStatus()) && !isSeller) {
                productDao.updateStatus(id, "LOCKED", currentUser.getId());
            }
        } else if ("CLOSED".equals(newStatus)) {
            boolean isBuyerOrSeller = currentUser.getId().equals(product.getSellerId())
                    || currentUser.getId().equals(product.getBuyerId());
            if ("LOCKED".equals(product.getStatus()) && isBuyerOrSeller) {
                productDao.updateStatus(id, "CLOSED", product.getBuyerId());
            }
        }

        return "redirect:/products/" + id;
    }

    // 💡 お気に入りの追加・解除
    @PostMapping("/products/{id}/favorite")
    public String toggleFavorite(@PathVariable("id") Long id, Authentication authentication) {
        User currentUser = currentUser(authentication);
        if (currentUser != null) {
            if (favoriteDao.exists(currentUser.getId(), id)) {
                favoriteDao.remove(currentUser.getId(), id);
            } else {
                favoriteDao.add(currentUser.getId(), id);
            }
        }
        return "redirect:/products/" + id;
    }

    // ====================== 商品出品 ======================

    @GetMapping("/products/new")
    public String newForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productDao.findAllCategories());
        model.addAttribute("isEdit", false);
        return "product/form";
    }

    @PostMapping("/products/save")
    public String save(
            @RequestParam("productName") String productName,
            @RequestParam("description") String description,
            @RequestParam("price") Integer price,
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            Authentication authentication,
            Model model) {

        User currentUser = currentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        String validationError = validate(productName, description, price, categoryId);
        if (validationError != null) {
            return reShowForm(model, null, productName, description, price, categoryId, validationError, false);
        }

        Product product = new Product();
        product.setProductName(productName.trim());
        product.setDescription(description.trim());
        product.setPrice(price);
        product.setCategoryId(categoryId);
        product.setSellerId(currentUser.getId());

        Long newId = productDao.save(product);
        return "redirect:/products/" + newId;
    }

    // ====================== 商品編集 ======================

    @GetMapping("/products/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Authentication authentication, Model model) {
        Product product = productDao.findById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "指定された商品が見つかりません");
        }

        User currentUser = currentUser(authentication);
        if (currentUser == null || !currentUser.getId().equals(product.getSellerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "この商品を編集する権限がありません");
        }
        if (!"OPEN".equals(product.getStatus())) {
            // 交渉開始後の商品は編集不可
            return "redirect:/products/" + id;
        }

        model.addAttribute("product", product);
        model.addAttribute("categories", productDao.findAllCategories());
        model.addAttribute("isEdit", true);
        return "product/form";
    }

    @PostMapping("/products/{id}/edit")
    public String edit(
            @PathVariable("id") Long id,
            @RequestParam("productName") String productName,
            @RequestParam("description") String description,
            @RequestParam("price") Integer price,
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            Authentication authentication,
            Model model) {

        Product existing = productDao.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "指定された商品が見つかりません");
        }

        User currentUser = currentUser(authentication);
        if (currentUser == null || !currentUser.getId().equals(existing.getSellerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "この商品を編集する権限がありません");
        }
        if (!"OPEN".equals(existing.getStatus())) {
            return "redirect:/products/" + id;
        }

        String validationError = validate(productName, description, price, categoryId);
        if (validationError != null) {
            existing.setProductName(productName);
            existing.setDescription(description);
            existing.setPrice(price);
            existing.setCategoryId(categoryId);
            return reShowForm(model, existing, productName, description, price, categoryId, validationError, true);
        }

        Product product = new Product();
        product.setId(id);
        product.setProductName(productName.trim());
        product.setDescription(description.trim());
        product.setPrice(price);
        product.setCategoryId(categoryId);
        product.setSellerId(currentUser.getId());

        productDao.update(product);
        return "redirect:/products/" + id;
    }

    // ====================== 商品削除 ======================

    @PostMapping("/products/{id}/delete")
    public String delete(@PathVariable("id") Long id, Authentication authentication) {
        Product product = productDao.findById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "指定された商品が見つかりません");
        }

        User currentUser = currentUser(authentication);
        if (currentUser == null || !currentUser.getId().equals(product.getSellerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "この商品を削除する権限がありません");
        }
        if (!"OPEN".equals(product.getStatus())) {
            // 交渉・取引中の商品は削除不可
            return "redirect:/products/" + id;
        }

        productDao.delete(id);
        return "redirect:/products/mypage";
    }

    // ====================== マイページ ======================

    @GetMapping("/products/mypage")
    public String mypage(Authentication authentication, Model model) {
        User currentUser = currentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("myProducts", productDao.findBySellerId(currentUser.getId()));
        model.addAttribute("myPurchases", productDao.findPurchaseHistory(currentUser.getId()));
        model.addAttribute("myNegotiations", productDao.findNegotiations(currentUser.getId()));
        model.addAttribute("myFavorites", favoriteDao.findProductsByUserId(currentUser.getId()));

        return "product/mypage";
    }

    // ====================== ニックネーム変更 ======================

    @GetMapping("/products/nickname")
    public String nicknameForm(Authentication authentication, Model model) {
        User currentUser = currentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentUser", currentUser);
        return "product/nickname";
    }

    @PostMapping("/products/nickname/update")
    public String updateNickname(@RequestParam("nickname") String nickname, Authentication authentication) {
        User currentUser = currentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (nickname != null && !nickname.trim().isEmpty()) {
            userService.updateNickname(currentUser.getId(), nickname.trim());
        }
        return "redirect:/products/mypage";
    }

    // ====================== 入力バリデーション ======================

    private String validate(String productName, String description, Integer price, Integer categoryId) {
        if (productName == null || productName.trim().isEmpty()) {
            return "商品名を入力してください。";
        }
        if (description == null || description.trim().isEmpty()) {
            return "商品説明を入力してください。";
        }
        if (categoryId == null) {
            return "カテゴリを選択してください。";
        }
        if (price == null || price < 0) {
            return "価格には0以上の数値を入力してください。";
        }
        if (price > 9999999) {
            return "価格は9,999,999円以下で入力してください。";
        }
        return null;
    }

    private String reShowForm(Model model, Product existing, String productName, String description,
            Integer price, Integer categoryId, String errorMessage, boolean isEdit) {
        Product product = existing != null ? existing : new Product();
        product.setProductName(productName);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategoryId(categoryId);

        model.addAttribute("product", product);
        model.addAttribute("categories", productDao.findAllCategories());
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("errorMessage", errorMessage);
        return "product/form";
    }
}
