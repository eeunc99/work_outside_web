package com.app.work.config;

import com.app.work.interceptor.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .order(1)
                .addPathPatterns("/admin/**", "/sd", "/statistics/**");

        registry.addInterceptor(new AdminInterceptor())
                .order(2)
                .addPathPatterns("/admin/**", "/statistics/statistics-admin");

        registry.addInterceptor(new ListInterceptor())
                .order(2)
                .addPathPatterns("/list/list");

        registry.addInterceptor(new LeaderInterceptor())
                .order(2)
                .addPathPatterns("/statistics/statistics");
    }
}
