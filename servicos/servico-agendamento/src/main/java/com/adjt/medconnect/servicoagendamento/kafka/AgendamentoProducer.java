package com.adjt.medconnect.servicoagendamento.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.adjt.medconnect.servicoagendamento.model.Consulta;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class AgendamentoProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public AgendamentoProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void enviarMensagem(Consulta consulta) {
        System.out.println("Enviando mensagem para o Kafka: " + consulta);
        try {
            String consultaJson = objectMapper.writeValueAsString(consulta);
            kafkaTemplate.send("agendamentos-topico", consultaJson);
        } catch (Exception e) {
            System.err.println("Erro ao serializar Consulta: " + e.getMessage());
        }
    }
}
