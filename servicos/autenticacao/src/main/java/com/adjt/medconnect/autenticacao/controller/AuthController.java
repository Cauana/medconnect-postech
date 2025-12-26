package com.adjt.medconnect.autenticacao.controller;

import com.adjt.medconnect.autenticacao.dto.*;
import com.adjt.medconnect.autenticacao.exception.CredenciaisInvalidasException;
import com.adjt.medconnect.autenticacao.exception.UnauthorizedException;
import com.adjt.medconnect.autenticacao.model.Role;
import com.adjt.medconnect.autenticacao.model.Usuario;
import com.adjt.medconnect.autenticacao.repository.UsuarioRepository;
import com.adjt.medconnect.autenticacao.service.JwtService;
import com.adjt.medconnect.autenticacao.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody Usuario usuario) {
        usuario.setRole(Role.PACIENTE); // força role segura
        usuarioService.salvar(usuario);

        return ResponseEntity.created(
                URI.create("/auth/" + usuario.getId())
        ).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@Valid @RequestBody LoginDTO dto) {
    try{
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUsuario(),
                        dto.getSenha()
                )
        );
        String token = jwtService.gerarToken(authentication);

        return ResponseEntity.ok(new TokenDTO(token));

    }catch(BadCredentialsException ex){
        throw new CredenciaisInvalidasException();
    }
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse> updatePassword(
            Authentication authentication,
            @RequestBody UpdatePasswordDTO dto
            ){

        if(authentication == null || !authentication.isAuthenticated()){
            throw new UnauthorizedException();
        }
        usuarioService.atualizarSenhaDoUsuarioLogado(
                authentication.getName(),
                dto.novaSenha()
        );
        return ResponseEntity.ok(
                new ApiResponse("Senha alterada com sucesso")
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse> adminUpdatePassword(
            @PathVariable Long id,
            @RequestBody UpdatePasswordDTO dto
            ){
        usuarioService.atualizarSenhaPorAdmin(id, dto.novaSenha());

        return ResponseEntity.ok(
                new ApiResponse("Senha do usuário alterada com sucesso.")
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<String> me(Authentication auth) {
        return ResponseEntity.ok("Você está autenticado: " + auth.getName());
    }
}
