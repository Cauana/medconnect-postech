package com.adjt.medconnect.servicoagendamento.controller;

import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import com.adjt.medconnect.servicoagendamento.model.Consulta;
import com.adjt.medconnect.servicoagendamento.model.StatusConsulta;
import com.adjt.medconnect.servicoagendamento.repository.ConsultaRepository;
import com.adjt.medconnect.servicoagendamento.service.ConsultaService;

import java.util.List;

@RestController
@RequestMapping("/consultas")
@Tag(name = "Consultas", description = "Endpoints para gerenciamento de consultas médicas")
public class ConsultaController {

    private final ConsultaRepository repositorio;

    private final ConsultaService service;

    public ConsultaController(ConsultaRepository repositorio, ConsultaService service) {
        this.repositorio = repositorio;
        this.service = service;
    }
    
    @PostMapping
    @Operation(
        summary = "Criar nova consulta",
        description = "Cria uma nova consulta e envia notificação através do Kafka"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consulta criada com sucesso", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Consulta.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public Consulta criar(@RequestBody Consulta consulta) {
        consulta.setStatus(StatusConsulta.AGENDADA);
        service.agendarConsulta(consulta);
        return repositorio.save(consulta);
    }

    @GetMapping
    @Operation(
        summary = "Listar todas as consultas",
        description = "Retorna uma lista de todas as consultas cadastradas"
    )
    @ApiResponse(responseCode = "200", description = "Lista de consultas retornada com sucesso")
    public List<Consulta> listar() {
        return repositorio.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar consulta por ID",
        description = "Retorna uma consulta específica pelo seu ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consulta encontrada"),
        @ApiResponse(responseCode = "404", description = "Consulta não encontrada")
    })
    public Consulta buscarPorId(@PathVariable long id) {
        return repositorio.findById(id).orElse(null);
    }

    @PutMapping("/{id}/status")
    @Operation(
        summary = "Atualizar status da consulta",
        description = "Atualiza o status de uma consulta existente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Consulta não encontrada")
    })
    public Consulta atualizarStatus(@PathVariable long id, @RequestParam String status) {
        var consulta = repositorio.findById(id).orElse(null);
        if (consulta != null) {
            consulta.setStatus(Enum.valueOf(StatusConsulta.class, status.toUpperCase()));
            return repositorio.save(consulta);
        }
        return null;
    }
}
