package com.example.Entrepaginas.controller.api;

import com.example.Entrepaginas.model.Libro;
import com.example.Entrepaginas.repository.LibroRepository;
import com.example.Entrepaginas.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
public class LibroApiController {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    // Todos los libros disponibles
    @GetMapping
    public List<Libro> obtenerTodos() {
        return libroRepository.findAll()
                .stream()
                .filter(Libro::isDisponible)
                .toList();
    }

    // Más populares (más préstamos)
    @GetMapping("/populares")
    public List<Libro> masPopulares() {
        return libroRepository.findAll()
                .stream()
                .filter(Libro::isDisponible)
                .sorted((a, b) -> {
                    long prestamosA = prestamoRepository.countByLibroId(a.getId());
                    long prestamosB = prestamoRepository.countByLibroId(b.getId());
                    return Long.compare(prestamosB, prestamosA);
                })
                .limit(8)
                .toList();
    }

    // Más recientes (por ID descendente)
    @GetMapping("/recientes")
    public List<Libro> masRecientes() {
        return libroRepository.findAll()
                .stream()
                .filter(Libro::isDisponible)
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .limit(8)
                .toList();
    }

    // Por género
    @GetMapping("/genero/{genero}")
    public List<Libro> porGenero(@PathVariable String genero) {
        return libroRepository.findAll()
                .stream()
                .filter(l -> l.isDisponible() && genero.equalsIgnoreCase(l.getGenero()))
                .toList();
    }

    // Buscar
    @GetMapping("/buscar")
    public List<Libro> buscar(@RequestParam String q) {
        String query = q.toLowerCase();
        return libroRepository.findAll()
                .stream()
                .filter(l -> l.isDisponible() &&
                    (l.getTitulo().toLowerCase().contains(query) ||
                     l.getAutor().toLowerCase().contains(query)))
                .toList();
    }
}