package com.example.Entrepaginas.controller.api;

import com.example.Entrepaginas.model.Cliente;
import com.example.Entrepaginas.model.Usuario;
import com.example.Entrepaginas.repository.ClienteRepository;
import com.example.Entrepaginas.repository.PrestamoRepository;
import com.example.Entrepaginas.repository.UsuarioRepository;
import com.example.Entrepaginas.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mi-cuenta")
public class MiCuentaApiController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private PrestamoRepository prestamoRepository;
    @Autowired private VentaRepository ventaRepository;

@GetMapping("/{id}")
public ResponseEntity<Map<String, Object>> obtenerPerfil(@PathVariable Long id) {
    Usuario usuario = usuarioRepository.findById(id).orElse(null);
    if (usuario == null) return ResponseEntity.notFound().build();

        Map<String, Object> resp = new HashMap<>();
        resp.put("correo", usuario.getCorreo());
        resp.put("rol", usuario.getRol());

        Cliente cliente = usuario.getCliente();
        if (cliente != null) {
            resp.put("nombre", cliente.getNombre());
            resp.put("dni", cliente.getDni());
            resp.put("telefono", cliente.getTelefono());
            resp.put("direccion", cliente.getDireccion());
            resp.put("clienteId", cliente.getId());

            resp.put("prestamos", prestamoRepository.findByClienteId(cliente.getId()));
            resp.put("ventas", ventaRepository.findByClienteId(cliente.getId()));
        }

        return ResponseEntity.ok(resp);
    }

    @PutMapping("/actualizar")
    public ResponseEntity<Map<String, Object>> actualizar(@RequestBody Map<String, String> datos) {
        try {
            String correo = datos.get("correo");
            Usuario usuario = usuarioRepository.findByCorreo(correo);
            if (usuario == null) return ResponseEntity.notFound().build();

            Cliente cliente = usuario.getCliente();
            if (cliente == null) {
                cliente = new Cliente();
                cliente.setCorreo(correo);
            }

            if (datos.get("nombre") != null) cliente.setNombre(datos.get("nombre"));
            if (datos.get("dni") != null) cliente.setDni(datos.get("dni"));
            if (datos.get("telefono") != null) cliente.setTelefono(datos.get("telefono"));
            if (datos.get("direccion") != null) cliente.setDireccion(datos.get("direccion"));

            clienteRepository.save(cliente);
            usuario.setCliente(cliente);
            usuarioRepository.save(usuario);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}