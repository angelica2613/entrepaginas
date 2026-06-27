package com.example.Entrepaginas;

import com.example.Entrepaginas.model.Usuario;
import com.example.Entrepaginas.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.findByCorreo("admin@entrepaginas.org") == null) {
                Usuario usuario = new Usuario();
                usuario.setCorreo("admin@entrepaginas.org");
                usuario.setContrasena(passwordEncoder.encode("123456"));
                usuario.setRol("ADMIN");
                usuarioRepository.save(usuario);
            }
        };
    }
}