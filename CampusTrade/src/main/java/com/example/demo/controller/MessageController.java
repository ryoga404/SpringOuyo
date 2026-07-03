package com.example.demo.controller; // パッケージ名はご自身の環境に合わせてください

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Message;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;

@Controller
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/send")
    public String sendMessage(
            @RequestParam("productId") Long productId,
            @RequestParam("content") String content,
            @AuthenticationPrincipal UserDetails userDetails) { // 👈 ログイン情報を取得

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("無効な商品ID:" + productId));

        // 1. ログイン中のユーザー（ユーザー名=学生番号）をDBから取得
        User sender = userRepository.findByUserId(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません"));

        // 2. メッセージエンティティにデータをセット
        Message message = new Message();
        message.setProduct(product);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        message.setSender(sender);       // 👈 これが抜けていたためエラーが起きていました！
        message.setIsRead(false);

        // 3. 保存して元の商品の詳細画面へリダイレクト
        messageRepository.save(message);

        return "redirect:/products/" + productId;
    }
}