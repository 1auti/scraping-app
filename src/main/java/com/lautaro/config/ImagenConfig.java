package com.lautaro.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ImagenConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        WebMvcConfigurer.super.addResourceHandlers(registry);

        // Configurar varias carpetas para recursos estáticos
        registry.addResourceHandler("/imagenes-productos-jumbo/**")
                .addResourceLocations("file:C:/Users/Lautaro/IdeaProjects/scraping-app/imagenes-productos-jumbo/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/imagenes-productos-dia/**")
                .addResourceLocations("file:C:/Users/Lautaro/IdeaProjects/scraping-app/imagenes-productos-dia/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/imagenes-productos-coto/**")
                .addResourceLocations("file:C:/Users/Lautaro/IdeaProjects/scraping-app/imagenes-productos-coto/")
                .setCachePeriod(3600);
    }
}
