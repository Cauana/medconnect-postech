package com.adjt.medconnect.serviconotificacao.kafka;

import com.adjt.medconnect.serviconotificacao.ServicoNotificacaoApplication;
import com.adjt.medconnect.serviconotificacao.event.ConsultaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {ServicoNotificacaoApplication.class, NotificacaoConsumerKafkaIT.TestConfig.class})
@EmbeddedKafka(partitions = 1, topics = {"agendamento-topic"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.mail.username=wellingtonfc95@gmail.com"
})
public class NotificacaoConsumerKafkaIT {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.kafka.test.EmbeddedKafkaBroker broker;

    @Test
    void deveConsumirEventoDoKafkaEEnviarEmail() throws Exception {
        Map<String, Object> producerProps = org.springframework.kafka.test.utils.KafkaTestUtils.producerProps(broker);
        DefaultKafkaProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(pf);
        kafkaTemplate.setDefaultTopic("agendamento-topic");

        ConsultaEvent event = new ConsultaEvent();
        event.setIdConsulta(2L);
        event.setEmailPaciente("wellingtonfc95@gmail.com");
        event.setNomePaciente("Paciente 2");
        event.setNomeMedico("Dr. Segundo");
        event.setStatus("AGENDADA");
        event.setAgendadoPorRole("MEDICO");
        event.setAgendadoPorNome("Dr. Segundo");
        String json = objectMapper.writeValueAsString(event);

        kafkaTemplate.sendDefault(json);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getTo()[0]).isEqualTo("wellingtonfc95@gmail.com");
        assertThat(captor.getValue().getFrom()).isEqualTo("wellingtonfc95@gmail.com");
        assertThat(captor.getValue().getText()).contains("consulta com Dr. Segundo foi agendada pelo médico Dr. Segundo");
    }

    @Configuration
    static class TestConfig {
        @Bean
        public JavaMailSender mailSender() {
            return mock(JavaMailSender.class);
        }
    }
}
