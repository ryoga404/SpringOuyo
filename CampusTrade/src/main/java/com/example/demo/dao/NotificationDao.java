package com.example.demo.dao;

import java.util.List;

import com.example.demo.model.Notification;

public interface NotificationDao {
    void save(Notification notification);

    // ⭕ 新着順で最大件数を取得（ヘッダーのお知らせ一覧表示用）
    List<Notification> findByUserId(Long userId, int limit);

    int countUnread(Long userId);

    void markAsRead(Long id, Long userId);

    void markAllAsRead(Long userId);
}
