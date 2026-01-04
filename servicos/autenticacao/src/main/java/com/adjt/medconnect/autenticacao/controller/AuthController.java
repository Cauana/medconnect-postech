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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/test-role")
    public ResponseEntity<String> testRole(Authentication auth) {
        return ResponseEntity.ok(
                "Usuário: " + auth.getName() +
                        "\nRoles: " + auth.getAuthorities().toString() +
                        "\nÉ admin? " + auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    // teste para verificação do
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debug(Authentication auth) {
        Map<String, Object> debugInfo = new HashMap<>();

        if (auth != null) {
            debugInfo.put("authenticated", true);
            debugInfo.put("username", auth.getName());
            debugInfo.put("authorities", auth.getAuthorities().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList()));
            debugInfo.put("isAdmin", auth.getAuthorities().contains(
                    new SimpleGrantedAuthority("ROLE_ADMIN")));
            debugInfo.put("isMedico", auth.getAuthorities().contains(
                    new SimpleGrantedAuthority("ROLE_MEDICO")));
            debugInfo.put("isPaciente", auth.getAuthorities().contains(
                    new SimpleGrantedAuthority("ROLE_PACIENTE")));
        } else {
            debugInfo.put("authenticated", false);
        }

        return ResponseEntity.ok(debugInfo);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody Usuario usuario) {
        // Valida se a role foi informada
        if (usuario.getRole() == null) {
            usuario.setRole(Role.PACIENTE); // Default é PACIENTE
        }

        // Verifica se está tentando criar ADMIN (não permitido via registro público)
        if (usuario.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Não é permitido criar usuários ADMIN via registro público. "
                            + "Use apenas: PACIENTE, MEDICO ou ENFERMEIRO"
            );
        }

        // Valida se é uma role válida (excluindo ADMIN)
        validateRoleForRegistration(usuario.getRole());

        usuarioService.salvar(usuario);

        return ResponseEntity.created(
                URI.create("/auth/" + usuario.getId())
        ).build();
    }

    private void validateRoleForRegistration(Role role) {
        // Lista de roles permitidas para registro público
        List<Role> allowedRoles = Arrays.asList(Role.PACIENTE, Role.MEDICO, Role.ENFERMEIRO);

        if (!allowedRoles.contains(role)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role inválida para registro. Use apenas: " +
                            allowedRoles.stream()
                                    .map(Enum::name)
                                    .collect(Collectors.joining(", "))
            );
        }
    }

    @GetMapping("/admin/test")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminTest(Authentication auth) {
        return auth.getAuthorities().toString();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@Valid @RequestBody LoginDTO dto) {
    try{
       authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUsuario(),
                        dto.getSenha()
                )
        );
        Usuario usuario = usuarioRepository.findByUsuario(dto.getUsuario())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        String token = jwtService.gerarToken(usuario);

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

    @GetMapping("/roles")
    public List<String> listaRoles(){
        return Arrays.stream(Role.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
