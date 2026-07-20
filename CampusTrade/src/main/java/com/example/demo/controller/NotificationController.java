package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.example.demo.dao.NotificationDao;
import com.example.demo.model.User;
import com.example.demo.service.UserService;

// ⭕ 9. 通知一覧・既読管理（値下げ通知・メッセージ通知・オファー通知などをまとめて表示）
@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationDao notificationDao;
    private final UserService userService;

    public NotificationController(NotificationDao notificationDao, UserService userService) {
        this.notificationDao = notificationDao;
        this.userService = userService;
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null) return null;
        return userService.getUserByEmail(authentication.getName()).orElse(null);
    }

    @GetMapping
    public String list(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("notifications", notificationDao.findByUserId(user.getId(), 50));
        notificationDao.markAllAsRead(user.getId());
        return "notification/list";
    }

    @PostMapping("/{id}/read")
    public String markRead(@PathVariable("id") Long id, Authentication authentication) {
        User user = currentUser(authentication);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        notificationDao.markAsRead(id, user.getId());
        return "redirect:/notifications";
    }
}
