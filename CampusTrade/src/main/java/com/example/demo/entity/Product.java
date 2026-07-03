package com.example.demo.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer price; // 0円含む

    private String category; // 教科書、家具、家電等
    
    // ステータス管理 (OPEN: 出品中, LOCKED: 交渉中/取引中, CLOSED: 取引完了, BANNED: 禁止)
    @Column(nullable = false)
    private String status = "OPEN"; 

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller; // 出品者

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer; // 購入者 (null可)

    // 1つの商品に対して複数のメッセージがぶら下がる (1:多)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Message> messages;
}