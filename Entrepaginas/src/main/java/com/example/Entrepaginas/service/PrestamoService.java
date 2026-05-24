package com.example.Entrepaginas.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Entrepaginas.model.Prestamo;
import com.example.Entrepaginas.model.Libro;
import com.example.Entrepaginas.repository.PrestamoRepository;
import com.example.Entrepaginas.repository.LibroRepository;

@Service
public class PrestamoService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private LibroRepository libroRepository;

    public List<Prestamo> obtenerTodos() {
        return prestamoRepository.findAll();
    }

    public List<Prestamo> obtenerPrestamosPorCliente(Long clienteId) {
    return prestamoRepository.findByClienteId(clienteId);
}

    public Prestamo obtenerPorId(Long id) {
        return prestamoRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        prestamoRepository.deleteById(id);
    }

    public long contarTodosLosPrestamos() {
        return prestamoRepository.count();
    }

    public long contarPrestamosVencidos() {
        return prestamoRepository.countByFechaDevolucionBeforeAndActivoTrue(LocalDate.now());
    }

    public long contarPrestamos() {
        return prestamoRepository.count();
    }

    public long contarPrestamosActivos() {
        return prestamoRepository.countByActivoTrue();
    }

    public List<Prestamo> obtenerUltimosPrestamos(int cantidad) {
        return prestamoRepository.findAll()
                .stream()
                .sorted((p1, p2) -> p2.getFechaPrestamo().compareTo(p1.getFechaPrestamo()))
                .limit(cantidad)
                .toList();
    }

    @Transactional
    public Prestamo guardarPrestamoYDescontarStock(Prestamo prestamo) throws Exception {
        Libro libro = libroRepository.findById(prestamo.getLibro().getId())
                .orElseThrow(() -> new Exception("Libro no encontrado con ID: " + prestamo.getLibro().getId()));

        if (libro.getStock() < 1) {
            throw new Exception("No hay stock disponible para el libro: " + libro.getTitulo());
        }
        libro.setStock(libro.getStock() - 1);
        libroRepository.save(libro);
        if (prestamo.getFechaPrestamo() == null) {
            prestamo.setFechaPrestamo(LocalDate.now());
        }
        prestamo.setActivo(true);
        prestamo.setLibro(libro);
        return prestamoRepository.save(prestamo);
    }

    @Transactional
    public void devolverPrestamoYRestaurarStock(Long idPrestamo) throws Exception {
        Prestamo prestamo = prestamoRepository.findById(idPrestamo)
                .orElseThrow(() -> new Exception("Préstamo no encontrado."));

        if (!prestamo.isActivo()) {
            throw new Exception("El préstamo N° " + idPrestamo + " ya fue devuelto.");
        }
        Libro libro = libroRepository.findById(prestamo.getLibro().getId())
                .orElseThrow(() -> new Exception("Libro del préstamo no encontrado."));

        libro.setStock(libro.getStock() + 1);
        libroRepository.save(libro);

        prestamo.setActivo(false);
        prestamoRepository.save(prestamo);
    }

    @Deprecated
    public Prestamo guardar(Prestamo prestamo) {
        return prestamoRepository.save(prestamo);
    }
}
