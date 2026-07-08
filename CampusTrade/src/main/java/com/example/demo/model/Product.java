package com.example.demo.model;

public class Product {
    private Long id;
    private String productName;
    private String description;
    private Integer price;
    private Integer categoryId;
    private String status;
    private Long sellerId;
    private Long buyerId;

    // Thymeleafでの表示用（テーブル結合の代わり、または簡易保持用）
    private String category;
    private User seller;

    public Product() {}

    public Product(Long id, String productName, String description, Integer price, 
                   Integer categoryId, String status, Long sellerId, Long buyerId) {
        this.id = id;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.status = status;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
    }

    // ゲッター・セッター
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }
}