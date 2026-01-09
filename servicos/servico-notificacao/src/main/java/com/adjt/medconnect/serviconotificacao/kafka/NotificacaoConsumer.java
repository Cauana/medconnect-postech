package com.adjt.medconnect.serviconotificacao.kafka;

import com.adjt.medconnect.serviconotificacao.event.ConsultaEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.time.format.DateTimeFormatter;

@Service
public class NotificacaoConsumer {

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;
    @Value("${spring.mail.username:}")
    private String mailFrom;

    public NotificacaoConsumer(JavaMailSender mailSender, ObjectMapper objectMapper) {
        this.mailSender = mailSender;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "agendamento-topic", groupId = "medconnect")
    public void consumirMensagem(String mensagem) {
        try {
            System.out.println("Consumido evento de agendamento: " + mensagem);
            ConsultaEvent event = objectMapper.readValue(mensagem, ConsultaEvent.class);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(event.getEmailPaciente());
            if (mailFrom != null && !mailFrom.isBlank()) {
                message.setFrom(mailFrom);
            }
            message.setSubject("Lembrete de consulta médica");
            String papel = event.getAgendadoPorRole();
            String nomeAgendador = event.getAgendadoPorNome();
            String quem = (papel != null && papel.equalsIgnoreCase("ENFERMEIRO")) ? "médico" : "médico";
            // se papel ENFERMEIRO, manter texto “enfermeiro”, caso contrário “médico”
            quem = (papel != null && papel.equalsIgnoreCase("ENFERMEIRO")) ? "enfermeiro" : "médico";
            String nome = (nomeAgendador != null && !nomeAgendador.isBlank()) ? nomeAgendador : event.getNomeMedico();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String dataHoraFmt = (event.getDataHora() != null) ? event.getDataHora().format(fmt) : "data não informada";
            message.setText("Olá " + event.getNomePaciente() + ",\n\nSua consulta com " + event.getNomeMedico() + " foi agendada pelo " + quem + " " + nome + " para " + dataHoraFmt + ".");
            mailSender.send(message);
            System.out.println("E-mail enviado para: " + event.getEmailPaciente());
        } catch (Exception e) {
            System.err.println("Falha ao enviar e-mail: " + e.getMessage());
        }
    }
}
