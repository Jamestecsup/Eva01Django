package com.tecup.exam_1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AccesoInterceptor accesoInterceptor;

    public WebConfig(AccesoInterceptor accesoInterceptor) {
        this.accesoInterceptor = accesoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accesoInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/logout",
                        "/recuperar",
                        "/recuperar/**",
                        "/restablecer",
                        "/restablecer/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico",
                        "/api/**"
                );
    }
}