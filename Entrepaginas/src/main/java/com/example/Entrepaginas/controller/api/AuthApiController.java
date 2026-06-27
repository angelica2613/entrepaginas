package com.example.Entrepaginas.controller.api;

import com.example.Entrepaginas.model.Cliente;
import com.example.Entrepaginas.model.Libro;
import com.example.Entrepaginas.model.Prestamo;
import com.example.Entrepaginas.model.Usuario;
import com.example.Entrepaginas.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.Entrepaginas.repository.ClienteRepository;
import com.example.Entrepaginas.repository.LibroRepository;
import com.example.Entrepaginas.repository.PrestamoRepository;
import java.time.LocalDate;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> datos) {
        String correo = datos.get("correo");
        String contrasena = datos.get("contrasena");

        Usuario usuario = usuarioRepository.findByCorreo(correo);

        if (usuario != null && passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "id", usuario.getId(),
                "correo", usuario.getCorreo(),
                "rol", usuario.getRol()
            ));
        }

        return ResponseEntity.status(401).body(Map.of(
            "success", false,
            "message", "Correo o contraseña incorrectos"
        ));
    }

    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registro(@RequestBody Map<String, String> datos) {
        try {
            String correo = datos.get("correo");
            
            if (usuarioRepository.findByCorreo(correo) != null) {
                return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "message", "Ese correo ya está registrado"
                ));
            }

            Usuario usuario = new Usuario();
            usuario.setCorreo(correo);
            usuario.setContrasena(passwordEncoder.encode(datos.get("contrasena")));
            usuario.setRol("CLIENTE");
            usuarioRepository.save(usuario);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error al registrar"
            ));
        }
    }
    @PostMapping("/prestamo/solicitar")
public ResponseEntity<Map<String, Object>> solicitarPrestamo(
        @RequestBody Map<String, Object> datos,
        HttpSession session) {
    try {
        // Obtener usuario de sesión (Thymeleaf) 
        // o del localStorage (Angular lo manda en el body)
        Long libroId = Long.valueOf(datos.get("libroId").toString());
        String correoCliente = datos.get("correo").toString();
        String fechaStr = datos.get("fechaDevolucion").toString();
        LocalDate fechaDevolucion = LocalDate.parse(fechaStr);

        // Buscar cliente por correo
        Cliente cliente = clienteRepository.findByCorreo(correoCliente);
        if (cliente == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Cliente no encontrado"
            ));
        }

        // Buscar libro
        Libro libro = libroRepository.findById(libroId).orElse(null);
        if (libro == null || !libro.isDisponible()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Libro no disponible"
            ));
        }

        // Crear préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setCliente(cliente);
        prestamo.setLibro(libro);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaDevolucion(fechaDevolucion);
        prestamo.setActivo(true);

        libro.setDisponible(false);
        libroRepository.save(libro);
        prestamoRepository.save(prestamo);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Préstamo registrado correctamente",
            "prestamoId", prestamo.getId()
        ));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Error: " + e.getMessage()
        ));
    }
}

}