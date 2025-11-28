package com.example.toke.repositories;

import com.example.toke.entities.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Busca todos los pedidos de un usuario específico, ordenados por fecha descendente
    // Ideal para el historial de pedidos del cliente.
    List<Pedido> findByUsuarioIdOrderByFechaPedidoDesc(Long idUsuario);
    List<Pedido> findAllByOrderByFechaPedidoDesc();

     @Query("SELECT COUNT(p) FROM Pedido p JOIN p.detalles d WHERE p.usuario.id = :usuarioId AND d.producto.id = :productoId AND p.estado IN ('PAGADO', 'ENVIADO', 'ENTREGADO')")
    int countByUsuarioIdAndProductoId(@Param("usuarioId") Long usuarioId, @Param("productoId") Long productoId);
}