-- CampusTrade 追加機能用スキーマ（実際のテーブル定義に合わせて調整）
-- furima データベースに対して、上から順に実行してください

USE furima;

-- 3. レビュー機能
CREATE TABLE reviews (
  id BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT(20) NOT NULL,
  reviewer_id BIGINT(20) NOT NULL,
  reviewee_id BIGINT(20) NOT NULL,
  rating INT NOT NULL,
  comment TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_review UNIQUE (product_id, reviewer_id),
  CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id),
  CONSTRAINT fk_reviews_reviewee FOREIGN KEY (reviewee_id) REFERENCES users(id)
);

-- 6. 通報機能
CREATE TABLE reports (
  id BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT(20) NOT NULL,
  reporter_id BIGINT(20) NOT NULL,
  reason VARCHAR(50) NOT NULL,
  detail VARCHAR(500),
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_reports_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id)
);

-- 7. 価格交渉（オファー）
CREATE TABLE offers (
  id BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT(20) NOT NULL,
  sender_id BIGINT(20) NOT NULL,
  offer_price INT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_offers_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_offers_sender FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- 9. 通知（値下げ通知・メッセージ通知・オファー通知など）
CREATE TABLE notifications (
  id BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT(20) NOT NULL,
  type VARCHAR(30) NOT NULL,
  content VARCHAR(255) NOT NULL,
  link VARCHAR(255),
  is_read TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 11. 退会機能（ソフトデリート）
ALTER TABLE users ADD COLUMN deleted_at DATETIME NULL;

-- 12. 検索強化（人気順ソート用）
ALTER TABLE products ADD COLUMN view_count INT NOT NULL DEFAULT 0;
