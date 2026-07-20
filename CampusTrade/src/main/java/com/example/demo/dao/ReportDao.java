package com.example.demo.dao;

import java.util.List;

import com.example.demo.model.Report;

public interface ReportDao {
    void save(Report report);

    // ⭕ 管理者ダッシュボード用：全通報一覧（新しい順）
    List<Report> findAll();

    // ⭕ 未対応の通報件数（ダッシュボードのバッジ表示用）
    int countPending();

    // ⭕ 通報のステータスを更新（対応済み・却下）
    void updateStatus(Long id, String status);
}
