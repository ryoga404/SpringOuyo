package com.example.demo.dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Report;

@Repository
public class ReportDaoImpl implements ReportDao {

    private final JdbcTemplate jdbcTemplate;

    public ReportDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Report> reportRowMapper = (rs, rowNum) -> {
        Report report = new Report();
        report.setId(rs.getLong("id"));
        report.setProductId(rs.getLong("product_id"));
        report.setReporterId(rs.getLong("reporter_id"));
        report.setReason(rs.getString("reason"));
        report.setDetail(rs.getString("detail"));
        report.setStatus(rs.getString("status"));
        report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        report.setProductName(rs.getString("product_name"));
        report.setReporterNickname(rs.getString("reporter_nickname"));
        return report;
    };

    @Override
    public void save(Report report) {
        String sql = "INSERT INTO reports (product_id, reporter_id, reason, detail, status, created_at) " +
                     "VALUES (?, ?, ?, ?, 'PENDING', NOW())";
        jdbcTemplate.update(sql, report.getProductId(), report.getReporterId(), report.getReason(), report.getDetail());
    }

    @Override
    public List<Report> findAll() {
        String sql = "SELECT r.*, p.product_name AS product_name, u.nickname AS reporter_nickname " +
                     "FROM reports r " +
                     "JOIN products p ON r.product_id = p.id " +
                     "JOIN users u ON r.reporter_id = u.id " +
                     "ORDER BY r.created_at DESC";
        return jdbcTemplate.query(sql, reportRowMapper);
    }

    @Override
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM reports WHERE status = 'PENDING'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public void updateStatus(Long id, String status) {
        jdbcTemplate.update("UPDATE reports SET status = ? WHERE id = ?", status, id);
    }
}
