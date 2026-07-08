-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- ホスト: 127.0.0.1
-- 生成日時: 2026-07-08 05:09:18
-- サーバのバージョン： 10.4.32-MariaDB
-- PHP のバージョン: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- データベース: `furima`
--

-- --------------------------------------------------------

--
-- テーブルの構造 `category`
--

CREATE TABLE `category` (
  `id` int(5) NOT NULL,
  `BigCategory` varchar(50) DEFAULT NULL,
  `MidCategory` varchar(50) DEFAULT NULL,
  `SmallCategory` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- テーブルのデータのダンプ `category`
--

INSERT INTO `category` (`id`, `BigCategory`, `MidCategory`, `SmallCategory`) VALUES
(101, '教材、学業関連', '教材・学業関連', NULL),
(102, '教材、学業関連', '教科書', '前学期の専門書や語学テキスト'),
(103, '教材、学業関連', '参考書', '資格試験（TOEIC、ITパスポートなど）の対策本'),
(104, '教材、学業関連', '文房具', '余ったノート、ルーズリーフ、未使用のペン'),
(105, '教材、学業関連', 'PC・タブレット・スマホ', NULL),
(106, '教材、学業関連', 'その他', NULL),
(201, 'ファッション・衣類', 'ファッション・衣類', NULL),
(202, 'ファッション・衣類', '私服', 'サイズが合わなくなった服、流行の古着'),
(203, 'ファッション・衣類', '就活スーツ', 'リクルートスーツ、ネクタイ、パンプス'),
(204, 'ファッション・衣類', 'バッグ', '通学用リュック、トートバッグ'),
(205, 'ファッション・衣類', '衣装', 'サークルや学園祭で一度だけ使ったコスプレ・ドレス'),
(301, '一人暮らし応援・日用品', '一人暮らし応援・日用品', NULL),
(302, '一人暮らし応援・日用品', '小型家電', 'ケトル、ドライヤー、ミニ扇風機、卓上ライト'),
(303, '一人暮らし応援・日用品', 'キッチン用品', 'マグカップ、お皿、未使用の調理器具'),
(304, '一人暮らし応援・日用品', 'インテリア', '収納ボックス、クッション、ポスター'),
(305, '一人暮らし応援・日用品', '消耗品', '実家から送られてきて余ったレトルト食品や洗剤'),
(401, '趣味・エンタメ', '趣味・エンタメ', NULL),
(402, '趣味・エンタメ', '本・マンガ', '読み終えた小説、コミック全巻セット'),
(403, '趣味・エンタメ', 'ゲーム・CD', '遊ばなくなったゲームソフト、周辺機器'),
(404, '趣味・エンタメ', 'サークル関連', '楽器、テニスラケット、スニーカー'),
(405, '趣味・エンタメ', 'ハンドメイド・その他', NULL),
(406, '趣味・エンタメ', '手作り雑貨', 'アクセサリー、布小物、ステッカー'),
(407, '趣味・エンタメ', 'おまけ・試供品', '化粧品のサンプル、ガチャガチャの景品');

-- --------------------------------------------------------

--
-- テーブルの構造 `messages`
--

CREATE TABLE `messages` (
  `id` bigint(20) NOT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `sender_id` bigint(20) DEFAULT NULL,
  `receiver_id` bigint(20) DEFAULT NULL,
  `content` text NOT NULL,
  `create_at` datetime NOT NULL DEFAULT current_timestamp(),
  `is_read` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- テーブルの構造 `products`
--

CREATE TABLE `products` (
  `id` bigint(20) NOT NULL,
  `product_name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `price` int(11) NOT NULL,
  `category_id` int(5) DEFAULT NULL,
  `status` varchar(255) NOT NULL DEFAULT 'OPEN',
  `seller_id` bigint(20) DEFAULT NULL,
  `buyer_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- テーブルのデータのダンプ `products`
--

INSERT INTO `products` (`id`, `product_name`, `description`, `price`, `category_id`, `status`, `seller_id`, `buyer_id`) VALUES
(1, 'システム開発論 教科書', '昨年度の講義で使用した教科書です。書き込みはありません。', 1500, 101, 'OPEN', 2, NULL),
(2, 'ミニ冷蔵庫（2023年製）', '引越しに伴い不要になったため譲ります。動作確認済み、綺麗です。', 5000, 202, 'OPEN', 2, NULL),
(3, 'キャンパスチェア（折りたたみ式）', 'サークル活動や屋外イベントで使える折りたたみ椅子です。', 800, 301, 'OPEN', 2, NULL),
(4, '理系大学生向け関数電卓', 'テスト用に購入し、数回しか使用していません。箱・説明書付き。', 2200, 4, 'OPEN', 2, NULL),
(5, '通学用クロスバイク（鍵・ライト付き）', '卒業に伴い手放します。防犯登録の解除手続きをしてからお渡しします。', 12000, 5, 'OPEN', 2, NULL),
(6, '【美品】MacBook Air 13インチ (M1)', 'レポート作成で使用。ケースをつけていたため目立つ傷はありません。', 65000, 4, 'OPEN', 2, NULL),
(7, '大学指定 体育館シューズ (26.5cm)', 'サイズを間違えて購入したため、未使用のまま出品します。', 1800, 6, 'OPEN', 2, NULL);

-- --------------------------------------------------------

--
-- テーブルの構造 `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nickname` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- テーブルのデータのダンプ `users`
--

INSERT INTO `users` (`id`, `email`, `password`, `nickname`, `role`) VALUES
(1, 'example@example.com', '$2a$10$uG0lIc6455I31OwBlNXcZezjdDcLV0F2YNrm.wOBlfSltssU05ZOW', NULL, 'USER'),
(2, 'test@test.com', '$2a$10$yjvCFxPt0nLGVbKsRroj3O6knz9.9J1Ky/6M62KaSukexi6FCfDmC', NULL, 'USER');

-- --------------------------------------------------------

--
-- テーブルの構造 `user_favorite`
--

CREATE TABLE `user_favorite` (
  `user_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- ダンプしたテーブルのインデックス
--

--
-- テーブルのインデックス `category`
--
ALTER TABLE `category`
  ADD PRIMARY KEY (`id`);

--
-- テーブルのインデックス `messages`
--
ALTER TABLE `messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `product_id` (`product_id`),
  ADD KEY `sender_id` (`sender_id`),
  ADD KEY `receiver_id` (`receiver_id`);

--
-- テーブルのインデックス `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id`),
  ADD KEY `seller_id` (`seller_id`),
  ADD KEY `buyer_id` (`buyer_id`);

--
-- テーブルのインデックス `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`email`);

--
-- テーブルのインデックス `user_favorite`
--
ALTER TABLE `user_favorite`
  ADD PRIMARY KEY (`user_id`,`product_id`),
  ADD KEY `product_id` (`product_id`);

--
-- ダンプしたテーブルの AUTO_INCREMENT
--

--
-- テーブルの AUTO_INCREMENT `messages`
--
ALTER TABLE `messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- テーブルの AUTO_INCREMENT `products`
--
ALTER TABLE `products`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- テーブルの AUTO_INCREMENT `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- ダンプしたテーブルの制約
--

--
-- テーブルの制約 `messages`
--
ALTER TABLE `messages`
  ADD CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  ADD CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `messages_ibfk_3` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`);

--
-- テーブルの制約 `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `products_ibfk_1` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `products_ibfk_2` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`);

--
-- テーブルの制約 `user_favorite`
--
ALTER TABLE `user_favorite`
  ADD CONSTRAINT `user_favorite_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `user_favorite_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
