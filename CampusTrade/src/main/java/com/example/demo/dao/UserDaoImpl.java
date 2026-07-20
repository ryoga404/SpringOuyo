package com.example.demo.dao;

import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.demo.model.User;

@Repository
public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("nickname"),
                rs.getString("role")
        );
        user.setDeleted(rs.getTimestamp("deleted_at") != null);
        return user;
    };

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, email, password, nickname, role, deleted_at FROM users WHERE email = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT id, email, password, nickname, role, deleted_at FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // ⭕ INSERT文を実行してDBにユーザーを保存する処理
    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (email, password, nickname, role) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getEmail(), user.getPassword(), user.getNickname(), user.getRole());
    }

    // ⭕ ニックネームのみ更新
    @Override
    public void updateNickname(Long userId, String nickname) {
        String sql = "UPDATE users SET nickname = ? WHERE id = ?";
        jdbcTemplate.update(sql, nickname, userId);
    }

    // ⭕ 退会機能：物理削除ではなく deleted_at を設定するだけ（出品履歴・メッセージの整合性を保つため）
    @Override
    public void softDelete(Long userId) {
        String sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, userId);
    }
}
