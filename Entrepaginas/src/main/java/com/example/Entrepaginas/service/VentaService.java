package com.example.Entrepaginas.service;


import com.example.Entrepaginas.model.Venta;
import com.example.Entrepaginas.model.DetalleVenta;
import com.example.Entrepaginas.model.Libro;
import com.example.Entrepaginas.repository.VentaRepository;
import com.example.Entrepaginas.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private LibroRepository libroRepository;

    public List<Venta> obtenerVentasPorCliente(Long clienteId) {
    return ventaRepository.findByClienteId(clienteId);
}
    
    // Asumimos que también tienes ClienteRepository y UsuarioRepository inyectados si son necesarios
    // para buscar entidades completas en otros métodos del service.

    // ====================================================================
    // MÉTODOS BÁSICOS
    // ====================================================================

    @Transactional(readOnly = true) 
    public List<Venta> listarTodasLasVentas() {
        List<Venta> ventas = ventaRepository.findAllWithClienteAndUsuario();
        ventas.forEach(venta -> {
            venta.getDetallesVenta().size(); 
            venta.getDetallesVenta().forEach(detalle -> {
                detalle.getLibro().getTitulo(); 
            });
        });
        return ventas; 
    }

    public Venta buscarVentaPorId(Long id) {
        return ventaRepository.findByIdWithDetalles(id).orElse(null);
    }
    
    // ====================================================================
    // CREAR VENTA (La lógica más simple de Stock)
    // ====================================================================

    /**
     * Guarda la nueva venta y DISMINUYE el stock de cada libro vendido.
     * @param venta Objeto Venta con la lista de DetallesVenta.
     * @return Venta guardada.
     * @throws Exception Si el stock es insuficiente o hay un error de guardado.
     */
    @Transactional // CRUCIAL: Si falla la actualización de un libro, toda la operación se revierte.
    public Venta guardarVentaYActualizarStock(Venta venta) throws Exception {
        
        // 1. Asignar fecha y preparar totales (si el controlador no lo hizo)
        if (venta.getFechaVenta() == null) {
             venta.setFechaVenta(LocalDateTime.now());
        }
        double totalVenta = 0.0;

        // 2. Iterar sobre los detalles para validar stock y calcular total
        for (DetalleVenta detalle : venta.getDetallesVenta()) {
            
            // a. Buscar el libro en la DB (para obtener el stock real)
            Libro libro = libroRepository.findById(detalle.getLibro().getId())
                            .orElseThrow(() -> new Exception("Libro no encontrado con ID: " + detalle.getLibro().getId()));
            
            // b. Validación de Stock
            if (libro.getStock() < detalle.getCantidad()) {
                throw new Exception("Stock insuficiente para el libro: " + libro.getTitulo());
            }

            // c. Actualizar stock
            libro.setStock(libro.getStock() - detalle.getCantidad());
            libroRepository.save(libro);

            // d. Vincular el objeto Libro completo al detalle (buena práctica)
            detalle.setLibro(libro); 
            
            // e. Vincular el detalle a esta venta y calcular subtotal
            detalle.setVenta(venta);
            totalVenta += detalle.getSubtotal();
        }

        // 3. Establecer el total y guardar
        venta.setTotal(totalVenta);
        return ventaRepository.save(venta);
    }

    // ====================================================================
    // ANULAR VENTA (Lógica de Reposición de Stock)
    // ====================================================================
    
    /**
     * Marca la venta como anulada y REPONE el stock de todos los libros.
     * @param idVenta ID de la venta a anular.
     * @throws Exception Si la venta no existe o ya está anulada.
     */
    @Transactional // Si falla la reposición de stock, la anulación se revierte.
    public void anularVentaYReponerStock(Long idVenta) throws Exception {
        Venta venta = ventaRepository.findById(idVenta)
                            .orElseThrow(() -> new Exception("Venta no encontrada."));

        if (venta.isAnulada()) {
            throw new Exception("La venta N° " + idVenta + " ya se encuentra anulada.");
        }

        // 1. Reponer Stock por cada detalle
        for (DetalleVenta detalle : venta.getDetallesVenta()) {
            Libro libro = detalle.getLibro();
            // Asegúrate de que el libro existe antes de reponer
            Libro libroOriginal = libroRepository.findById(libro.getId())
                                    .orElseThrow(() -> new Exception("Libro de detalle no encontrado."));
            
            libroOriginal.setStock(libroOriginal.getStock() + detalle.getCantidad());
            libroRepository.save(libroOriginal);
        }

        // 2. Marcar la venta como anulada
        venta.setAnulada(true);
        ventaRepository.save(venta);
    }

    // ====================================================================
    // EDITAR VENTA (Lógica de Comparación y Ajuste de Stock)
    // ====================================================================

    /**
     * Actualiza una venta comparando los detalles viejos con los nuevos para ajustar el stock.
     * @param ventaActualizada Objeto Venta con los nuevos detalles.
     * @return Venta actualizada.
     * @throws Exception Si hay problemas de stock o datos.
     */
    @Transactional
    public Venta actualizarVenta(Venta ventaActualizada) throws Exception {
        
        // 1. Obtener la venta original de la base de datos
        Venta ventaOriginal = ventaRepository.findById(ventaActualizada.getId())
                            .orElseThrow(() -> new Exception("Venta a editar no encontrada."));

        if (ventaOriginal.isAnulada()) {
            throw new Exception("No se puede editar una venta anulada.");
        }

        // --- PREPARACIÓN DE MAPAS PARA LA COMPARACIÓN ---
        
        // Mapa: [Libro ID] -> Cantidad Vendida Originalmente
        Map<Long, Integer> stockOriginal = new HashMap<>();
        for (DetalleVenta detalle : ventaOriginal.getDetallesVenta()) {
            stockOriginal.put(detalle.getLibro().getId(), detalle.getCantidad());
        }

        // Mapa: [Libro ID] -> Cantidad Vendida en la Edición
        Map<Long, Integer> stockNuevo = new HashMap<>();
        double nuevoTotal = 0.0;

        // 2. Procesar los nuevos detalles y validar stock
        for (DetalleVenta detalleNuevo : ventaActualizada.getDetallesVenta()) {
            Long libroId = detalleNuevo.getLibro().getId();
            Libro libroDB = libroRepository.findById(libroId)
                                .orElseThrow(() -> new Exception("Libro no encontrado: " + libroId));
            
            int cantidadNueva = detalleNuevo.getCantidad();
            int cantidadOriginal = stockOriginal.getOrDefault(libroId, 0); // 0 si el libro es nuevo
            
            int diferenciaStock = cantidadNueva - cantidadOriginal; 
            
            // 3. Aplicar ajuste de stock
            if (diferenciaStock != 0) {
                // Validación: Si la diferencia es positiva, se está vendiendo más, verificar stock disponible
                if (diferenciaStock > 0) {
                    // Stock disponible = stock actual - lo que ya estaba vendido en esta venta
                    int stockDisponibleReal = libroDB.getStock() + cantidadOriginal; 
                    
                    if (stockDisponibleReal < cantidadNueva) {
                        throw new Exception("Stock insuficiente para aumentar la venta del libro: " + libroDB.getTitulo());
                    }
                }
                
                // Aplicar la diferencia (positivo = vender más, negativo = devolver)
                libroDB.setStock(libroDB.getStock() - diferenciaStock);
                libroRepository.save(libroDB);
            }
            
            // 4. Calcular nuevo total y actualizar mapas
            nuevoTotal += detalleNuevo.getSubtotal();
            stockNuevo.put(libroId, cantidadNueva);
        }

        // 5. Reponer Stock de libros ELIMINADOS
        for (Map.Entry<Long, Integer> entry : stockOriginal.entrySet()) {
            Long libroId = entry.getKey();
            int cantidadOriginal = entry.getValue();

            // Si el libro estaba en la venta original pero NO está en la nueva lista
            if (!stockNuevo.containsKey(libroId)) {
                Libro libroEliminado = libroRepository.findById(libroId).get();
                libroEliminado.setStock(libroEliminado.getStock() + cantidadOriginal);
                libroRepository.save(libroEliminado);
            }
        }
        
        // 6. Actualizar la Venta Original con los nuevos datos
        ventaOriginal.setCliente(ventaActualizada.getCliente());
        ventaOriginal.setTotal(nuevoTotal);
        
        // CRUCIAL: Reemplazar los detalles. La opción orphanRemoval=true se encargará de eliminar 
        // los detalles viejos que ya no existen en la base de datos.
        ventaOriginal.setDetallesVenta(ventaActualizada.getDetallesVenta()); 
        
        // 7. Guardar la venta actualizada
        return ventaRepository.save(ventaOriginal);
    }

}