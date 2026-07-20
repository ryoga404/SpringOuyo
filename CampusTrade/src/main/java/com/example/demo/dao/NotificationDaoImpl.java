package com.example.demo.dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Notification;

@Repository
public class NotificationDaoImpl implements NotificationDao {

    private final JdbcTemplate jdbcTemplate;

    public NotificationDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Notification> notificationRowMapper = (rs, rowNum) -> {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setUserId(rs.getLong("user_id"));
        n.setType(rs.getString("type"));
        n.setContent(rs.getString("content"));
        n.setLink(rs.getString("link"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return n;
    };

    @Override
    public void save(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, type, content, link, is_read, created_at) " +
                     "VALUES (?, ?, ?, ?, 0, NOW())";
        jdbcTemplate.update(sql, notification.getUserId(), notification.getType(),
                notification.getContent(), notification.getLink());
    }

    @Override
    public List<Notification> findByUserId(Long userId, int limit) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, notificationRowMapper, userId, limit);
    }

    @Override
    public int countUnread(Long userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    @Override
    public void markAsRead(Long id, Long userId) {
        jdbcTemplate.update("UPDATE notifications SET is_read = 1 WHERE id = ? AND user_id = ?", id, userId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        jdbcTemplate.update("UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0", userId);
    }
}
