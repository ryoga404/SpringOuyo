package com.example.demo.config;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.example.demo.dao.ProductDao;
import com.example.demo.dao.UserDao;
import com.example.demo.model.Product;
import com.example.demo.model.User;

// ⭕ 修正1：非公開チャット（買い手確定後のLOCKED/CLOSED商品）のWebSocket配信(/topic/products/{id})を、
//    出品者・購入者以外が購読できてしまう問題を修正する。
//    STOMPのSUBSCRIBEフレームをここで検査し、権限が無ければ購読自体を拒否する。
@Component
public class ChatChannelInterceptor implements ChannelInterceptor {

    private static final Pattern TOPIC_PATTERN = Pattern.compile("^/topic/products/(\\d+)$");

    private final ProductDao productDao;
    private final UserDao userDao;

    public ChatChannelInterceptor(ProductDao productDao, UserDao userDao) {
        this.productDao = productDao;
        this.userDao = userDao;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            if (destination != null) {
                Matcher matcher = TOPIC_PATTERN.matcher(destination);
                if (matcher.matches()) {
                    Long productId = Long.valueOf(matcher.group(1));
                    Product product = productDao.findById(productId);
                    if (product == null) {
                        throw new MessagingException("指定された商品が見つかりません");
                    }

                    // 買い手が確定している（非公開チャットになっている）場合のみ、当事者チェックを行う
                    boolean isPrivateChat = product.getBuyerId() != null;
                    if (isPrivateChat) {
                        Long currentUserId = resolveUserId(accessor.getUser());
                        boolean allowed = currentUserId != null
                                && (currentUserId.equals(product.getSellerId()) || currentUserId.equals(product.getBuyerId()));
                        if (!allowed) {
                            throw new MessagingException("このチャットを購読する権限がありません");
                        }
                    }
                }
            }
        }

        return message;
    }

    private Long resolveUserId(Principal principal) {
        if (principal == null) return null;
        return userDao.findByEmail(principal.getName()).map(User::getId).orElse(null);
    }
}
