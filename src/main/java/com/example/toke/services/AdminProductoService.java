package com.example.toke.services;

import com.example.toke.dto.ProductoAdminDTO;
import com.example.toke.entities.Categoria;
import com.example.toke.entities.Inventario;
import com.example.toke.entities.Producto;
import com.example.toke.entities.Talla;
import com.example.toke.repositories.CategoriaRepository;
import com.example.toke.repositories.InventarioRepository;
import com.example.toke.repositories.ProductoRepository;
import com.example.toke.repositories.TallaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class AdminProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final StorageService storageService;
    private final InventarioRepository inventarioRepository;
    private final TallaRepository tallaRepository;

    @Autowired
    public AdminProductoService(ProductoRepository productoRepository,
                                CategoriaRepository categoriaRepository,
                                StorageService storageService,
                                InventarioRepository inventarioRepository,
                                TallaRepository tallaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.storageService = storageService;
        this.inventarioRepository = inventarioRepository;
        this.tallaRepository = tallaRepository;
    }

    @Transactional
    public void guardarProducto(ProductoAdminDTO productoDTO) {
        // 1. Crear o actualizar la entidad Producto
        Producto producto;
        if (productoDTO.getId() != null) {
            producto = productoRepository.findById(productoDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoDTO.getId()));
        } else {
            producto = new Producto();
        }

        producto.setNombre(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setPrecio(productoDTO.getPrecio());

        Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + productoDTO.getCategoriaId()));
        producto.setCategoria(categoria);

        MultipartFile imagen = productoDTO.getImagen();
        if (imagen != null && !imagen.isEmpty()) {
            String imageUrl = storageService.store(imagen);
            producto.setUrlImagen(imageUrl);
        } else if (producto.getId() == null) {
            // Asigna una URL por defecto para productos nuevos sin imagen
            producto.setUrlImagen("/images/default-product.png"); // Asegúrate de tener esta imagen en static/images
        }

        // Guardamos el producto para asegurarnos de que tenga un ID antes de manejar el inventario
        Producto productoGuardado = productoRepository.save(producto);

        // 2. Procesar y guardar el inventario por tallas
        if (productoDTO.getInventario() != null) {
            for (Map.Entry<Long, Integer> entry : productoDTO.getInventario().entrySet()) {
                Long tallaId = entry.getKey();
                Integer stock = entry.getValue();

                // Si no se introduce un valor, lo tratamos como 0 para evitar nulos.
                if (stock == null) {
                    stock = 0;
                }

                // Busca si ya existe una entrada de inventario para este producto y talla
                Inventario inventario = inventarioRepository
                        .findByProductoIdAndTallaId(productoGuardado.getId(), tallaId)
                        .orElse(new Inventario()); // Si no existe, crea una nueva instancia

                // Si es una nueva entrada, debemos establecer las relaciones
                if (inventario.getId() == null) {
                    Talla talla = tallaRepository.findById(tallaId)
                            .orElseThrow(() -> new RuntimeException("Talla no encontrada con ID: " + tallaId));
                    inventario.setProducto(productoGuardado);
                    inventario.setTalla(talla);
                }

                // Actualiza la cantidad de stock y guarda la entidad Inventario
                inventario.setStock(stock);
                inventarioRepository.save(inventario);
            }
        }
    }
    
    @Transactional
    public void eliminarProducto(Long id) {
        // En un futuro, aquí podrías añadir lógica para eliminar la imagen de Cloudinary
        productoRepository.deleteById(id);
    }
}