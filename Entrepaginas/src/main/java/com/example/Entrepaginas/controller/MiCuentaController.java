package com.example.Entrepaginas.controller;

import com.example.Entrepaginas.model.Usuario;
import com.example.Entrepaginas.model.Cliente;
import com.example.Entrepaginas.service.UsuarioService;
import com.example.Entrepaginas.service.PrestamoService;
import com.example.Entrepaginas.service.VentaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/mi-cuenta")
public class MiCuentaController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private VentaService ventaService;

    // Vista principal de "Mi Cuenta"
    @GetMapping
    public String miCuenta(HttpSession session, Model model) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion == null) return "redirect:/login";

        // Recargar desde BD para evitar LazyInitializationException
        Usuario usuario = usuarioService.obtenerPorId(usuarioSesion.getId());
        if (usuario == null) return "redirect:/login";

        // Actualizar sesión con datos frescos
        session.setAttribute("usuarioLogueado", usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("cliente", usuario.getCliente());

        if (usuario.getCliente() != null) {
            Long clienteId = usuario.getCliente().getId();
            model.addAttribute("prestamos",
                prestamoService.obtenerPrestamosPorCliente(clienteId));
            model.addAttribute("ventas",
                ventaService.obtenerVentasPorCliente(clienteId));
        }

        return "mi-cuenta";
    }

    // Guardar cambios del perfil
    @PostMapping("/actualizar")
    public String actualizarPerfil(@ModelAttribute Cliente clienteForm,
                                   HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/login";

        // Si no tiene cliente aún, crear uno nuevo
        if (usuario.getCliente() == null) {
            usuario.setCliente(clienteForm);
        } else {
            Cliente c = usuario.getCliente();
            c.setNombre(clienteForm.getNombre());
            c.setTelefono(clienteForm.getTelefono());
            c.setDni(clienteForm.getDni());
            c.setDireccion(clienteForm.getDireccion());
        }

        Usuario actualizado = usuarioService.actualizarSinContrasena(usuario);

        if (actualizado != null) {
            session.setAttribute("usuarioLogueado", 
                usuarioService.obtenerPorId(actualizado.getId()));
        }
        return "redirect:/mi-cuenta?guardado=true";
    }
}