package com.example.toke.services;

import com.example.toke.dto.ProductoAdminDTO;
import com.example.toke.entities.Categoria;
import com.example.toke.entities.Producto;
import com.example.toke.repositories.CategoriaRepository;
import com.example.toke.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AdminProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final StorageService storageService;

    @Autowired
    public AdminProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository, StorageService storageService) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.storageService = storageService;
    }

   @Transactional
public void guardarProducto(ProductoAdminDTO productoDTO) {
    Producto producto;
    if (productoDTO.getId() != null) {
        producto = productoRepository.findById(productoDTO.getId()).orElseThrow(
            () -> new RuntimeException("Producto no encontrado")
        );
    } else {
        producto = new Producto();
    }

    producto.setNombre(productoDTO.getNombre());
    producto.setDescripcion(productoDTO.getDescripcion());
    producto.setPrecio(productoDTO.getPrecio());

    Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId()).orElseThrow(
        () -> new RuntimeException("Categoría no encontrada")
    );
    producto.setCategoria(categoria);

    MultipartFile imagen = productoDTO.getImagen();
    
    // --- ESTA ES LA LÓGICA MODIFICADA ---
    if (imagen != null && !imagen.isEmpty()) {
        // 1. Llama al StorageService, que ahora sube a Cloudinary.
        String imageUrl = storageService.store(imagen);
        
        // 2. Guarda la URL completa devuelta por Cloudinary en la base de datos.
        producto.setUrlImagen(imageUrl);
        
        // (Opcional: si estás editando y reemplazando una imagen, aquí deberías
        // añadir la lógica para borrar la imagen antigua de Cloudinary).
    } else if (producto.getId() == null) {
        // Si es un producto nuevo sin imagen, puedes asignar una URL por defecto.
        // Puede ser una imagen que subas a Cloudinary y cuya URL copies aquí.
        producto.setUrlImagen("https://res.cloudinary.com/tu-cloud-name/image/upload/v12345/toke-inka/default-product.png");
    }

    productoRepository.save(producto);
}
    
    public void eliminarProducto(Long id) {
        // Aquí también deberías añadir lógica para eliminar la imagen del sistema de archivos.
        productoRepository.deleteById(id);
    }
}