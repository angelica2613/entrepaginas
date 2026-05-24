package com.example.Entrepaginas.controller;

import com.example.Entrepaginas.model.Libro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.Entrepaginas.service.CatalagoService;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class CatalogoController {

    @Autowired
    private CatalagoService catalogoService;


    // Catálogo PÚBLICO
    @GetMapping({"/",  "" ,"/catalogo", "/biblioteca", "/index"})
    public String mostrarCatalogo(
            @RequestParam(value = "genero", required = false) String genero,
            @RequestParam(value = "query", required = false) String query,
            Model model,
            HttpSession session) {

        List<Libro> libros;
        if (query != null && !query.isEmpty()) {
            libros = catalogoService.buscarLibros(query);
            if (genero != null && !genero.isEmpty()) {
                libros = libros.stream()
                        .filter(l -> genero.equalsIgnoreCase(l.getGenero()))
                        .toList();
            }
        } else if (genero != null && !genero.isEmpty()) {
            libros = catalogoService.getLibrosByGenero(genero);
        } else {
            libros = catalogoService.getAllLibros();
        }

        model.addAttribute("libros", libros);
        model.addAttribute("generoSeleccionado", genero);
        model.addAttribute("queryBusqueda", query);

        // Para el navbar — sin requerir sesión
        model.addAttribute("usuarioLogueado", session.getAttribute("usuarioLogueado"));
        model.addAttribute("usuarioNombre", session.getAttribute("usuarioNombre"));
        model.addAttribute("usuarioRol", session.getAttribute("usuarioRol"));

        return "biblioteca";
    }

    @GetMapping("/catalogo/{id}")
    public String mostrarDetalleLibro(@PathVariable Long id, Model model, HttpSession session) {
        Optional<Libro> libro = catalogoService.getLibroById(id);
        if (libro.isPresent()) {
            model.addAttribute("libro", libro.get());
            model.addAttribute("usuarioLogueado", session.getAttribute("usuarioLogueado"));
            model.addAttribute("usuarioNombre", session.getAttribute("usuarioNombre"));
            return "detalle-libro";
        }
        return "redirect:/catalogo?error=LibroNoEncontrado";
    }

    @PutMapping("/catalogo/prestar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> prestarLibro(@PathVariable Long id) {
        Libro prestado = catalogoService.prestarLibro(id);
        if (prestado != null) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Libro prestado.", "libro", prestado));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No disponible."));
    }

    @PutMapping("/catalogo/devolver/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> devolverLibro(@PathVariable Long id) {
        Libro devuelto = catalogoService.devolverLibro(id);
        if (devuelto != null) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Libro devuelto.", "libro", devuelto));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No encontrado."));
    }

    @PutMapping("/catalogo/toggle-disponibilidad/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleDisponibilidad(
            @PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            Boolean nuevoEstado = payload.get("disponible");
            if (nuevoEstado == null) {
                response.put("success", false);
                response.put("message", "El estado es requerido.");
                return ResponseEntity.badRequest().body(response);
            }
            Optional<Libro> optionalLibro = catalogoService.getLibroById(id);
            if (optionalLibro.isPresent()) {
                Libro libro = optionalLibro.get();
                libro.setDisponible(nuevoEstado);
                catalogoService.saveLibro(libro);
                response.put("success", true);
                response.put("message", "Disponibilidad actualizada.");
                response.put("libro", libro);
                return ResponseEntity.ok(response);
            }
            response.put("success", false);
            response.put("message", "Libro no encontrado.");
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}