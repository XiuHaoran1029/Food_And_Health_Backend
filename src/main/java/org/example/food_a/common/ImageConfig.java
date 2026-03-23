package org.example.food_a.common;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ImageConfig implements WebMvcConfigurer {

    @Value("${image.path1}")
    private String path1;

    @Value("${image.path2}")
    private String path2;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("file:" + path1);

        registry.addResourceHandler("/image/**")
                .addResourceLocations("file:" + path2);
    }
}