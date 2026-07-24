package com.example.demo.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageService {

    public static final String IMAGE_DIR = "src/main/resources/static/images/products/";
    public static final int MAX_IMAGES = 5;

    private String fileName(Long productId, int seq) {
        return IMAGE_DIR + productId + "_" + seq + ".jpg";
    }

    public boolean hasImage(Long productId) {
        if (productId == null) return false;
        return Files.exists(Paths.get(fileName(productId, 1)));
    }

    // ⭕ 一覧・詳細のサムネイル表示用：1枚目の画像URL
    public String getImageUrl(Long productId) {
        return "/images/products/" + productId + "_1.jpg";
    }

    // ⭕ 実際に保存されている画像のURL一覧（存在するものだけ、連番順）
    public List<String> getImageUrls(Long productId) {
        List<String> urls = new ArrayList<>();
        if (productId == null) return urls;
        for (int seq = 1; seq <= MAX_IMAGES; seq++) {
            if (Files.exists(Paths.get(fileName(productId, seq)))) {
                urls.add("/images/products/" + productId + "_" + seq + ".jpg");
            }
        }
        return urls;
    }

    // ⭕ アップロードされた複数画像を「商品ID_連番」のファイル名でJPEGとして統一保存する
    //    新規に画像を選択しなかった場合は既存の画像をそのまま維持する。
    public void saveImages(Long productId, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) return;

        // アップロードの中に有効なファイルが1つ以上含まれているか事前確認
        boolean hasValidFile = files.stream().anyMatch(f -> f != null && !f.isEmpty());
        if (!hasValidFile) {
            return; // 有効なファイルがなければ既存画像を維持して終了
        }

        Path dirPath = Paths.get(IMAGE_DIR);
        Files.createDirectories(dirPath);

        // 新しい画像を保存する前に、既存の古い画像を一度クリアする
        deleteImage(productId);

        int seq = 1;
        for (MultipartFile file : files) {
            if (seq > MAX_IMAGES) break;
            if (file == null || file.isEmpty()) continue;

            BufferedImage original = ImageIO.read(file.getInputStream());
            if (original == null) {
                // 画像として読み込めない場合はスキップ
                continue;
            }

            // JPEG変換（白背景のRGB画像に描画）
            BufferedImage rgbImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgbImage.createGraphics();
            try {
                g.drawImage(original, 0, 0, Color.WHITE, null);
            } finally {
                g.dispose(); // グラフィックスリソースの確実な解放
            }

            File outputFile = new File(fileName(productId, seq));
            ImageIO.write(rgbImage, "jpg", outputFile);
            seq++;
        }
    }

    public void deleteImage(Long productId) {
        if (productId == null) return;
        
        for (int seq = 1; seq <= MAX_IMAGES; seq++) {
            try {
                Files.deleteIfExists(Paths.get(fileName(productId, seq)));
            } catch (IOException e) {
                // 削除失敗時は無視
            }
        }
    }
}