package com.example.demo.dao;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Offer;

@Repository
public class OfferDaoImpl implements OfferDao {

    private final JdbcTemplate jdbcTemplate;

    public OfferDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Offer> offerRowMapper = (rs, rowNum) -> {
        Offer offer = new Offer();
        offer.setId(rs.getLong("id"));
        offer.setProductId(rs.getLong("product_id"));
        offer.setSenderId(rs.getLong("sender_id"));
        offer.setOfferPrice(rs.getInt("offer_price"));
        offer.setStatus(rs.getString("status"));
        offer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        offer.setSenderNickname(rs.getString("sender_nickname"));
        return offer;
    };

    @Override
    public void save(Offer offer) {
        String sql = "INSERT INTO offers (product_id, sender_id, offer_price, status, created_at) " +
                     "VALUES (?, ?, ?, 'PENDING', NOW())";
        jdbcTemplate.update(sql, offer.getProductId(), offer.getSenderId(), offer.getOfferPrice());
    }

    @Override
    public List<Offer> findByProductId(Long productId) {
        String sql = "SELECT o.*, u.nickname AS sender_nickname FROM offers o " +
                     "JOIN users u ON o.sender_id = u.id " +
                     "WHERE o.product_id = ? ORDER BY o.created_at DESC";
        return jdbcTemplate.query(sql, offerRowMapper, productId);
    }

    @Override
    public Offer findById(Long id) {
        String sql = "SELECT o.*, u.nickname AS sender_nickname FROM offers o " +
                     "JOIN users u ON o.sender_id = u.id WHERE o.id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, offerRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void updateStatus(Long id, String status) {
        jdbcTemplate.update("UPDATE offers SET status = ? WHERE id = ?", status, id);
    }

    @Override
    public boolean hasPendingOffer(Long productId, Long senderId) {
        String sql = "SELECT COUNT(*) FROM offers WHERE product_id = ? AND sender_id = ? AND status = 'PENDING'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, productId, senderId);
        return count != null && count > 0;
    }

    @Override
    public void rejectOtherPendingOffers(Long productId, Long acceptedOfferId) {
        String sql = "UPDATE offers SET status = 'REJECTED' WHERE product_id = ? AND id != ? AND status = 'PENDING'";
        jdbcTemplate.update(sql, productId, acceptedOfferId);
    }

    @Override
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM offers WHERE status = 'PENDING'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
