package com.adjt.medconnect.servicoagendamento.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.adjt.medconnect.servicoagendamento.model.Usuario;
import com.adjt.medconnect.servicoagendamento.repository.UsuarioRepository;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository repository;

    public UsuarioController(UsuarioRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criar(@RequestBody Usuario usuario) {
        // Verifica se já existe usuário com o mesmo CPF ou email
        Usuario existentePorCpf = repository.findByCpf(usuario.getCpf());
        if (existentePorCpf != null) {
            return ResponseEntity.ok(existentePorCpf); // Retorna o existente
        }
        
        Usuario existentePorEmail = repository.findByEmail(usuario.getEmail());
        if (existentePorEmail != null) {
            return ResponseEntity.ok(existentePorEmail); // Retorna o existente
        }
        
        try {
            Usuario salvo = repository.save(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Erro ao criar perfil: " + e.getMessage()));
        }
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDICO') or hasRole('ENFERMEIRO')")
    public List<Usuario> listar() {
        return repository.findAll();
    }
    
    // Classe auxiliar para resposta de erro
    static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
