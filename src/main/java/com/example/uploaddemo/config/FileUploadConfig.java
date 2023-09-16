package com.example.uploaddemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.io.IOException;

@Configuration
@ConfigurationProperties(prefix = "file")
public class FileUploadConfig {

//    private String tempDir;

    @Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver() throws IOException {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setDefaultEncoding("UTF-8");
        multipartResolver.setMaxUploadSizePerFile(30 * 1024 * 1024); // 设置文件上传大小限制
        multipartResolver.setResolveLazily(true);
//        multipartResolver.setUploadTempDir(new FileSystemResource(tempDir));
        return multipartResolver;
    }

//    public void setTempDir(String tempDir) {
//        this.tempDir = tempDir;
//    }
}