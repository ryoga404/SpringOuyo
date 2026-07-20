package com.example.demo.model;

import java.time.LocalDateTime;

// ⭕ 購入希望者が価格交渉（値下げオファー）を出品者に送るための情報。
//    ACCEPTED になった時点で商品を LOCKED にし、buyerId をオファー送信者にする。
public class Offer {
    private Long id;
    private Long productId;
    private Long senderId; // オファーを出した人（購入希望者）
    private int offerPrice;
    private String status; // PENDING / ACCEPTED / REJECTED
    private LocalDateTime createdAt;

    // 表示用
    private String senderNickname;

    public Offer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public int getOfferPrice() { return offerPrice; }
    public void setOfferPrice(int offerPrice) { this.offerPrice = offerPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getSenderNickname() { return senderNickname; }
    public void setSenderNickname(String senderNickname) { this.senderNickname = senderNickname; }
}
