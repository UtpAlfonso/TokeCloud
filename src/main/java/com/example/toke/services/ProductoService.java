package com.example.toke.services;

import com.example.toke.dto.ProductoDetalleDTO;
import com.example.toke.dto.ProductoResumenDTO;
import com.example.toke.entities.Inventario;
import com.example.toke.entities.Producto;
import com.example.toke.entities.Usuario;
import com.example.toke.exception.ProductoNoEncontradoException;
import com.example.toke.repositories.PedidoRepository;
import com.example.toke.repositories.ProductoRepository;
import com.example.toke.repositories.ResenaRepository;
import com.example.toke.repositories.UsuarioRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ResenaService resenaService;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final ResenaRepository resenaRepository; // Reutilizamos el servicio de reseñas

   @Autowired
    public ProductoService(ProductoRepository productoRepository, 
                         ResenaService resenaService, 
                         UsuarioRepository usuarioRepository, 
                         PedidoRepository pedidoRepository, 
                         ResenaRepository resenaRepository) {
        this.productoRepository = productoRepository;
        this.resenaService = resenaService;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.resenaRepository = resenaRepository;
    }
    @Transactional(readOnly = true)
    public List<ProductoResumenDTO> obtenerTodosLosProductos() {
        return productoRepository.findAll().stream()
                .map(this::mapToProductoResumenDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductoDetalleDTO obtenerProductoPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado con ID: " + id));
        
        // Obtenemos el usuario actual (si existe) del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        return mapToProductoDetalleDTO(producto, authentication);
    }
    // Mapeador de Entidad a DTO de Resumen
    private ProductoResumenDTO mapToProductoResumenDTO(Producto producto) {
        ProductoResumenDTO dto = new ProductoResumenDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setPrecio(producto.getPrecio());
        dto.setUrlImagen(producto.getUrlImagen());
        if (producto.getCategoria() != null) {
            dto.setNombreCategoria(producto.getCategoria().getNombre());
        }
        return dto;
    }

    // Mapeador de Entidad a DTO de Detalle
    private ProductoDetalleDTO mapToProductoDetalleDTO(Producto producto, Authentication authentication) {
        ProductoDetalleDTO dto = new ProductoDetalleDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setUrlImagen(producto.getUrlImagen());
        dto.setFechaCreacion(producto.getFechaCreacion());
        if (producto.getCategoria() != null) {
            dto.setNombreCategoria(producto.getCategoria().getNombre());
        }

        Map<String, Integer> tallasDisponibles = producto.getInventario().stream()
                .collect(Collectors.toMap(
                        inventario -> inventario.getTalla().getNombre(),
                        Inventario::getStock
                ));
        dto.setTallasDisponibles(tallasDisponibles);

        dto.setReseñas(resenaService.obtenerResenasPorProducto(producto.getId()));
        
        // --- INICIO DE LA NUEVA LÓGICA DE VALIDACIÓN DE RESEÑAS ---
      boolean puedeResenar = false;
System.out.println("--- Depurando Lógica de Reseñas ---"); // LOG 1

if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
    String email = authentication.getName();
    System.out.println("Usuario autenticado: " + email); // LOG 2

    Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
    
    if (usuario != null) {
        System.out.println("Usuario encontrado en BD. ID: " + usuario.getId()); // LOG 3

        boolean haComprado = pedidoRepository.countByUsuarioIdAndProductoId(usuario.getId(), producto.getId()) > 0;
        System.out.println("¿Ha comprado el producto? -> " + haComprado); // LOG 4

        boolean yaReseno = resenaRepository.existsByUsuarioIdAndProductoId(usuario.getId(), producto.getId());
        System.out.println("¿Ya ha dejado una reseña? -> " + yaReseno); // LOG 5
        
        puedeResenar = haComprado && !yaReseno;
    }
} else {
    System.out.println("Usuario no está autenticado."); // LOG 6
}

System.out.println("Resultado final de puedeDejarResena: " + puedeResenar); // LOG 7
System.out.println("------------------------------------");
dto.setPuedeDejarResena(puedeResenar);
        // --- FIN DE LA NUEVA LÓGICA ---
        
        return dto;
    }
}
