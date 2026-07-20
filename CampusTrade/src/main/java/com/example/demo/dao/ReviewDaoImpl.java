package com.example.demo.dao;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Review;
import com.example.demo.model.User;

@Repository
public class ReviewDaoImpl implements ReviewDao {

    private final JdbcTemplate jdbcTemplate;

    public ReviewDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Review> reviewRowMapper = (rs, rowNum) -> {
        Review review = new Review();
        review.setId(rs.getLong("id"));
        review.setProductId(rs.getLong("product_id"));
        review.setReviewerId(rs.getLong("reviewer_id"));
        review.setRevieweeId(rs.getLong("reviewee_id"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        User reviewer = new User();
        reviewer.setId(rs.getLong("reviewer_id"));
        reviewer.setNickname(rs.getString("reviewer_nickname"));
        review.setReviewer(reviewer);

        return review;
    };

    @Override
    public void save(Review review) {
        String sql = "INSERT INTO reviews (product_id, reviewer_id, reviewee_id, rating, comment, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, NOW())";
        jdbcTemplate.update(sql, review.getProductId(), review.getReviewerId(), review.getRevieweeId(),
                review.getRating(), review.getComment());
    }

    @Override
    public boolean hasReviewed(Long productId, Long reviewerId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE product_id = ? AND reviewer_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, productId, reviewerId);
        return count != null && count > 0;
    }

    @Override
    public List<Review> findByRevieweeId(Long revieweeId) {
        String sql = "SELECT r.*, u.nickname AS reviewer_nickname " +
                     "FROM reviews r JOIN users u ON r.reviewer_id = u.id " +
                     "WHERE r.reviewee_id = ? ORDER BY r.created_at DESC";
        return jdbcTemplate.query(sql, reviewRowMapper, revieweeId);
    }

    @Override
    public Double getAverageRating(Long revieweeId) {
        String sql = "SELECT AVG(rating) FROM reviews WHERE reviewee_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Double.class, revieweeId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public int countByRevieweeId(Long revieweeId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE reviewee_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, revieweeId);
        return count != null ? count : 0;
    }
}
