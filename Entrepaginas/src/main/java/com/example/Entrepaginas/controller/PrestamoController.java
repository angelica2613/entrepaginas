package com.example.Entrepaginas.controller;

import com.example.Entrepaginas.model.Cliente;
import com.example.Entrepaginas.model.Libro;
import com.example.Entrepaginas.model.Prestamo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.Entrepaginas.service.ClienteService;
import com.example.Entrepaginas.service.LibroService;
import com.example.Entrepaginas.service.PrestamoService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;

@Controller
@RequestMapping("/prestamos")  // Changed base path
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private ClienteService clienteService;  // Added missing autowiring

    @Autowired
    private LibroService libroService;      // Added missing autowiring

    @GetMapping
    public String listarPrestamos(Model model, HttpSession session) {
        Object nombre = session.getAttribute("usuarioNombre");
        if (nombre == null) {
            return "redirect:/entrepaginas/login";
        }

        model.addAttribute("prestamos", prestamoService.obtenerTodos());
        model.addAttribute("usuarioNombre", nombre.toString());
        model.addAttribute("usuarioRol", session.getAttribute("usuarioRol"));
        
        return "prestamos";
    }

    @GetMapping("/nuevo")
    public String formularioNuevoPrestamo(Model model, HttpSession session) {
        Object nombre = session.getAttribute("usuarioNombre");
        if (nombre == null) {
            return "redirect:/entrepaginas/login";
        }

        model.addAttribute("prestamo", new Prestamo());
        model.addAttribute("clientes", clienteService.obtenerTodos());
        model.addAttribute("libros", libroService.obtenerTodos());
        model.addAttribute("usuarioNombre", nombre.toString());
        model.addAttribute("usuarioRol", session.getAttribute("usuarioRol"));
        
        return "nuevo-prestamo";
    }

    @PostMapping
    public String guardarPrestamo(@ModelAttribute Prestamo prestamo, 
                                @RequestParam Long clienteId, 
                                @RequestParam Long libroId,
                                @RequestParam(required = false) String fechaDevolucionEstimada, 
                                HttpSession session) {
        Object nombre = session.getAttribute("usuarioNombre");
        if (nombre == null) {
            return "redirect:/entrepaginas/login";
        }

        Cliente cliente = clienteService.obtenerPorId(clienteId);
        Libro libro = libroService.obtenerPorId(libroId);
        
        if (cliente != null && libro != null && libro.isDisponible()) {
            prestamo.setCliente(cliente);
            prestamo.setLibro(libro);
            prestamo.setFechaPrestamo(LocalDate.now());
            prestamo.setActivo(true);
            libro.setDisponible(false);
        
        if (fechaDevolucionEstimada != null && !fechaDevolucionEstimada.isEmpty()) {
            prestamo.setFechaDevolucion(LocalDate.parse(fechaDevolucionEstimada));
        }
            
            libroService.guardar(libro);
            prestamoService.guardar(prestamo);
        }
        
        return "redirect:/prestamos";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditarPrestamo(@PathVariable Long id, Model model, HttpSession session) {
        Object nombre = session.getAttribute("usuarioNombre");
        if (nombre == null) {
            return "redirect:/entrepaginas/login";
        }

        Prestamo prestamo = prestamoService.obtenerPorId(id);
        model.addAttribute("prestamo", prestamo);
        model.addAttribute("clientes", clienteService.obtenerTodos());
        model.addAttribute("libros", libroService.obtenerTodos());
        model.addAttribute("usuarioNombre", nombre.toString());
        model.addAttribute("usuarioRol", session.getAttribute("usuarioRol"));
        
        return "editar-prestamo";
    }

    @GetMapping("/devolver/{id}")
    public String devolverPrestamo(@PathVariable Long id, HttpSession session) {
        Object nombre = session.getAttribute("usuarioNombre");
        if (nombre == null) {
            return "redirect:/entrepaginas/login";
        }


        Prestamo prestamo = prestamoService.obtenerPorId(id);
        if (prestamo != null && prestamo.isActivo()) {
            prestamo.setActivo(false);
            prestamo.setFechaDevolucion(LocalDate.now());
            Libro libro = prestamo.getLibro();
            if (libro != null) {
                libro.setDisponible(true);
                libroService.guardar(libro);
            }
            prestamoService.guardar(prestamo);
        }
        
        return "redirect:/prestamos"; // Redirige a la lista de préstamos
    }

    @GetMapping("/eliminar/{id}") // NUEVO: Endpoint para eliminar un préstamo
    public String eliminarPrestamo(@PathVariable Long id, HttpSession session) {
        Object nombre = session.getAttribute("usuarioNombre");
        if (nombre == null) {
            return "redirect:/entrepaginas/login";
        }

        Prestamo prestamo = prestamoService.obtenerPorId(id);
        if (prestamo != null) {
            // Si el préstamo está activo, primero asegúrate de que el libro vuelva a estar disponible
            if (prestamo.isActivo()) {
                Libro libro = prestamo.getLibro();
                if (libro != null) {
                    libro.setDisponible(true);
                    libroService.guardar(libro);
                }
            }
            prestamoService.eliminar(id);
        }
        return "redirect:/prestamos"; // Redirige a la lista de préstamos
    }

    @Autowired
    private com.example.Entrepaginas.repository.ClienteRepository clienteRepository;

    // ====================================================================
    // API PARA PRÉSTAMOS WEB (BIBLIOTECA)
    // ====================================================================
    
    @PostMapping("/api/crear")
    @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> crearPrestamoWeb(@RequestBody com.example.Entrepaginas.dto.PrestamoWebDTO prestamoWebDTO) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            // 1. Crear o Buscar Cliente "Web"
            Cliente cliente = new Cliente();
            cliente.setNombre(prestamoWebDTO.getClienteNombre());
            cliente.setCorreo(prestamoWebDTO.getClienteEmail());
            // Generar datos ficticios obligatorios si faltan
            cliente.setDni("00000000"); 
            cliente.setDireccion("Dirección Web");
            cliente.setTelefono("000-000000");
            
            // Podrías buscar si el correo ya existe para no duplicar
            Cliente existente = clienteRepository.findByCorreo(prestamoWebDTO.getClienteEmail());
            if (existente != null) {
                cliente = existente;
            } else {
                cliente = clienteRepository.save(cliente);
            }

            // 2. Buscar Libro
            Libro libro = libroService.obtenerPorId(prestamoWebDTO.getLibroId());
            if (libro == null) {
                throw new Exception("Libro no encontrado con ID: " + prestamoWebDTO.getLibroId());
            }
            if (!libro.isDisponible()) {
                throw new Exception("El libro '" + libro.getTitulo() + "' no está disponible actualmente.");
            }

            // 3. Crear Préstamo
            Prestamo prestamo = new Prestamo();
            prestamo.setCliente(cliente);
            prestamo.setLibro(libro);
            prestamo.setFechaPrestamo(LocalDate.now());
            prestamo.setFechaDevolucion(prestamoWebDTO.getFechaDevolucion()); // Asumiendo que el DTO lo envía
            prestamo.setActivo(true);
            
            // 4. Actualizar Libro
            libro.setDisponible(false);
            libroService.guardar(libro);
            
            prestamoService.guardar(prestamo);
            
            response.put("success", true);
            response.put("message", "Préstamo registrado con éxito. ID: " + prestamo.getId());
            return org.springframework.http.ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al procesar el préstamo: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}