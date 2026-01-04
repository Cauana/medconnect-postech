package com.adjt.medconnect.autenticacao.repository;

import com.adjt.medconnect.autenticacao.model.Role;
import com.adjt.medconnect.autenticacao.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByUsuario(String usuario);
    Optional<Usuario> findByUsuario(String usuario);
    boolean existsByRole(Role role);
}
