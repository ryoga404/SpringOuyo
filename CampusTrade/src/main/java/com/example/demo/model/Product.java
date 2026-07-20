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

    // ⭕ 双方確認式の取引完了フラグ（seller_confirmed / buyer_confirmed カラムに対応）
    private boolean sellerConfirmed;
    private boolean buyerConfirmed;

    // Thymeleafでの表示用（テーブル結合の代わり、または簡易保持用）
    private String category;
    private User seller;

    // ⭕ 商品画像の有無（DBカラムではなく、画像ファイルの存在チェック結果を保持する一時的な項目）
    private boolean hasImage;

    // ⭕ 商品詳細ページの閲覧数（人気順ソート・検索強化用）
    private int viewCount;

    // ⭕ 複数画像対応：実際に存在する画像のURL一覧（一時的な項目、DBカラムではない）
    private java.util.List<String> imageUrls = new java.util.ArrayList<>();

    // ⭕ レビュー機能：この商品の取引についた平均評価・件数（一時的な項目）
    private Double averageRating;
    private int reviewCount;

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

    public boolean isSellerConfirmed() { return sellerConfirmed; }
    public void setSellerConfirmed(boolean sellerConfirmed) { this.sellerConfirmed = sellerConfirmed; }

    public boolean isBuyerConfirmed() { return buyerConfirmed; }
    public void setBuyerConfirmed(boolean buyerConfirmed) { this.buyerConfirmed = buyerConfirmed; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public boolean isHasImage() { return hasImage; }
    public void setHasImage(boolean hasImage) { this.hasImage = hasImage; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public java.util.List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(java.util.List<String> imageUrls) { this.imageUrls = imageUrls; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
}
