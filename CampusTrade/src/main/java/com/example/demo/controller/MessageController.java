package com.example.demo.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; // ⭕ 変更
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;

@Controller
public class MessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 通常のフォーム送信 (POST /messages/send) を処理するメソッド
     */
    @PostMapping("/messages/send")
    public String handleFormSendMessage(
            @RequestParam("productId") Long productId,
            @RequestParam("content") String content,
            @AuthenticationPrincipal UserDetails userDetails) { // ⭕ CustomUserDetails から UserDetails へ変更

        if (userDetails == null) {
            return "redirect:/login";
        }

        // ⭕ userDetails.getUsername() は SecurityConfig より email が入っています
        User sender = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

        // メッセージの作成・保存
        Message message = new Message();
        message.setContent(content);
        message.setProductId(productId);
        message.setSenderId(sender.getId());
        message.setSender(sender);
        message.setCreatedAt(LocalDateTime.now());
        message.setRead(false);

        Message savedMessage = messageRepository.save(message);

        // WebSocketで接続しているクライアントへ通知
        messagingTemplate.convertAndSend("/topic/products/" + productId, savedMessage);

        return "redirect:/products/" + productId + "#chat";
    }

    /**
     * WebSocket経由でメッセージを受信・保存する
     */
    @MessageMapping("/chat/{productId}")
    public void sendMessage(
            @DestinationVariable Long productId,
            @Payload Message messageRequest,
            @AuthenticationPrincipal UserDetails userDetails) { // ⭕ CustomUserDetails から UserDetails へ変更

        if (userDetails == null) {
            throw new RuntimeException("認証されていません");
        }
        
        User sender = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

        Message message = new Message();
        message.setContent(messageRequest.getContent());
        message.setProductId(productId);
        message.setSenderId(sender.getId());
        message.setSender(sender);
        message.setCreatedAt(LocalDateTime.now());
        message.setRead(false);

        Message savedMessage = messageRepository.save(message);

        messagingTemplate.convertAndSend("/topic/products/" + productId, savedMessage);
    }
}