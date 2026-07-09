package com.example.demo.dao;

import java.util.Optional;

import com.example.demo.model.User;

public interface UserDao {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    // ⭕ ユーザーを新規登録するメソッド
    void save(User user);
    // ⭕ ニックネームのみ更新するメソッド（プロフィール編集用）
    void updateNickname(Long userId, String nickname);
}
