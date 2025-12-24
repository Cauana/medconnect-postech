package com.adjt.medconnect.serviconotificacao.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoConsumer {

    @KafkaListener(topics = "agendamento-topic", groupId = "medconnect")
    public void consumirMensagem(String mensagem) {
        System.out.println("📩 Nova consulta recebida pelo serviço de notificação: " + mensagem);
        // Aqui você poderia enviar e-mail ou logar no banco
    }
}
