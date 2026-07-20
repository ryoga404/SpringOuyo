package com.example.demo.service;

import java.awt.Color;
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

// 💡 学内演習用の簡易実装：
//    src/main/resources/static/images/products/ 配下に「商品ID_連番.jpg」という
//    ファイル名で画像を保存し、そのまま静的リソースとして配信します。
//    ⭕ 複数枚（最大 MAX_IMAGES 枚）対応：商品ID_1.jpg 〜 商品ID_5.jpg のように連番で保存する。
//       画像が無い商品は単に「画像なし」として扱う。
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

        Path dirPath = Paths.get(IMAGE_DIR);
        Files.createDirectories(dirPath);

        int seq = 1;
        for (MultipartFile file : files) {
            if (seq > MAX_IMAGES) break;
            if (file == null || file.isEmpty()) continue;

            BufferedImage original = ImageIO.read(file.getInputStream());
            if (original == null) {
                // 画像として読み込めない場合（画像以外のファイルが指定された等）はスキップ
                continue;
            }

            // JPEGはアルファチャンネルを扱えないため、白背景のRGB画像に変換してから保存する
            BufferedImage rgbImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgbImage.createGraphics().drawImage(original, 0, 0, Color.WHITE, null);

            File outputFile = new File(fileName(productId, seq));
            ImageIO.write(rgbImage, "jpg", outputFile);
            seq++;
        }
    }

    public void deleteImage(Long productId) {
        for (int seq = 1; seq <= MAX_IMAGES; seq++) {
            try {
                Files.deleteIfExists(Paths.get(fileName(productId, seq)));
            } catch (IOException e) {
                // 削除に失敗しても致命的ではないため無視する
            }
        }
    }
}
