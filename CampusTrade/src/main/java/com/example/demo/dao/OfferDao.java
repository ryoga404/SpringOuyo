package com.example.demo.dao;

import java.util.List;

import com.example.demo.model.Offer;

public interface OfferDao {
    void save(Offer offer);

    // ⭕ 商品詳細ページに表示するオファー一覧（新しい順）
    List<Offer> findByProductId(Long productId);

    Offer findById(Long id);

    // ⭕ オファーを承諾/却下
    void updateStatus(Long id, String status);

    // ⭕ ある商品に既に保留中のオファーがあるかどうか（同時に複数オファーを避ける用）
    boolean hasPendingOffer(Long productId, Long senderId);

    // ⭕ 承諾された1件以外の保留中オファーをまとめて却下（購入が決まったら他のオファーは自動的にクローズ）
    void rejectOtherPendingOffers(Long productId, Long acceptedOfferId);

    // ⭕ 管理者ダッシュボード用：保留中オファー件数
    int countPending();
}
