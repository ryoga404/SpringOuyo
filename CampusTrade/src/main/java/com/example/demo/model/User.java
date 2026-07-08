package com.example.demo.model;

public class User {
    private Long id;
    private String email;
    private String password;
    private String role;

    public User() {}

    public User(Long id, String email, String password, String role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // 💡 HTMLエラー防止用：nicknameの代わりにemailの＠より前を返す
    public String getNickname() {
        if (this.email != null && this.email.contains("@")) {
            return this.email.split("@")[0];
        }
        return this.email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}