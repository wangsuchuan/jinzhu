package com.github.binarywang.demo.wx.mp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

//@Configuration
public class ResourceConfig {
    @Bean
    public ResourceLoader createResourceLoader() {
        return new DefaultResourceLoader();
    }
}
