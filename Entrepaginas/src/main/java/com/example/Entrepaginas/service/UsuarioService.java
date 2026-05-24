package com.example.Entrepaginas.service;

import com.example.Entrepaginas.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.Entrepaginas.repository.UsuarioRepository;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario guardar(Usuario usuario) {
        // Solo hashea si NO está ya hasheada (bcrypt empieza con $2a$)
        if (usuario.getContrasena() != null
                && !usuario.getContrasena().isEmpty()
                && !usuario.getContrasena().startsWith("$2a$")) {
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        }
        return usuarioRepository.save(usuario);
    }

    // Para actualizar perfil sin tocar la contraseña
    public Usuario actualizarSinContrasena(Usuario usuario) {
        Usuario existente = usuarioRepository.findById(usuario.getId()).orElse(null);
        if (existente == null) return null;
        existente.setCorreo(usuario.getCorreo());
        existente.setRol(usuario.getRol());
        existente.setCliente(usuario.getCliente());
        return usuarioRepository.save(existente);
    }

    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }
}