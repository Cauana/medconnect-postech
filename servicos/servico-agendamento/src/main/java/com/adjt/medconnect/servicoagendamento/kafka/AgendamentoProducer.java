package com.adjt.medconnect.servicoagendamento.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AgendamentoProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public AgendamentoProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void enviarMensagem(String mensagem) {
        System.out.println("📤 Enviando mensagem para o Kafka: " + mensagem);
        kafkaTemplate.send("agendamentos-topico", mensagem);
    }
}
