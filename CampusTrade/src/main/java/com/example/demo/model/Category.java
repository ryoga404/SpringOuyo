package com.example.demo.model;

public class Category {
    private Integer id;
    private String bigCategory;
    private String midCategory;
    private String smallCategory;

    public Category() {}

    public Category(Integer id, String bigCategory, String midCategory, String smallCategory) {
        this.id = id;
        this.bigCategory = bigCategory;
        this.midCategory = midCategory;
        this.smallCategory = smallCategory;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getBigCategory() { return bigCategory; }
    public void setBigCategory(String bigCategory) { this.bigCategory = bigCategory; }

    public String getMidCategory() { return midCategory; }
    public void setMidCategory(String midCategory) { this.midCategory = midCategory; }

    public String getSmallCategory() { return smallCategory; }
    public void setSmallCategory(String smallCategory) { this.smallCategory = smallCategory; }
}
