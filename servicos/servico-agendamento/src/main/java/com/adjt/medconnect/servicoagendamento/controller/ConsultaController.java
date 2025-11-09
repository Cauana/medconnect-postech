package com.adjt.medconnect.servicoagendamento.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.adjt.medconnect.servicoagendamento.kafka.AgendamentoProducer;

@RestController
@RequestMapping("/agendamentos")
public class ConsultaController {

    private final AgendamentoProducer producer;

    public ConsultaController(AgendamentoProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<String> criarConsulta(@RequestBody String consultaJson) {
        producer.enviarMensagem(consultaJson);
        return ResponseEntity.ok("Consulta enviada para processamento!");
    }
}
