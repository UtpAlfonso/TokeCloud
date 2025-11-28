package com.example.toke.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class StorageService {

    private final Cloudinary cloudinary;

    @Autowired
    public StorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Sube un archivo a Cloudinary y devuelve su URL segura.
     * @param file El archivo MultipartFile recibido del formulario.
     * @return La URL HTTPS del archivo subido.
     */
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("No se puede subir un archivo vacío.");
        }

        try {
            // Sube los bytes del archivo a Cloudinary.
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), 
                ObjectUtils.asMap(
                    // Opcional: Asigna un nombre de archivo público único para evitar colisiones
                    // y facilitar la gestión. Si no, Cloudinary asigna uno aleatorio.
                    "public_id", UUID.randomUUID().toString(),
                    
                    // Importante: Organiza las imágenes en una carpeta dentro de Cloudinary.
                    "folder", "toke-inka/products" 
                ));
            
            // El resultado de la subida contiene mucha información.
            // Nos interesa la 'secure_url', que es la URL con HTTPS.
            return (String) uploadResult.get("secure_url");
            
        } catch (IOException e) {
            throw new RuntimeException("Fallo al subir el archivo a Cloudinary.", e);
        }
    }
    
    /**
     * (Opcional pero recomendado) Elimina una imagen de Cloudinary.
     * Necesitarías el 'public_id' de la imagen para esto.
     */
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Fallo al eliminar el archivo de Cloudinary.", e);
        }
    }
}