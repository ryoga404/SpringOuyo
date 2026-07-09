-- ⭕ 双方確認式の取引完了機能のために products テーブルへカラムを追加します。
--    MariaDB（例: HeidiSQL や `mysql -u root -p furima < migration_add_confirmation.sql`）で
--    このファイルを一度だけ実行してください。

ALTER TABLE `products`
  ADD COLUMN `seller_confirmed` TINYINT(1) NOT NULL DEFAULT 0 AFTER `buyer_id`,
  ADD COLUMN `buyer_confirmed` TINYINT(1) NOT NULL DEFAULT 0 AFTER `seller_confirmed`;
