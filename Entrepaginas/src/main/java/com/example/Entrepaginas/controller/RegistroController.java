package com.example.Entrepaginas.controller;

import com.example.Entrepaginas.model.Cliente;
import com.example.Entrepaginas.model.Usuario;
import com.example.Entrepaginas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    // Si alguien va directo a /registro → redirige al login
    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "redirect:/login";
    }

    // El modal llama a este endpoint con fetch (JSON)
   @PostMapping("/registro")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> procesarRegistro(
            @RequestBody Map<String, Object> datos) {
        try {
            Usuario usuario = new Usuario();
            usuario.setCorreo((String) datos.get("correo"));
            usuario.setContrasena((String) datos.get("contrasena"));
            usuario.setRol("CLIENTE");

            // Crear cliente con los datos extra si vienen
            if (datos.get("cliente") != null) {
                @SuppressWarnings("unchecked")
                Map<String, String> clienteData = (Map<String, String>) datos.get("cliente");
                Cliente cliente = new Cliente();
                cliente.setNombre(clienteData.get("nombre"));
                cliente.setDni(clienteData.get("dni"));
                cliente.setTelefono(clienteData.get("telefono"));
                cliente.setDireccion(clienteData.get("direccion"));
                cliente.setCorreo(clienteData.get("correo"));
                usuario.setCliente(cliente);
            }

            usuarioService.guardar(usuario);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("success", false, "message", "Ese correo ya está registrado."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Error al crear la cuenta."));
        }
    }
}