package com.adjt.medconnect.servicoagendamento.service;

import com.adjt.medconnect.servicoagendamento.model.Consulta;
import com.adjt.medconnect.servicoagendamento.model.StatusConsulta;
import com.adjt.medconnect.servicoagendamento.model.Usuario;
import com.adjt.medconnect.servicoagendamento.repository.ConsultaRepository;
import com.adjt.medconnect.servicoagendamento.repository.UsuarioRepository;
import com.adjt.medconnect.servicoagendamento.event.ConsultaEvent;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final UsuarioRepository usuarioRepository;
    private final KafkaTemplate<String, ConsultaEvent> kafkaTemplate;

    public ConsultaService(
            ConsultaRepository consultaRepository,
            UsuarioRepository usuarioRepository,
            KafkaTemplate<String, ConsultaEvent> kafkaTemplate) {
        this.consultaRepository = consultaRepository;
        this.usuarioRepository = usuarioRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Consulta criar(Consulta consulta) {
        consulta.setStatus(StatusConsulta.AGENDADA);
        consulta.setDataHora(LocalDateTime.now());
        Consulta salva = consultaRepository.save(consulta);
        Usuario paciente = usuarioRepository.findById(salva.getIdPaciente()).orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
        Usuario medico = usuarioRepository.findById(salva.getIdMedico()).orElseThrow(() -> new RuntimeException("Médico não encontrado"));
        String email = paciente.getEmail();

        // Envia evento Kafka com as informações básicas
        ConsultaEvent event = new ConsultaEvent(
                salva.getId(),
                paciente.getEmail(),
                paciente.getNome(),
                medico.getNome(),
                salva.getDataHora(),
                salva.getStatus().name()
        );

        kafkaTemplate.send("agendamento-topic", event);
        return salva;
    }

    public List<Consulta> listarTodas() {
        return consultaRepository.findAll();
    }

    public Optional<Consulta> buscarPorId(long id) {
        return consultaRepository.findById(id);
    }

    public Consulta atualizarStatus(long id, StatusConsulta novoStatus) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consulta não encontrada"));
        consulta.setStatus(novoStatus);
        return consultaRepository.save(consulta);
    }

    public void deletar(long id) {
        consultaRepository.deleteById(id);
    }

    public Consulta agendarConsulta(Consulta consulta) {
        Usuario paciente = usuarioRepository.findById(consulta.getIdPaciente())
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
        Usuario medico = usuarioRepository.findById(consulta.getIdMedico())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado"));

        Consulta salva = consultaRepository.save(consulta);

        // Cria e envia o evento Kafka
        ConsultaEvent event = new ConsultaEvent(
                salva.getId(),
                paciente.getEmail(),
                paciente.getNome(),
                medico.getNome(),
                consulta.getDataHora(),
                salva.getStatus().name()
        );

        kafkaTemplate.send("agendamento-topic", event);
        return salva;
    }
}
