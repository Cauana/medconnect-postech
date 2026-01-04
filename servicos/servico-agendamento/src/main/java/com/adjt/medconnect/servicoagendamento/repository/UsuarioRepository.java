package com.adjt.medconnect.servicoagendamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adjt.medconnect.servicoagendamento.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
    Usuario findByCpf(String cpf);
}
