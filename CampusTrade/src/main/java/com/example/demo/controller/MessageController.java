package com.example.demo.controller;

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
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/messages")
public class MessageController {

    private final MessageDao messageDao;
    private final ProductDao productDao;
    private final UserService userService;

    public MessageController(MessageDao messageDao, ProductDao productDao, UserService userService) {
        this.messageDao = messageDao;
        this.productDao = productDao;
        this.userService = userService;
    }

    @GetMapping
    public String index() {
        return "messages/index";
    }

    // 💡 メッセージ送信
    //    買い手未確定（OPEN）の間は receiverId=null の「公開Q&A」として全員に表示される。
    //    買い手確定後（LOCKED/CLOSED）は、出品者・購入者間の「非公開メッセージ」として
    //    相手（送信者が出品者なら購入者、購入者なら出品者）を receiverId に設定する。
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

        return "redirect:/products/" + productId + "#chat";
    }
}
