package com.adjt.medconnect.servicoagendamento.service;

import com.adjt.medconnect.servicoagendamento.model.Consulta;
import com.adjt.medconnect.servicoagendamento.model.StatusConsulta;
import com.adjt.medconnect.servicoagendamento.model.Usuario;
import com.adjt.medconnect.servicoagendamento.model.TipoAcao;
import com.adjt.medconnect.servicoagendamento.repository.ConsultaRepository;
import com.adjt.medconnect.servicoagendamento.repository.UsuarioRepository;
import com.adjt.medconnect.servicoagendamento.event.ConsultaEvent;

import jakarta.transaction.Transactional;
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
    private final HistoricoConsultaService historicoService;

    public ConsultaService(
            ConsultaRepository consultaRepository,
            UsuarioRepository usuarioRepository,
            KafkaTemplate<String, ConsultaEvent> kafkaTemplate,
            HistoricoConsultaService historicoService) {
        this.consultaRepository = consultaRepository;
        this.usuarioRepository = usuarioRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.historicoService = historicoService;
    }

    @Transactional
    public Consulta criar(Consulta consulta) {

        Usuario paciente = usuarioRepository.findById(consulta.getIdPaciente())
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));

        Usuario medico = usuarioRepository.findById(consulta.getIdMedico())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado"));

        consulta.setStatus(StatusConsulta.AGENDADA);
        consulta.setDataHora(LocalDateTime.now());

        Consulta salva = consultaRepository.save(consulta); // ✅ AGORA SIM

        // Histórico
        historicoService.registrarHistorico(
                salva.getId(),
                salva.getIdPaciente(),
                salva.getIdMedico(),
                null,
                StatusConsulta.AGENDADA,
                TipoAcao.CRIACAO,
                salva.getIdMedico(),
                "MEDICO",
                "Consulta criada"
        );

        // Evento Kafka
        ConsultaEvent event = new ConsultaEvent(
                salva.getId(),
                paciente.getEmail(),
                paciente.getNome(),
                medico.getNome(),
                salva.getDataHora(),
                salva.getStatus().name(),
                "MEDICO",
                medico.getNome()
        );

        try {
            kafkaTemplate.send("agendamento-topic", event);
        } catch (Exception ex) {
            // não bloqueia a transação
        }

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
        StatusConsulta statusAnterior = consulta.getStatus();
        consulta.setStatus(novoStatus);
        Consulta atualizada = consultaRepository.save(consulta);
        
        // Registra no histórico
        historicoService.registrarHistorico(
            id,
            consulta.getIdPaciente(),
            consulta.getIdMedico(),
            statusAnterior,
            novoStatus,
            TipoAcao.ALTERACAO_DE_STATUS,
            1L, // ID padrão do sistema
            "SISTEMA",
            "Status alterado de " + statusAnterior + " para " + novoStatus
        );
        
        return atualizada;
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

        StatusConsulta statusAnterior = consulta.getStatus();
        consulta.setStatus(novoStatus);
        Consulta atualizada = consultaRepository.save(consulta);
        
        // Registra no histórico
        historicoService.registrarHistorico(
            id,
            consulta.getIdPaciente(),
            consulta.getIdMedico(),
            statusAnterior,
            novoStatus,
            TipoAcao.ALTERACAO_DE_STATUS,
            userId,
            userRole,
            "Status alterado de " + statusAnterior + " para " + novoStatus + " por " + userRole
        );
        
        return atualizada;
    }

    public void deletar(long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consulta não encontrada"));
        
        // Registra no histórico antes de deletar
        historicoService.registrarHistorico(
            id,
            consulta.getIdPaciente(),
            consulta.getIdMedico(),
            consulta.getStatus(),
            consulta.getStatus(),
            TipoAcao.CANCELAMENTO,
            1L, // ID padrão do sistema
            "SISTEMA",
            "Consulta cancelada/deletada"
        );
        
        consultaRepository.deleteById(id);
    }

    public Consulta agendarConsulta(Consulta consulta, String userRole, long userId) {
        Usuario paciente = usuarioRepository.findById(consulta.getIdPaciente())
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
        Usuario medico = usuarioRepository.findById(consulta.getIdMedico())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado"));

        Consulta salva = consultaRepository.save(consulta);

        // Registra no histórico
        historicoService.registrarHistorico(
            salva.getId(),
            salva.getIdPaciente(),
            salva.getIdMedico(),
            null,
            salva.getStatus(),
            TipoAcao.CRIACAO,
            salva.getIdMedico(),
            "MEDICO",
            "Consulta agendada para " + salva.getDataHora()
        );

        // Cria e envia o evento Kafka
        String agendadoPorRole = (userRole == null || userRole.isBlank()) ? "MEDICO" : userRole;
        String agendadoPorNome;
        if ("MEDICO".equalsIgnoreCase(agendadoPorRole)) {
            agendadoPorNome = medico.getNome();
        } else {
            Usuario agendador = usuarioRepository.findById(userId)
                    .orElse(null);
            agendadoPorNome = agendador != null ? agendador.getNome() : medico.getNome();
        }

        ConsultaEvent event = new ConsultaEvent(
                salva.getId(),
                paciente.getEmail(),
                paciente.getNome(),
                medico.getNome(),
                consulta.getDataHora(),
                salva.getStatus().name(),
                agendadoPorRole,
                agendadoPorNome
        );

        try {
            kafkaTemplate.send("agendamento-topic", event);
        } catch (Exception ex) {
            // swallow Kafka errors to not block HTTP creation
        }
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

        StringBuilder descricaoAlteracao = new StringBuilder("Consulta editada: ");
        boolean houveAlteracao = false;

        // Se idPaciente foi alterado, validar existência
        if (consultaAtualizada.getIdPaciente() > 0 && consultaAtualizada.getIdPaciente() != consultaExistente.getIdPaciente()) {
            usuarioRepository.findById(consultaAtualizada.getIdPaciente())
                    .orElseThrow(() -> new IllegalArgumentException("Paciente informado não existe"));
            descricaoAlteracao.append("Paciente alterado; ");
            consultaExistente.setIdPaciente(consultaAtualizada.getIdPaciente());
            houveAlteracao = true;
        }

        // Se idMedico foi alterado, validar existência
        if (consultaAtualizada.getIdMedico() > 0 && consultaAtualizada.getIdMedico() != consultaExistente.getIdMedico()) {
            usuarioRepository.findById(consultaAtualizada.getIdMedico())
                    .orElseThrow(() -> new IllegalArgumentException("Médico informado não existe"));
            descricaoAlteracao.append("Médico alterado; ");
            consultaExistente.setIdMedico(consultaAtualizada.getIdMedico());
            houveAlteracao = true;
        }

        if (consultaAtualizada.getDataHora() != null && !consultaAtualizada.getDataHora().equals(consultaExistente.getDataHora())) {
            descricaoAlteracao.append("Data/Hora alterada; ");
            consultaExistente.setDataHora(consultaAtualizada.getDataHora());
            houveAlteracao = true;
        }

        if (consultaAtualizada.getObservacoes() != null && !consultaAtualizada.getObservacoes().equals(consultaExistente.getObservacoes())) {
            descricaoAlteracao.append("Observações alteradas; ");
            consultaExistente.setObservacoes(consultaAtualizada.getObservacoes());
            houveAlteracao = true;
        }

        if (consultaAtualizada.getStatus() != null && consultaAtualizada.getStatus() != consultaExistente.getStatus()) {
            descricaoAlteracao.append("Status alterado; ");
            consultaExistente.setStatus(consultaAtualizada.getStatus());
            houveAlteracao = true;
        }

        Consulta salva = consultaRepository.save(consultaExistente);
        
        // Registra no histórico se houve alteração
        if (houveAlteracao) {
            historicoService.registrarHistorico(
                id,
                consultaExistente.getIdPaciente(),
                consultaExistente.getIdMedico(),
                consultaExistente.getStatus(),
                consultaExistente.getStatus(),
                TipoAcao.EDICAO,
                userId,
                userRole,
                descricaoAlteracao.toString()
            );
        }
        
        return salva;
    }
}
