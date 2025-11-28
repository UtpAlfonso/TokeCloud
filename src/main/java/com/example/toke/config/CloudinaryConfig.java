package com.example.toke.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    /**
     * Crea un bean de Cloudinary.
     * La librería de Cloudinary es lo suficientemente inteligente como para buscar
     * automáticamente la variable de entorno "CLOUDINARY_URL" e inicializarse
     * con ella. No necesitamos leerla manualmente con @Value.
     * En tu PC local, puedes crear esta variable de entorno para probar.
     */
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary();
    }
}
