package com.example.Entrepaginas.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private FileUploadConfig fileUploadConfig;

    @Autowired
    private SessionInterceptor sessionInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Para libros
        registry.addResourceHandler("/uploads/libros/**")
                .addResourceLocations("file:" + fileUploadConfig.getUploadDirLibros());

        // Para perfiles (MODIFICADO para mayor robustez en Windows con file:///)
        String perfilesUploadDir = fileUploadConfig.getUploadDirPerfiles();

        // Normalizar la ruta y asegurar que termina con un separador de directorio
        Path perfilesPath = Paths.get(perfilesUploadDir).toAbsolutePath().normalize();
        String perfilesLocation = "file:///" + perfilesPath.toString().replace("\\", "/");
        if (!perfilesLocation.endsWith("/")) {
             perfilesLocation += "/";
        }

        System.out.println("DEBUG WebConfig: Mapeando /uploads/perfiles/** a la ubicación: " + perfilesLocation);

        registry.addResourceHandler("/uploads/perfiles/**")
                .addResourceLocations(perfilesLocation);

        // Para recursos estáticos generales (CSS, JS, imágenes por defecto)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }


   @Override
   public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/**")          // protege todo
                .excludePathPatterns(
                        "/",
                        "/entrepaginas",    // ← agregar
                        "/entrepaginas/",   // ← agregar
                        "/catalogo",
                        "/catalogo/**",
                        "/login",
                        "/registro",
                        "/api/**",
                        "/acceder",
                        "/logout",
                        "/registro",
                        "/static/**",
                        "/uploads/**",
                        "/images/**"
                        );
        }
}
