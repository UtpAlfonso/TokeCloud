package com.example.toke.controllers;

import com.example.toke.dto.ProductoAdminDTO;
import com.example.toke.entities.Inventario;
import com.example.toke.entities.Producto;
import com.example.toke.entities.enums.EstadoPedido;
import com.example.toke.repositories.CategoriaRepository;
import com.example.toke.repositories.ProductoRepository;
import com.example.toke.repositories.TallaRepository;
import com.example.toke.services.AdminPedidoService;
import com.example.toke.services.AdminProductoService;
import com.example.toke.services.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DashboardService dashboardService;
    private final AdminProductoService adminProductoService;
    private final AdminPedidoService adminPedidoService;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final TallaRepository tallaRepository;

    @Autowired
    public AdminController(DashboardService dashboardService,
                           AdminProductoService adminProductoService,
                           AdminPedidoService adminPedidoService,
                           ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository,
                           TallaRepository tallaRepository) {
        this.dashboardService = dashboardService;
        this.adminProductoService = adminProductoService;
        this.adminPedidoService = adminPedidoService;
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.tallaRepository = tallaRepository;
    }

    // --- SECCIÓN DE DASHBOARD ---

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model, HttpServletRequest request) {
        model.addAttribute("estadisticas", dashboardService.obtenerEstadisticas());
        model.addAttribute("requestURI", request.getRequestURI());
        return "admin/dashboard";
    }

    // --- SECCIÓN DE GESTIÓN DE PRODUCTOS ---

    @GetMapping("/productos")
    public String listarProductos(Model model, HttpServletRequest request) {
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("requestURI", request.getRequestURI());
        return "admin/productos/lista";
    }

    @GetMapping("/productos/nuevo")
    public String mostrarFormularioNuevoProducto(Model model, HttpServletRequest request) {
        model.addAttribute("producto", new ProductoAdminDTO());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("todasLasTallas", tallaRepository.findAll());
        model.addAttribute("pageTitle", "Nuevo Producto");
        model.addAttribute("requestURI", request.getRequestURI());
        return "admin/productos/formulario";
    }

    @GetMapping("/productos/editar/{id}")
    public String mostrarFormularioEditarProducto(@PathVariable Long id, Model model, HttpServletRequest request) {
        Producto producto = productoRepository.findById(id).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        ProductoAdminDTO productoDTO = new ProductoAdminDTO();
        productoDTO.setId(producto.getId());
        productoDTO.setNombre(producto.getNombre());
        productoDTO.setDescripcion(producto.getDescripcion());
        productoDTO.setPrecio(producto.getPrecio());
        if (producto.getCategoria() != null) {
            productoDTO.setCategoriaId(producto.getCategoria().getId());
        }
        productoDTO.setUrlImagenActual(producto.getUrlImagen());
        
        Map<Long, Integer> stockActual = producto.getInventario().stream()
                .collect(Collectors.toMap(inv -> inv.getTalla().getId(), Inventario::getStock));
        productoDTO.setInventario(stockActual);
        
        model.addAttribute("producto", productoDTO);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("todasLasTallas", tallaRepository.findAll());
        model.addAttribute("pageTitle", "Editar Producto");
        model.addAttribute("requestURI", request.getRequestURI());
        return "admin/productos/formulario";
    }

    @PostMapping("/productos/guardar")
    public String guardarProducto(@Valid @ModelAttribute("producto") ProductoAdminDTO productoDTO,
                                 BindingResult result,
                                 Model model,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaRepository.findAll());
            model.addAttribute("todasLasTallas", tallaRepository.findAll());
            model.addAttribute("pageTitle", (productoDTO.getId() == null) ? "Nuevo Producto" : "Editar Producto");
            model.addAttribute("requestURI", request.getRequestURI());
            return "admin/productos/formulario";
        }

        adminProductoService.guardarProducto(productoDTO);
        redirectAttributes.addFlashAttribute("mensaje", "Producto guardado con éxito.");
        return "redirect:/admin/productos";
    }
    
    @PostMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminProductoService.eliminarProducto(id);
        redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado con éxito.");
        return "redirect:/admin/productos";
    }

    // --- SECCIÓN DE GESTIÓN DE PEDIDOS ---

    @GetMapping("/pedidos")
    public String listarPedidos(Model model, HttpServletRequest request) {
        model.addAttribute("pedidos", adminPedidoService.listarTodosLosPedidos());
        model.addAttribute("requestURI", request.getRequestURI());
        return "admin/pedidos/lista";
    }

    @GetMapping("/pedidos/{id}")
    public String verDetallePedido(@PathVariable Long id, Model model, HttpServletRequest request) {
        model.addAttribute("pedido", adminPedidoService.obtenerDetallePedido(id));
        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("requestURI", request.getRequestURI());
        return "admin/pedidos/detalle";
    }

    @PostMapping("/pedidos/actualizar-estado")
    public String actualizarEstadoPedido(@RequestParam Long pedidoId,
                                       @RequestParam EstadoPedido estado,
                                       RedirectAttributes redirectAttributes) {
        adminPedidoService.actualizarEstadoPedido(pedidoId, estado);
        redirectAttributes.addFlashAttribute("mensaje", "Estado del pedido actualizado con éxito.");
        return "redirect:/admin/pedidos/" + pedidoId;
    }

    @PostMapping("/pedidos/eliminar/{id}")
    public String eliminarPedido(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminPedidoService.eliminarPedido(id);
        redirectAttributes.addFlashAttribute("mensaje", "Pedido eliminado con éxito.");
        return "redirect:/admin/pedidos";
    }
}