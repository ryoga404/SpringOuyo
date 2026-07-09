package com.example.demo.dao;

import java.util.List;

import com.example.demo.model.Category;
import com.example.demo.model.Product;

public interface ProductDao {
    List<Product> findAll();

    List<Product> findByConditions(String keyword, String tagCategory, Integer categoryId, String price, String delivery, String sort);

    // 3連選択ボックス（大・中・小分類）の元データ取得用
    List<Category> findAllCategories();

    // 商品詳細画面用：1件取得
    Product findById(Long id);

    // 取引ステータスを更新（LOCKEDにする際は買い手のIDを渡す。確認フラグはリセットされる）
    void updateStatus(Long id, String status, Long buyerId);

    // ⭕ 取引完了の双方確認：自分側の確認フラグを立て、両方揃ったら status を CLOSED にする
    void confirmCompletion(Long id, boolean isSeller);

    // ⭕ 商品出品：新規登録し、生成されたIDを返す
    Long save(Product product);

    // ⭕ 商品編集：自分の出品情報を更新
    void update(Product product);

    // ⭕ 商品削除
    void delete(Long id);

    // ⭕ マイページ：自分が出品した商品一覧（出品履歴）
    List<Product> findBySellerId(Long sellerId);

    // ⭕ マイページ：購入が完了した商品一覧（購入履歴）
    List<Product> findPurchaseHistory(Long buyerId);

    // ⭕ マイページ：現在交渉中（LOCKED）の商品一覧（自分が出品者 or 購入者）
    List<Product> findNegotiations(Long userId);

    // ⭕ 管理者機能：ステータスを問わず全件取得
    List<Product> findAllForAdmin();

    // ⭕ 管理者機能：規約違反商品を「禁止」ステータスにする
    void banProduct(Long id);
}
