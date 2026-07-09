package com.example.demo.dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Message;
import com.example.demo.model.User;

@Repository
public class MessageDaoImpl implements MessageDao {

    private final JdbcTemplate jdbcTemplate;

    public MessageDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 💡 実際のDBカラムは product_id, sender_id, receiver_id, content, create_at, is_read（parent_idは無い）
    private final RowMapper<Message> messageRowMapper = (rs, rowNum) -> {
        Message message = new Message();
        message.setId(rs.getLong("id"));
        message.setProductId(rs.getLong("product_id"));
        message.setSenderId(rs.getLong("sender_id"));
        long receiverId = rs.getLong("receiver_id");
        message.setReceiverId(rs.wasNull() ? null : receiverId);
        message.setContent(rs.getString("content"));
        message.setCreatedAt(rs.getTimestamp("create_at").toLocalDateTime());
        message.setRead(rs.getBoolean("is_read"));

        User sender = new User();
        sender.setId(rs.getLong("sender_id"));
        sender.setEmail(rs.getString("sender_email"));
        sender.setNickname(rs.getString("sender_nickname"));
        message.setSender(sender);

        return message;
    };

    @Override
    public List<Message> findByProductId(Long productId) {
        String sql = "SELECT m.id, m.product_id, m.sender_id, m.receiver_id, m.content, m.create_at, m.is_read, " +
                     "u.email AS sender_email, u.nickname AS sender_nickname " +
                     "FROM messages m " +
                     "JOIN users u ON m.sender_id = u.id " +
                     "WHERE m.product_id = ? " +
                     "ORDER BY m.create_at ASC";
        return jdbcTemplate.query(sql, messageRowMapper, productId);
    }

    @Override
    public void save(Message message) {
        String sql = "INSERT INTO messages (product_id, sender_id, receiver_id, content, create_at, is_read) " +
                     "VALUES (?, ?, ?, ?, NOW(), 0)";
        jdbcTemplate.update(sql, message.getProductId(), message.getSenderId(), message.getReceiverId(), message.getContent());
    }

    @Override
    public void markAsRead(Long productId, Long receiverId) {
        String sql = "UPDATE messages SET is_read = 1 WHERE product_id = ? AND receiver_id = ? AND is_read = 0";
        jdbcTemplate.update(sql, productId, receiverId);
    }
}
