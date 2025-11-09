package com.adjt.medconnect.servicoagendamento.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.adjt.medconnect.servicoagendamento.kafka.AgendamentoProducer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/agendamentos")
@Tag(name = "Consultas", description = "Endpoints para gerenciamento de consultas médicas")
public class ConsultaController {

    private final AgendamentoProducer producer;

    public ConsultaController(AgendamentoProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    @Operation(
        summary = "Criar nova consulta",
        description = "Cria uma nova consulta e envia notificação através do Kafka"
    )
    public ResponseEntity<String> criarConsulta(@RequestBody String consultaJson) {
        producer.enviarMensagem(consultaJson);
        return ResponseEntity.ok("Consulta enviada para processamento!");
    }
}
