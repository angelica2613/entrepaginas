package com.example.Entrepaginas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import com.example.Entrepaginas.model.Venta;
import java.util.List;

// La anotación @Repository es opcional en las interfaces que extienden de JpaRepository,
// pero es buena práctica incluirla.
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    
    // Spring Data JPA ya proporciona:
    // - findAll() para listar todas las ventas.
    // - findById(Long id) para buscar una venta por ID.
    // - save(Venta venta) para guardar o actualizar una venta.
    // - delete(Venta venta) para eliminar una venta (físicamente).

    // Puedes agregar consultas personalizadas aquí si las necesitas. Por ejemplo:
    
    // List<Venta> findByFechaVentaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    // List<Venta> findByAnuladaFalseOrderByFechaVentaDesc();

    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.detallesVenta dv LEFT JOIN FETCH dv.libro LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.usuario WHERE v.id = :id")
    Optional<Venta> findByIdWithDetalles(@Param("id") Long id);

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.usuario")
    List<Venta> findAllWithClienteAndUsuario();
    List<Venta> findByClienteId(Long clienteId);
}