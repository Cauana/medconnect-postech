package com.adjt.medconnect.autenticacao.service;

import com.adjt.medconnect.autenticacao.exception.UsuarioJaExisteException;
import com.adjt.medconnect.autenticacao.exception.UsuarioNaoEncontradoException;
import com.adjt.medconnect.autenticacao.model.Usuario;
import com.adjt.medconnect.autenticacao.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRole().name())
                .build();
    }

    public Usuario salvar(Usuario usuario) {

        if(usuarioRepository.existsByUsername(usuario.getUsername())){
            throw new UsuarioJaExisteException("Usuário já cadastrado");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void atualizarSenhaDoUsuarioLogado(String username, String novaSenha){
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        if(novaSenha == null || novaSenha.isBlank()){
            throw new IllegalArgumentException("Senha não pode ser vazia");
        }
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void atualizarSenhaPorAdmin(Long id, String novaSenha){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(UsuarioNaoEncontradoException::new);
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }


    public void deletar(Long id){
        if(!usuarioRepository.existsById(id)){
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        usuarioRepository.deleteById(id);
    }
}
