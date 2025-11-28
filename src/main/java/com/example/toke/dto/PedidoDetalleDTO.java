package com.example.toke.dto;

import com.example.toke.entities.enums.EstadoPedido;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List; // Asegúrate de importar java.util.List

@Data
public class PedidoDetalleDTO {
    private Long id;
    private LocalDateTime fechaPedido;
    private EstadoPedido estado;
    private BigDecimal total;
    private String nombreCliente;
    private String emailCliente;

    // Dirección de envío
    private String direccionEnvio;
    // No necesitamos ciudad y país si no los estamos usando.
    // private String ciudadEnvio;
    // private String paisEnvio;

    // CORRECCIÓN: La lista debe ser de 'DetallePedidoDTO' (singular)
    private List<DetallePedidoDTO> detalles;
}