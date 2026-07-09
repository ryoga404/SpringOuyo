package com.example.demo.dao;

import java.util.List;

import com.example.demo.model.Message;

public interface MessageDao {
    // 指定した商品に紐づくメッセージを全件取得（作成日時の昇順）
    List<Message> findByProductId(Long productId);

    // メッセージを1件保存
    void save(Message message);

    // ⭕ 指定ユーザーが受信者であるメッセージを既読にする（商品詳細画面を開いたとき）
    void markAsRead(Long productId, Long receiverId);
}
