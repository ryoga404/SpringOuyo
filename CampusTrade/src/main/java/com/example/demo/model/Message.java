package com.example.demo.model;

import java.time.LocalDateTime;

// 💡 実際のDB構造(messagesテーブル)に合わせて parent_id は廃止。
//    代わりに receiver_id / is_read を持つフラットなメッセージとして扱う。
//    receiverId が null の間は「商品ページ上での公開Q&A」、
//    買い手確定後は出品者・購入者間の「非公開メッセージ」として receiverId を設定する。
public class Message {
    private Long id;
    private Long productId;
    private Long senderId;
    private Long receiverId; // null = 公開Q&A（全員に表示）, 値あり = 非公開メッセージ
    private String content;
    private LocalDateTime createdAt;
    private boolean isRead;

    private User sender; // 送信者情報（結合して表示用に保持）

    public Message() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { this.isRead = read; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
}
