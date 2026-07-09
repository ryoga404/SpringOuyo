package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// ⭕ 商品画像を実行時に保存したファイルを、クラスパスの再読込を待たずに
//    確実に配信できるよう、ディスク上のフォルダを直接マッピングする
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/products/**")
                .addResourceLocations("file:src/main/resources/static/images/products/");
    }
}
