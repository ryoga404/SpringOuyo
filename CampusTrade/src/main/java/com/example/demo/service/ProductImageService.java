package com.example.demo.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// 💡 学内演習用の簡易実装：
//    src/main/resources/static/images/products/ 配下に「商品ID.jpg」という
//    ファイル名で画像を保存し、そのまま静的リソースとして配信します。
//    画像が無い商品は単に「画像なし」として扱います。
@Service
public class ProductImageService {

    public static final String IMAGE_DIR = "src/main/resources/static/images/products/";

    public boolean hasImage(Long productId) {
        if (productId == null) return false;
        return Files.exists(Paths.get(IMAGE_DIR + productId + ".jpg"));
    }

    public String getImageUrl(Long productId) {
        return "/images/products/" + productId + ".jpg";
    }

    // ⭕ アップロードされた画像を商品IDのファイル名でJPEGとして統一保存する
    public void saveImage(Long productId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return;

        Path dirPath = Paths.get(IMAGE_DIR);
        Files.createDirectories(dirPath);

        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) {
            // 画像として読み込めない場合（画像以外のファイルが指定された等）は保存しない
            return;
        }

        // JPEGはアルファチャンネルを扱えないため、白背景のRGB画像に変換してから保存する
        BufferedImage rgbImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        rgbImage.createGraphics().drawImage(original, 0, 0, Color.WHITE, null);

        File outputFile = new File(IMAGE_DIR + productId + ".jpg");
        ImageIO.write(rgbImage, "jpg", outputFile);
    }

    public void deleteImage(Long productId) {
        try {
            Files.deleteIfExists(Paths.get(IMAGE_DIR + productId + ".jpg"));
        } catch (IOException e) {
            // 削除に失敗しても致命的ではないため無視する
        }
    }
}
