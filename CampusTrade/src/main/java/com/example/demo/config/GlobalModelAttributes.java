package com.example.demo.config;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

// ⭕ ナビゲーションバーなどで、メールアドレスではなく「ニックネーム」を表示するため、
//    ログイン中のユーザー情報を全画面共通で Model に注入する
@ControllerAdvice
public class GlobalModelAttributes {

    private final UserService userService;

    public GlobalModelAttributes(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("loginUser")
    public User loginUser(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userService.getUserByEmail(authentication.getName()).orElse(null);
    }
}
