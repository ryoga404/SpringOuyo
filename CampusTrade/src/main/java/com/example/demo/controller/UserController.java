package com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ログイン画面を表示
    @GetMapping("/login")
    public String login() {
        return "login"; 
    }

    // ユーザー登録画面を表示
    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    // ユーザー登録の実行処理
    @PostMapping("/register")
    public String registerUser(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            Model model) {

        try {
            // 新しいユーザーオブジェクトを作成
            User user = new User();
            user.setEmail(email);
            user.setPassword(password); // 生パスワード（Service側で暗号化されます）
            user.setNickname(username); // 登録画面の「ユーザー名」を初期ニックネームとして保存
            user.setRole("USER");       // 一般ユーザー権限を付与

            // ⭕ サービス層の登録処理を呼び出す
            userService.registerUser(user);

            // 登録成功後はログイン画面へ
            return "redirect:/login";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "登録に失敗しました。入力内容を確認するか、別のメールアドレスをお試しください。");
            return "register";
        }
    }

    // ====================== 11. 退会機能 ======================

    // ⭕ 退会確認画面
    @GetMapping("/account/delete")
    public String deleteConfirm(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        User currentUser = userService.getUserByEmail(authentication.getName()).orElse(null);
        model.addAttribute("currentUser", currentUser);
        return "account/delete";
    }

    // ⭕ 退会の実行：ソフトデリートしてログアウトさせる
    @PostMapping("/account/delete")
    public String deleteAccount(Authentication authentication, HttpServletRequest request) {
        if (authentication == null) {
            return "redirect:/login";
        }
        User currentUser = userService.getUserByEmail(authentication.getName()).orElse(null);
        if (currentUser != null) {
            userService.deleteAccount(currentUser.getId());
        }
        new SecurityContextLogoutHandler().logout(request, null, authentication);
        return "redirect:/login?deleted=1";
    }
}
