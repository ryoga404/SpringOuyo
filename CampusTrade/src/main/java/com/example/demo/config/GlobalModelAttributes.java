package com.example.demo.config;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.dao.NotificationDao;
import com.example.demo.model.User;
import com.example.demo.service.UserService;

// ⭕ ナビゲーションバーなどで、メールアドレスではなく「ニックネーム」を表示するため、
//    ログイン中のユーザー情報を全画面共通で Model に注入する
//    ⭕ 9. あわせて未読通知件数も全画面共通で注入し、ベルアイコンにバッジ表示できるようにする
@ControllerAdvice
public class GlobalModelAttributes {

    private final UserService userService;
    private final NotificationDao notificationDao;

    public GlobalModelAttributes(UserService userService, NotificationDao notificationDao) {
        this.userService = userService;
        this.notificationDao = notificationDao;
    }

    @ModelAttribute("loginUser")
    public User loginUser(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userService.getUserByEmail(authentication.getName()).orElse(null);
    }

    @ModelAttribute("unreadNotificationCount")
    public int unreadNotificationCount(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return 0;
        }
        User user = userService.getUserByEmail(authentication.getName()).orElse(null);
        if (user == null) return 0;
        return notificationDao.countUnread(user.getId());
    }
}
