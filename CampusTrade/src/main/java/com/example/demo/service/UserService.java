package com.example.demo.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dao.UserDao;
import com.example.demo.model.User;

@Service
public class UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> getUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public Optional<User> getUserById(Long id) {
        return userDao.findById(id);
    }

    // ⭕ パスワードをハッシュ化してデータベースに保存する
    public void registerUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userDao.save(user);
    }

    // ⭕ ニックネームの変更（プロフィール編集機能）
    public void updateNickname(Long userId, String nickname) {
        userDao.updateNickname(userId, nickname);
    }

    // ⭕ 11. 退会機能：ソフトデリート（deleted_atを設定するだけで、出品履歴・メッセージは残す）
    public void deleteAccount(Long userId) {
        userDao.softDelete(userId);
    }
}
