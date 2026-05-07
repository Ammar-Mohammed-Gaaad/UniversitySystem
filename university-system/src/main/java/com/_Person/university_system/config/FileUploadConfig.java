package com._Person.university_system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    // المجلد اللي هنحفظ فيه الملفات المرفوعة
    public static final String UPLOAD_DIR = "uploads/";

    // نخلي Spring يقدر يخدم الملفات المرفوعة كـ static resources
    // يعني لما الزائر يطلب http://localhost:8081/uploads/file.pdf هيلاقيه
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + UPLOAD_DIR);
    }
}
