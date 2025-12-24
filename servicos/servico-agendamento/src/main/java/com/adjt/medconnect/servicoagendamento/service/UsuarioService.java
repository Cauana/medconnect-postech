package com.adjt.medconnect.servicoagendamento.service;

import org.springframework.stereotype.Service;

import com.adjt.medconnect.servicoagendamento.model.Usuario;
import com.adjt.medconnect.servicoagendamento.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepositorio;

    public UsuarioService(UsuarioRepository usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public Usuario salvar(Usuario usuario) {
        return usuarioRepositorio.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepositorio.findAll();
    }

    public void deletar(long id) {
        usuarioRepositorio.deleteById(id);
    }
}
