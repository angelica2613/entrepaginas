package com.example.Entrepaginas.controller.api;

import com.example.Entrepaginas.model.Usuario;
import com.example.Entrepaginas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
}