package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.dao.NotificationDao;
import com.example.demo.model.Notification;

@Service
public class NotificationService {

    private final NotificationDao notificationDao;

    public NotificationService(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    // ⭕ 通知を1件作成する共通ヘルパー
    public void notify(Long userId, String type, String content, String link) {
        if (userId == null) return;
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setContent(content);
        n.setLink(link);
        notificationDao.save(n);
    }
}
