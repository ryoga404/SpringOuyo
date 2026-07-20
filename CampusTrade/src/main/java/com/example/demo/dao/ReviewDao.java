package com.example.demo.dao;

import java.util.List;

import com.example.demo.model.Review;

public interface ReviewDao {
    // ⭕ 取引完了後のレビュー投稿
    void save(Review review);

    // ⭕ 指定ユーザーが指定商品について既にレビュー済みか
    boolean hasReviewed(Long productId, Long reviewerId);

    // ⭕ あるユーザーが受け取ったレビュー一覧（プロフィール・マイページ表示用）
    List<Review> findByRevieweeId(Long revieweeId);

    // ⭕ あるユーザーの平均評価（件数0件ならnull）
    Double getAverageRating(Long revieweeId);

    // ⭕ あるユーザーが受け取ったレビュー件数
    int countByRevieweeId(Long revieweeId);
}
