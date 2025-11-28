package com.example.toke.config;

import com.example.toke.entities.*;
import com.example.toke.entities.enums.RolUsuario;
import com.example.toke.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    // Repositorios existentes
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Nuevos repositorios para los productos
    private final CategoriaRepository categoriaRepository;
    private final TallaRepository tallaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;

    @Autowired
    public DataLoader(UsuarioRepository usuarioRepository,
                      PasswordEncoder passwordEncoder,
                      CategoriaRepository categoriaRepository,
                      TallaRepository tallaRepository,
                      ProductoRepository productoRepository,
                      InventarioRepository inventarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoriaRepository = categoriaRepository;
        this.tallaRepository = tallaRepository;
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        crearAdminSiNoExiste();
    }

    private void crearAdminSiNoExiste() {
        if (!usuarioRepository.existsByRol(RolUsuario.ROLE_ADMIN)) {
            System.out.println("Creando administrador por defecto...");
            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setApellido("TokeInca");
            admin.setEmail("admin@tokeinca.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol(RolUsuario.ROLE_ADMIN);
            usuarioRepository.save(admin);
            System.out.println("Administrador creado: admin@tokeinca.com / admin123");
        }
    }
    /**
     * Método de ayuda para crear una entrada de inventario.
     */
    private void crearInventario(Producto producto, Talla talla, int stock) {
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setTalla(talla);
        inventario.setStock(stock);
        inventarioRepository.save(inventario);
    }
}