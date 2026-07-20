package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.dao.UserDao;
import com.example.demo.model.User;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDao userDao;

    public SecurityConfig(UserDao userDao) {
        this.userDao = userDao;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                // ⭕ 8. WebSocket(STOMP/SockJS)のハンドシェイクはXHR POSTを伴うため、CSRF保護の対象外とする
                .ignoringRequestMatchers("/ws-chat/**")
            )
            .authorizeHttpRequests(auth -> auth
                // ⭕ /register を追加して、未ログインでもユーザー登録画面を開けるようにします
            		// SecurityConfig.java 内の該当箇所
            		.requestMatchers("/login", "/register", "/", "/home", "/products", "/css/**", "/js/**", "/images/**", "/ws-chat/**").permitAll()
                // ⭕ 商品一覧・詳細は未ログインでも閲覧できる（購入・出品・コメントは別途認証必須）
                .requestMatchers(HttpMethod.GET, "/products/{id:[0-9]+}").permitAll()
                // ⭕ 管理者機能は ROLE_ADMIN のユーザーのみアクセス可能（URL直打ち対策）
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            User user = userDao.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole())
                    // ⭕ 退会済み（deleted_at が設定済み）のアカウントはログインできないようにする
                    .disabled(user.isDeleted())
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}