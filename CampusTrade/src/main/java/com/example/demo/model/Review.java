package com.example.demo.model;

import java.time.LocalDateTime;

// ⭕ 取引完了(CLOSED)後に、出品者・購入者が互いを評価するためのレビュー
public class Review {
    private Long id;
    private Long productId;
    private Long reviewerId;   // 評価した人
    private Long revieweeId;   // 評価された人
    private int rating;        // 1〜5
    private String comment;
    private LocalDateTime createdAt;

    private User reviewer; // 表示用に結合して保持

    public Review() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }

    public Long getRevieweeId() { return revieweeId; }
    public void setRevieweeId(Long revieweeId) { this.revieweeId = revieweeId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getReviewer() { return reviewer; }
    public void setReviewer(User reviewer) { this.reviewer = reviewer; }
}
