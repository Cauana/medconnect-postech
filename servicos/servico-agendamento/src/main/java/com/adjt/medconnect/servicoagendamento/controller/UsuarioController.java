package com.adjt.medconnect.servicoagendamento.controller;

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
    public Usuario criar(@RequestBody Usuario usuario) {
        return repository.save(usuario);
    }
    
    @GetMapping
    public List<Usuario> listar() {
        return repository.findAll();
    }
}
