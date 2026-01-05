package com.adjt.medconnect.servicoagendamento.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.adjt.medconnect.servicoagendamento.model.Consulta;
import com.adjt.medconnect.servicoagendamento.model.StatusConsulta;
import com.adjt.medconnect.servicoagendamento.repository.ConsultaRepository;
import com.adjt.medconnect.servicoagendamento.service.ConsultaService;

@RestController
@RequestMapping("/consultas")
@Tag(name = "Consultas", description = "Endpoints para gerenciamento de consultas médicas")
@SecurityRequirement(name = "bearer-jwt")
public class ConsultaController {

    private final ConsultaRepository repositorio;
    private final ConsultaService service;

    public ConsultaController(ConsultaRepository repositorio, ConsultaService service) {
        this.repositorio = repositorio;
        this.service = service;
    }
    
    /**
     * Extrai o papel (role) do usuário autenticado
     */
    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("");
    }
    
    /**
     * Extrai o ID do usuário autenticado
     */
    private long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDICO') or hasRole('ENFERMEIRO')")
    @Operation(
        summary = "Criar nova consulta",
        description = "Cria uma nova consulta e envia notificação através do Kafka. Apenas ADMIN, MEDICO e ENFERMEIRO podem criar."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consulta criada com sucesso", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Consulta.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para criar consultas")
    })
    public ResponseEntity<Consulta> criar(@RequestBody Consulta consulta) {
        try {
            consulta.setStatus(StatusConsulta.AGENDADA);
            service.agendarConsulta(consulta);
            Consulta consultaSalva = repositorio.save(consulta);
            return ResponseEntity.status(HttpStatus.CREATED).body(consultaSalva);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDICO') or hasRole('ENFERMEIRO') or hasRole('PACIENTE')")
    @Operation(
        summary = "Listar consultas com controle de acesso por papel",
        description = "MÉDICOS veem suas próprias consultas (idMedico = userId). ENFERMEIROS veem todas. PACIENTES veem apenas suas consultas (idPaciente = userId)."
    )
    @ApiResponse(responseCode = "200", description = "Lista de consultas retornada com sucesso")
    public ResponseEntity<List<Consulta>> listar() {
        String userRole = getCurrentUserRole();
        long userId = getCurrentUserId();
        List<Consulta> consultas;
        
        if ("PACIENTE".equals(userRole)) {
            // Pacientes veem apenas suas próprias consultas
            consultas = repositorio.findByIdPaciente(userId);
        } else if ("MEDICO".equals(userRole)) {
            // Médicos veem apenas suas próprias consultas (onde são o médico responsável)
            consultas = repositorio.findByIdMedico(userId);
        } else {
            // Enfermeiros e Admin veem todas as consultas
            consultas = repositorio.findAll();
        }
        
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDICO') or hasRole('ENFERMEIRO') or hasRole('PACIENTE')")
    @Operation(
        summary = "Buscar consulta por ID com controle de acesso",
        description = "MÉDICOS veem apenas suas próprias consultas. PACIENTES veem apenas suas. ENFERMEIROS e ADMIN veem qualquer uma."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consulta encontrada", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Consulta.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado - Não autorizado a visualizar esta consulta"),
        @ApiResponse(responseCode = "404", description = "Consulta não encontrada")
    })
    public ResponseEntity<Consulta> buscarPorId(@PathVariable long id) {
        String userRole = getCurrentUserRole();
        long userId = getCurrentUserId();
        
        Optional<Consulta> consulta = repositorio.findById(id);
        
        if (consulta.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Consulta consultaEncontrada = consulta.get();
        
        // PACIENTE: pode ver apenas suas próprias consultas
        if ("PACIENTE".equals(userRole)) {
            if (consultaEncontrada.getIdPaciente() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        // MÉDICO: pode ver apenas suas próprias consultas (onde é o médico responsável)
        else if ("MEDICO".equals(userRole)) {
            if (consultaEncontrada.getIdMedico() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        // ENFERMEIRO e ADMIN: têm acesso total
        
        return ResponseEntity.ok(consultaEncontrada);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDICO') or hasRole('ENFERMEIRO')")
    @Operation(
        summary = "Atualizar status da consulta",
        description = "Atualiza o status de uma consulta. Apenas ADMIN e MÉDICOS podem alterar status. MÉDICOS só podem editar suas próprias consultas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Consulta.class))),
        @ApiResponse(responseCode = "400", description = "Status inválido"),
        @ApiResponse(responseCode = "404", description = "Consulta não encontrada"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para atualizar")
    })
    public ResponseEntity<Consulta> atualizarStatus(@PathVariable long id, @RequestBody java.util.Map<String, String> body) {
        String userRole = getCurrentUserRole();
        long userId = getCurrentUserId();
        try {
            String status = body.get("status");
            if (status == null || status.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            StatusConsulta novoStatus = Enum.valueOf(StatusConsulta.class, status.toUpperCase());
            Consulta atualizada = service.atualizarStatusAutorizado(id, novoStatus, userRole, userId);
            return ResponseEntity.ok(atualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDICO') or hasRole('ENFERMEIRO')")
    @Operation(
        summary = "Editar consulta completa",
        description = "Edita uma consulta permitindo alterações de data, médico responsável, observações e status. " +
                      "MÉDICOS só podem editar suas próprias consultas. ENFERMEIROS e ADMIN podem editar qualquer uma."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consulta atualizada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Consulta.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - MÉDICO não pode editar consulta de outro médico"),
        @ApiResponse(responseCode = "404", description = "Consulta não encontrada")
    })
    public ResponseEntity<Consulta> editarConsulta(@PathVariable long id, @RequestBody Consulta consultaAtualizada) {
        String userRole = getCurrentUserRole();
        long userId = getCurrentUserId();
        try {
            Consulta atualizada = service.editarConsulta(id, consultaAtualizada, userRole, userId);
            return ResponseEntity.ok(atualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDICO') or hasRole('ENFERMEIRO')")
    @Operation(
        summary = "Deletar consulta",
        description = "Deleta uma consulta existente. Apenas ADMIN, MEDICO e ENFERMEIRO podem deletar."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Consulta deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Consulta não encontrada"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para deletar")
    })
    public ResponseEntity<Void> deletar(@PathVariable long id) {
        if (repositorio.existsById(id)) {
            repositorio.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
