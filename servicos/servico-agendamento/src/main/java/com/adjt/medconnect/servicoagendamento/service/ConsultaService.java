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

    /**
     * Atualiza status com validação de papel/ownership.
     */
    public Consulta atualizarStatusAutorizado(long id, StatusConsulta novoStatus, String userRole, long userId) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Consulta não encontrada"));

        if ("PACIENTE".equals(userRole)) {
            throw new SecurityException("Paciente não pode atualizar status");
        }

        if ("MEDICO".equals(userRole) && consulta.getIdMedico() != userId) {
            throw new SecurityException("Médico não pode atualizar status de consulta de outro médico");
        }

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

    /**
     * Edita uma consulta existente aplicando regras de acesso por papel.
     * - MEDICO pode editar somente se for o médico responsável
     * - ENFERMEIRO e ADMIN podem editar qualquer consulta
     * - PACIENTE não pode editar
     */
    public Consulta editarConsulta(long id, Consulta consultaAtualizada, String userRole, long userId) {
        Consulta consultaExistente = consultaRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Consulta não encontrada"));

        if ("PACIENTE".equals(userRole)) {
            throw new SecurityException("Paciente não pode editar consultas");
        }

        if ("MEDICO".equals(userRole) && consultaExistente.getIdMedico() != userId) {
            throw new SecurityException("Médico não pode editar consulta de outro médico");
        }

        // Se idPaciente foi alterado, validar existência
        if (consultaAtualizada.getIdPaciente() > 0 && consultaAtualizada.getIdPaciente() != consultaExistente.getIdPaciente()) {
            usuarioRepository.findById(consultaAtualizada.getIdPaciente())
                    .orElseThrow(() -> new IllegalArgumentException("Paciente informado não existe"));
            consultaExistente.setIdPaciente(consultaAtualizada.getIdPaciente());
        }

        // Se idMedico foi alterado, validar existência
        if (consultaAtualizada.getIdMedico() > 0 && consultaAtualizada.getIdMedico() != consultaExistente.getIdMedico()) {
            usuarioRepository.findById(consultaAtualizada.getIdMedico())
                    .orElseThrow(() -> new IllegalArgumentException("Médico informado não existe"));
            consultaExistente.setIdMedico(consultaAtualizada.getIdMedico());
        }

        if (consultaAtualizada.getDataHora() != null) {
            consultaExistente.setDataHora(consultaAtualizada.getDataHora());
        }

        if (consultaAtualizada.getObservacoes() != null) {
            consultaExistente.setObservacoes(consultaAtualizada.getObservacoes());
        }

        if (consultaAtualizada.getStatus() != null) {
            consultaExistente.setStatus(consultaAtualizada.getStatus());
        }

        return consultaRepository.save(consultaExistente);
    }
}
