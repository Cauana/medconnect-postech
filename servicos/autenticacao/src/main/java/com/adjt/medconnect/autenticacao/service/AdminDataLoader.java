package com.adjt.medconnect.autenticacao.service;

import com.adjt.medconnect.autenticacao.model.Role;
import com.adjt.medconnect.autenticacao.model.Usuario;
import com.adjt.medconnect.autenticacao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminDataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        boolean existeAdmin = usuarioRepository
                .existsByRole(Role.ADMIN);

        if (!existeAdmin) {

            Usuario admin = new Usuario();
            admin.setUsuario("admin");
            admin.setSenha(passwordEncoder.encode("admin"));
            admin.setRole(Role.ADMIN);

            usuarioRepository.save(admin);

            System.out.println("✅ Usuário ADMIN criado com sucesso");
        }
    }
}
