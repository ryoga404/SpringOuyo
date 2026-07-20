package com.example.demo.model;

public class User {
    private Long id;
    private String email;
    private String password;
    private String nickname;
    private String role;

    // ⭕ 退会機能（ソフトデリート）用。deleted_at が NULL でなければ退会済みとみなす。
    private boolean deleted;

    public User() {}

    public User(Long id, String email, String password, String nickname, String role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // 💡 nicknameが未設定(NULL)の場合はemailの＠より前を仮の表示名として返す
    public String getNickname() {
        if (this.nickname != null && !this.nickname.trim().isEmpty()) {
            return this.nickname;
        }
        if (this.email != null && this.email.contains("@")) {
            return this.email.split("@")[0];
        }
        return this.email;
    }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
