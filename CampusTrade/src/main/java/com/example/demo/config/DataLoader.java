package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. テスト用ユーザーの作成
        User studentA = new User();
        studentA.setUserId("student01"); // 学生番号（ログインID）
        studentA.setPassword(passwordEncoder.encode("password")); // パスワードを暗号化
        studentA.setNickname("拓也（A君）");
        studentA.setRole("ROLE_USER");
        userRepository.save(studentA);

        User studentB = new User();
        studentB.setUserId("student02");
        studentB.setPassword(passwordEncoder.encode("password"));
        studentB.setNickname("美咲（Bさん）");
        studentB.setRole("ROLE_USER");
        userRepository.save(studentB);

        // 2. 初期商品データの作成（画像なし）
        Product p1 = new Product();
        p1.setProductName("Javaプログラミング基礎（教科書）");
        p1.setDescription("昨年の講義で使用しました。少し書き込みがありますが通読には問題ありません。");
        p1.setPrice(1200);
        p1.setCategory("教科書");
        p1.setStatus("OPEN");
        p1.setSeller(studentA); // 出品者はstudent01
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setProductName("ミニ冷蔵庫（動作確認済み）");
        p2.setDescription("引越しに伴い不要になったため譲ります。綺麗に使っていました。0円でも可。");
        p2.setPrice(0);
        p2.setCategory("家電");
        p2.setStatus("OPEN");
        p2.setSeller(studentB); // 出品者はstudent02
        productRepository.save(p2);
        
        System.out.println("====== テスト用初期データの投入が完了しました ======");
        System.out.println("ログインID: student01 / パスワード: password");
    }
}