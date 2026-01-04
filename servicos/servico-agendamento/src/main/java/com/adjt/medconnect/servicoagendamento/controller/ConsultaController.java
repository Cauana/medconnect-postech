package com.adjt.medconnect.servicoagendamento.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

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
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENFERMEIRO')")
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
        summary = "Listar todas as consultas",
        description = "Retorna uma lista de todas as consultas cadastradas. Qualquer usuário autenticado pode listar."
    )
    @ApiResponse(responseCode = "200", description = "Lista de consultas retornada com sucesso")
    public ResponseEntity<List<Consulta>> listar() {
        return ResponseEntity.ok(repositorio.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDICO') or hasRole('ENFERMEIRO') or hasRole('PACIENTE')")
    @Operation(
        summary = "Buscar consulta por ID",
        description = "Retorna uma consulta específica pelo seu ID. Qualquer usuário autenticado pode consultar."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consulta encontrada", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Consulta.class))),
        @ApiResponse(responseCode = "404", description = "Consulta não encontrada"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<Consulta> buscarPorId(@PathVariable long id) {
        return repositorio.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDICO') or hasRole('ENFERMEIRO')")
    @Operation(
        summary = "Atualizar status da consulta",
        description = "Atualiza o status de uma consulta existente. Apenas ADMIN, MEDICO e ENFERMEIRO podem alterar status."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Consulta.class))),
        @ApiResponse(responseCode = "400", description = "Status inválido"),
        @ApiResponse(responseCode = "404", description = "Consulta não encontrada"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para atualizar")
    })
    public ResponseEntity<Consulta> atualizarStatus(@PathVariable long id, @RequestParam String status) {
        try {
            var consulta = repositorio.findById(id);
            if (consulta.isPresent()) {
                Consulta consultaAtualizar = consulta.get();
                consultaAtualizar.setStatus(Enum.valueOf(StatusConsulta.class, status.toUpperCase()));
                Consulta consultaSalva = repositorio.save(consultaAtualizar);
                return ResponseEntity.ok(consultaSalva);
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
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
