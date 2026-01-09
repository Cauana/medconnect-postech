package com.adjt.medconnect.serviconotificacao.kafka;

import com.adjt.medconnect.serviconotificacao.event.ConsultaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class NotificacaoConsumerTest {

    @Test
    void deveEnviarEmailAoConsumirEvento() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        ObjectMapper objectMapper = new ObjectMapper();
        NotificacaoConsumer consumer = new NotificacaoConsumer(mailSender, objectMapper);

        ConsultaEvent event = new ConsultaEvent();
        event.setIdConsulta(1L);
        event.setEmailPaciente("paciente@example.com");
        event.setNomePaciente("Paciente Teste");
        event.setNomeMedico("Dr. Médico");
        event.setStatus("AGENDADA");
        event.setAgendadoPorRole("MEDICO");
        event.setAgendadoPorNome("Dr. Médico");
        String json = objectMapper.writeValueAsString(event);

        consumer.consumirMensagem(json);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertEquals("paciente@example.com", captor.getValue().getTo()[0]);
        assertEquals(true, captor.getValue().getText().contains("consulta com Dr. Médico foi agendada pelo médico Dr. Médico"));
    }
}
