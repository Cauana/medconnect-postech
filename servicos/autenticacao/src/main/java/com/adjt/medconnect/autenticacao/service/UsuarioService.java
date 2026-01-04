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
    public UserDetails loadUserByUsername(String usuarioLoad) throws UsernameNotFoundException{
        Usuario usuario = usuarioRepository.findByUsuario(usuarioLoad)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + usuarioLoad));
        return User.builder()
                .username(usuario.getUsuario())
                .password(usuario.getSenha())
                .roles(usuario.getRole().name())
                .build();
    }

    public Usuario salvar(Usuario usuario) {

        if(usuarioRepository.existsByUsuario(usuario.getUsuario())){
            throw new UsuarioJaExisteException("Usuário já cadastrado");
        }
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void atualizarSenhaDoUsuarioLogado(String usuarioLogado, String novaSenha){
        Usuario usuario = usuarioRepository.findByUsuario(usuarioLogado)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        if(novaSenha == null || novaSenha.isBlank()){
            throw new IllegalArgumentException("Senha não pode ser vazia");
        }
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void atualizarSenhaPorAdmin(Long id, String novaSenha){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(UsuarioNaoEncontradoException::new);
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }


    public void deletar(Long id){
        if(!usuarioRepository.existsById(id)){
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        usuarioRepository.deleteById(id);
    }
}
