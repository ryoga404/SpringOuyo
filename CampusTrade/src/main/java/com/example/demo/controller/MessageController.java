package com.example.demo.controller;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dao.MessageDao;
import com.example.demo.dao.ProductDao;
import com.example.demo.model.Message;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/messages")
public class MessageController {

    private final MessageDao messageDao;
    private final ProductDao productDao;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    public MessageController(MessageDao messageDao, ProductDao productDao, UserService userService,
            SimpMessagingTemplate messagingTemplate, NotificationService notificationService) {
        this.messageDao = messageDao;
        this.productDao = productDao;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String index() {
        return "messages/index";
    }

    // 💡 メッセージ送信
    //    買い手未確定（OPEN）の間は receiverId=null の「公開Q&A」として全員に表示される。
    //    買い手確定後（LOCKED/CLOSED）は、出品者・購入者間の「非公開メッセージ」として
    //    相手（送信者が出品者なら購入者、購入者なら出品者）を receiverId に設定する。
    // ⭕ リアルタイムチャット：保存後に /topic/products/{productId} へ配信し、
    //    その商品ページを開いている全員にページ再読み込みなしで届ける。
    @PostMapping("/send")
    public String send(
            @RequestParam Long productId,
            @RequestParam String content,
            Authentication authentication) {

        Product product = productDao.findById(productId);
        if (product == null) {
            return "redirect:/products";
        }

        User currentUser = userService.getUserByEmail(authentication.getName()).orElse(null);
        if (currentUser == null) {
            return "redirect:/products/" + productId;
        }

        Long currentUserId = currentUser.getId();
        boolean isSeller = currentUserId.equals(product.getSellerId());
        boolean isBuyer = product.getBuyerId() != null && currentUserId.equals(product.getBuyerId());

        // 買い手が確定済み（取引が非公開化）の場合、出品者・購入者以外は投稿できない
        if (product.getBuyerId() != null && !isSeller && !isBuyer) {
            return "redirect:/products/" + productId;
        }

        if (content == null || content.trim().isEmpty()) {
            return "redirect:/products/" + productId + "#chat";
        }

        Long receiverId = null;
        if (product.getBuyerId() != null) {
            // 非公開メッセージ：相手側のIDを受信者として設定
            receiverId = isSeller ? product.getBuyerId() : product.getSellerId();
        }

        Message message = new Message();
        message.setProductId(productId);
        message.setSenderId(currentUserId);
        message.setReceiverId(receiverId);
        message.setContent(content.trim());
        messageDao.save(message);

        // ⭕ WebSocketで同じ商品ページを開いている人に即時配信
        Map<String, Object> payload = Map.of(
                "senderId", currentUserId,
                "senderNickname", currentUser.getNickname(),
                "content", message.getContent(),
                "createdAt", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd HH:mm"))
        );
        messagingTemplate.convertAndSend("/topic/products/" + productId, payload);

        // ⭕ 相手に新着メッセージ通知
        if (receiverId != null) {
            notificationService.notify(receiverId, "MESSAGE",
                    currentUser.getNickname() + "さんから「" + product.getProductName() + "」にメッセージが届きました",
                    "/products/" + productId + "#chat");
        }

        return "redirect:/products/" + productId + "#chat";
    }
}
