package com.example.toke.services;

import com.example.toke.dto.ResenaDTO;
import com.example.toke.entities.Producto;
import com.example.toke.entities.Resena;
import com.example.toke.entities.Usuario;
import com.example.toke.exception.ProductoNoEncontradoException;
import com.example.toke.repositories.PedidoRepository;
import com.example.toke.repositories.ProductoRepository;
import org.springframework.security.access.AccessDeniedException;
import com.example.toke.repositories.ResenaRepository;
import com.example.toke.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository; // <-- NUEVA INYECCIÓN

    @Autowired
    public ResenaService(ResenaRepository resenaRepository, ProductoRepository productoRepository, UsuarioRepository usuarioRepository, PedidoRepository pedidoRepository) {
        this.resenaRepository = resenaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public ResenaDTO crearResena(ResenaDTO resenaDTO, String userEmail) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        Producto producto = productoRepository.findById(resenaDTO.getProductoId())
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado para la reseña."));
        
        // --- VALIDACIÓN CLAVE ---
        // Verifica si el usuario ha comprado este producto.
        if (pedidoRepository.countByUsuarioIdAndProductoId(usuario.getId(), producto.getId()) == 0) {
            throw new AccessDeniedException("No puedes dejar una reseña de un producto que no has comprado.");
        }
        
        // Verifica si el usuario ya ha dejado una reseña para este producto.
        if (resenaRepository.existsByUsuarioIdAndProductoId(usuario.getId(), producto.getId())) {
             throw new IllegalStateException("Ya has dejado una reseña para este producto.");
        }

        Resena nuevaResena = new Resena();
        nuevaResena.setProducto(producto);
        nuevaResena.setUsuario(usuario);
        nuevaResena.setCalificacion(resenaDTO.getCalificacion());
        nuevaResena.setComentario(resenaDTO.getComentario());

        Resena guardada = resenaRepository.save(nuevaResena);
        return mapToResenaDTO(guardada);
    }

    @Transactional(readOnly = true)
    public List<ResenaDTO> obtenerResenasPorProducto(Long productoId) {
        return resenaRepository.findByProductoId(productoId).stream()
                .map(this::mapToResenaDTO)
                .collect(Collectors.toList());
    }
    
    private ResenaDTO mapToResenaDTO(Resena resena) {
        ResenaDTO dto = new ResenaDTO();
        dto.setId(resena.getId());
        dto.setCalificacion(resena.getCalificacion());
        dto.setComentario(resena.getComentario());
        dto.setFechaResena(resena.getFechaResena());
        dto.setProductoId(resena.getProducto().getId());
        if (resena.getUsuario() != null) {
            dto.setNombreUsuario(resena.getUsuario().getNombre() + " " + resena.getUsuario().getApellido().charAt(0) + ".");
        }
        return dto;
    }
}
