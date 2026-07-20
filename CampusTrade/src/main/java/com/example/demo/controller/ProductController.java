package com.example.demo.controller;

import java.io.IOException;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dao.FavoriteDao;
import com.example.demo.dao.MessageDao;
import com.example.demo.dao.OfferDao;
import com.example.demo.dao.ProductDaoImpl;
import com.example.demo.dao.ReportDao;
import com.example.demo.dao.ReviewDao;
import com.example.demo.model.Category;
import com.example.demo.model.Message;
import com.example.demo.model.Offer;
import com.example.demo.model.Product;
import com.example.demo.model.Report;
import com.example.demo.model.Review;
import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
import com.example.demo.service.ProductImageService;
import com.example.demo.service.UserService;

@Controller
public class ProductController {

    private final ProductDaoImpl productDao;
    private final MessageDao messageDao;
    private final FavoriteDao favoriteDao;
    private final UserService userService;
    private final ProductImageService productImageService;
    private final ReviewDao reviewDao;
    private final ReportDao reportDao;
    private final OfferDao offerDao;
    private final NotificationService notificationService;

    public ProductController(ProductDaoImpl productDao, MessageDao messageDao, FavoriteDao favoriteDao,
            UserService userService, ProductImageService productImageService, ReviewDao reviewDao,
            ReportDao reportDao, OfferDao offerDao, NotificationService notificationService) {
        this.productDao = productDao;
        this.messageDao = messageDao;
        this.favoriteDao = favoriteDao;
        this.userService = userService;
        this.productImageService = productImageService;
        this.reviewDao = reviewDao;
        this.reportDao = reportDao;
        this.offerDao = offerDao;
        this.notificationService = notificationService;
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
        // ⭕ 一覧のサムネイル表示用に、それぞれの商品の画像有無をチェックしておく
        products.forEach(p -> p.setHasImage(productImageService.hasImage(p.getId())));

        List<Category> categories = productDao.findAllCategories();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedSort", sort);

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
        product.setHasImage(productImageService.hasImage(id));
        // ⭕ 複数画像対応：実際に保存されている画像URL一覧
        product.setImageUrls(productImageService.getImageUrls(id));

        // ⭕ 検索強化：閲覧数を+1
        productDao.incrementViewCount(id);

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

        // ⭕ 自分側が取引完了の確認済みかどうか（双方確認式のボタン制御用）
        boolean myConfirmed = isSeller ? product.isSellerConfirmed() : (isBuyer && product.isBuyerConfirmed());

        // ⭕ レビュー機能：出品者が受けた評価の平均・件数、取引完了後にレビュー可能かどうか
        Double sellerAvgRating = reviewDao.getAverageRating(product.getSellerId());
        int sellerReviewCount = reviewDao.countByRevieweeId(product.getSellerId());
        List<Review> productReviews = "CLOSED".equals(product.getStatus())
                ? reviewDao.findByRevieweeId(product.getSellerId()) : new ArrayList<>();
        boolean canReview = "CLOSED".equals(product.getStatus()) && isBuyerOrSeller
                && currentUserId != null && !reviewDao.hasReviewed(id, currentUserId);

        // ⭕ 価格交渉：オファー一覧（出品者、またはオファー送信者本人にのみテンプレート側で表示）
        List<Offer> offers = "OPEN".equals(product.getStatus()) ? offerDao.findByProductId(id) : new ArrayList<>();
        boolean canOffer = currentUserId != null && !isSeller && "OPEN".equals(product.getStatus())
                && !offerDao.hasPendingOffer(id, currentUserId);

        model.addAttribute("product", product);
        model.addAttribute("messages", messages);
        model.addAttribute("canViewMessages", canViewMessages);
        model.addAttribute("isPrivateChat", isPrivateChat);
        model.addAttribute("isSeller", isSeller);
        model.addAttribute("isBuyerOrSeller", isBuyerOrSeller);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("isFavorite", isFavorite);
        model.addAttribute("myConfirmed", myConfirmed);
        model.addAttribute("sellerAvgRating", sellerAvgRating);
        model.addAttribute("sellerReviewCount", sellerReviewCount);
        model.addAttribute("productReviews", productReviews);
        model.addAttribute("canReview", canReview);
        model.addAttribute("offers", offers);
        model.addAttribute("canOffer", canOffer);

        return "product/detail";
    }

    // 💡 購入申込（OPEN → LOCKED）
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
                notificationService.notify(product.getSellerId(), "OFFER",
                        currentUser.getNickname() + "さんが「" + product.getProductName() + "」の購入を申し込みました",
                        "/products/" + id);
            }
        }

        return "redirect:/products/" + id;
    }

    // 💡 取引完了の確認（双方が確認したら自動的に CLOSED になる）
    @PostMapping("/products/{id}/confirm-complete")
    public String confirmComplete(@PathVariable("id") Long id, Authentication authentication) {
        Product product = productDao.findById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "指定された商品が見つかりません");
        }

        User currentUser = currentUser(authentication);
        if (currentUser == null || !"LOCKED".equals(product.getStatus())) {
            return "redirect:/products/" + id;
        }

        boolean isSeller = currentUser.getId().equals(product.getSellerId());
        boolean isBuyer = currentUser.getId().equals(product.getBuyerId());
        if (isSeller || isBuyer) {
            productDao.confirmCompletion(id, isSeller);
        }

        return "redirect:/products/" + id;
    }

    // ====================== 5. 取引キャンセル ======================
    // ⭕ LOCKED状態の取引を出品者・購入者どちらからでもキャンセルし、OPENに戻せるようにする
    @PostMapping("/products/{id}/cancel")
    public String cancel(@PathVariable("id") Long id, Authentication authentication) {
        Product product = productDao.findById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "指定された商品が見つかりません");
        }
        User currentUser = currentUser(authentication);
        if (currentUser == null || !"LOCKED".equals(product.getStatus())) {
            return "redirect:/products/" + id;
        }
        boolean isSeller = currentUser.getId().equals(product.getSellerId());
        boolean isBuyer = currentUser.getId().equals(product.getBuyerId());
        if (isSeller || isBuyer) {
            productDao.cancelTransaction(id);
            Long otherPartyId = isSeller ? product.getBuyerId() : product.getSellerId();
            notificationService.notify(otherPartyId, "SYSTEM",
                    "「" + product.getProductName() + "」の取引がキャンセルされ、再び出品中に戻りました",
                    "/products/" + id);
        }
        return "redirect:/products/" + id;
    }

    // ====================== 6. 通報機能 ======================
    @PostMapping("/products/{id}/report")
    public String report(@PathVariable("id") Long id,
            @RequestParam("reason") String reason,
            @RequestParam(name = "detail", required = false) String detail,
            Authentication authentication) {
        Product product = productDao.findById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "指定された商品が見つかりません");
        }
        User currentUser = currentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }
        Report reportEntity = new Report();
        reportEntity.setProductId(id);
        reportEntity.setReporterId(currentUser.getId());
        reportEntity.setReason(reason);
        reportEntity.setDetail(detail);
        reportDao.save(reportEntity);
        return "redirect:/products/" + id + "?reported=1";
    }

    // ====================== 7. 価格交渉（オファー） ======================
    @PostMapping("/products/{id}/offer")
    public String createOffer(@PathVariable("id") Long id,
            @RequestParam("offerPrice") Integer offerPrice,
            Authentication authentication) {
        Product product = productDao.findById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "指定された商品が見つかりません");
        }
        User currentUser = currentUser(authentication);
        if (currentUser == null || currentUser.getId().equals(product.getSellerId())) {
            return "redirect:/products/" + id;
        }
        if (!"OPEN".equals(product.getStatus()) || offerPrice == null || offerPrice < 0) {
            return "redirect:/products/" + id;
        }

        Offer offer = new Offer();
        offer.setProductId(id);
        offer.setSenderId(currentUser.getId());
        offer.setOfferPrice(offerPrice);
        offerDao.save(offer);

        notificationService.notify(product.getSellerId(), "OFFER",
                currentUser.getNickname() + "さんが「" + product.getProductName() + "」に" + offerPrice + "円のオファーを送りました",
                "/products/" + id);

        return "redirect:/products/" + id + "#chat";
    }

    @PostMapping("/products/{id}/offers/{offerId}/accept")
    public String acceptOffer(@PathVariable("id") Long id, @PathVariable("offerId") Long offerId, Authentication authentication) {
        Product product = productDao.findById(id);
        Offer offer = offerDao.findById(offerId);
        User currentUser = currentUser(authentication);
        if (product == null || offer == null || currentUser == null
                || !currentUser.getId().equals(product.getSellerId())
                || !"PENDING".equals(offer.getStatus())
                || !"OPEN".equals(product.getStatus())) {
            return "redirect:/products/" + id;
        }

        productDao.lockWithOfferPrice(id, offer.getSenderId(), offer.getOfferPrice());
        offerDao.updateStatus(offerId, "ACCEPTED");
        offerDao.rejectOtherPendingOffers(id, offerId);

        notificationService.notify(offer.getSenderId(), "OFFER",
                "「" + product.getProductName() + "」へのオファー（" + offer.getOfferPrice() + "円）が承諾されました",
                "/products/" + id);

        return "redirect:/products/" + id;
    }

    @PostMapping("/products/{id}/offers/{offerId}/reject")
    public String rejectOffer(@PathVariable("id") Long id, @PathVariable("offerId") Long offerId, Authentication authentication) {
        Product product = productDao.findById(id);
        Offer offer = offerDao.findById(offerId);
        User currentUser = currentUser(authentication);
        if (product == null || offer == null || currentUser == null
                || !currentUser.getId().equals(product.getSellerId())
                || !"PENDING".equals(offer.getStatus())) {
            return "redirect:/products/" + id;
        }
        offerDao.updateStatus(offerId, "REJECTED");
        notificationService.notify(offer.getSenderId(), "OFFER",
                "「" + product.getProductName() + "」へのオファー（" + offer.getOfferPrice() + "円）は見送られました",
                "/products/" + id);
        return "redirect:/products/" + id;
    }

    // ====================== 3. レビュー機能 ======================
    @PostMapping("/products/{id}/review")
    public String submitReview(@PathVariable("id") Long id,
            @RequestParam("rating") Integer rating,
            @RequestParam(name = "comment", required = false) String comment,
            Authentication authentication) {
        Product product = productDao.findById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "指定された商品が見つかりません");
        }
        User currentUser = currentUser(authentication);
        if (currentUser == null || !"CLOSED".equals(product.getStatus())) {
            return "redirect:/products/" + id;
        }
        boolean isSeller = currentUser.getId().equals(product.getSellerId());
        boolean isBuyer = currentUser.getId().equals(product.getBuyerId());
        if (!isSeller && !isBuyer) {
            return "redirect:/products/" + id;
        }
        if (reviewDao.hasReviewed(id, currentUser.getId())) {
            return "redirect:/products/" + id;
        }
        if (rating == null || rating < 1 || rating > 5) {
            return "redirect:/products/" + id;
        }

        Long revieweeId = isSeller ? product.getBuyerId() : product.getSellerId();
        Review review = new Review();
        review.setProductId(id);
        review.setReviewerId(currentUser.getId());
        review.setRevieweeId(revieweeId);
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : null);
        reviewDao.save(review);

        notificationService.notify(revieweeId, "SYSTEM",
                currentUser.getNickname() + "さんから「" + product.getProductName() + "」の取引レビューが届きました",
                "/products/" + id);

        return "redirect:/products/" + id;
    }

    // ⭕ お気に入りの追加・解除
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
            @RequestParam(name = "images", required = false) List<MultipartFile> images,
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

        // ⭕ 複数画像が指定されていれば、商品IDの連番ファイル名で保存する（最大5枚）
        try {
            productImageService.saveImages(newId, images);
        } catch (IOException e) {
            // 画像保存に失敗しても出品自体は成立させる（画像なし扱い）
        }

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
        product.setHasImage(productImageService.hasImage(id));
        product.setImageUrls(productImageService.getImageUrls(id));

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
            @RequestParam(name = "images", required = false) List<MultipartFile> images,
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
            existing.setHasImage(productImageService.hasImage(id));
            existing.setImageUrls(productImageService.getImageUrls(id));
            return reShowForm(model, existing, productName, description, price, categoryId, validationError, true);
        }

        // ⭕ 9. 値下げ通知：更新前の価格より安くなった場合、お気に入り登録している全ユーザーに通知する
        Integer oldPrice = existing.getPrice();

        Product product = new Product();
        product.setId(id);
        product.setProductName(productName.trim());
        product.setDescription(description.trim());
        product.setPrice(price);
        product.setCategoryId(categoryId);
        product.setSellerId(currentUser.getId());

        productDao.update(product);

        if (oldPrice != null && price < oldPrice) {
            List<Long> favoriterIds = favoriteDao.findUserIdsByProductId(id);
            for (Long userId : favoriterIds) {
                notificationService.notify(userId, "PRICE_DROP",
                        "お気に入りの「" + productName.trim() + "」が" + oldPrice + "円→" + price + "円に値下げされました",
                        "/products/" + id);
            }
        }

        // ⭕ 新しい画像が選択されていれば上書き保存（未選択なら既存の画像をそのまま維持）
        try {
            productImageService.saveImages(id, images);
        } catch (IOException e) {
            // 画像保存に失敗しても更新自体は成立させる
        }

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
        productImageService.deleteImage(id);
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
        // ⭕ レビュー機能：自分が受け取った評価一覧・平均点
        model.addAttribute("myReviews", reviewDao.findByRevieweeId(currentUser.getId()));
        model.addAttribute("myAvgRating", reviewDao.getAverageRating(currentUser.getId()));

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
