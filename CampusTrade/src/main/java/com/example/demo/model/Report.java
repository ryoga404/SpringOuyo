package com.example.demo.model;

import java.time.LocalDateTime;

// ⭕ 不適切な出品をユーザーが通報するための情報。管理者ダッシュボードから確認・対応する。
public class Report {
    private Long id;
    private Long productId;
    private Long reporterId;
    private String reason;   // 例: "禁止物の出品", "詐欺の疑い", "その他"
    private String detail;
    private String status;   // PENDING / REVIEWED / DISMISSED
    private LocalDateTime createdAt;

    // 表示用
    private String productName;
    private String reporterNickname;

    public Report() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getReporterNickname() { return reporterNickname; }
    public void setReporterNickname(String reporterNickname) { this.reporterNickname = reporterNickname; }
}
