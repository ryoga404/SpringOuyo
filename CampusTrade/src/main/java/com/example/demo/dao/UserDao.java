package com.example.demo.dao;

import java.util.Optional;

import com.example.demo.model.User;

public interface UserDao {
    Optional<User> findByEmail(String email);
    // ⭕ ユーザーを新規登録するメソッドを追加
    void save(User user);
}