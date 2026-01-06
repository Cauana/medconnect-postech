package com.adjt.medconnect.servicoagendamento.service;

import com.adjt.medconnect.servicoagendamento.dto.HistoricoConsultaDTO;
import com.adjt.medconnect.servicoagendamento.dto.HistoricoConsultaResumoDTO;
import com.adjt.medconnect.servicoagendamento.dto.HistoricoConsultaDetalhadoDTO;
import com.adjt.medconnect.servicoagendamento.model.HistoricoConsulta;
import com.adjt.medconnect.servicoagendamento.model.StatusConsulta;
import com.adjt.medconnect.servicoagendamento.model.TipoAcao;
import com.adjt.medconnect.servicoagendamento.repository.HistoricoConsultaRepository;
import com.adjt.medconnect.servicoagendamento.repository.ConsultaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoricoConsultaService {
    
    private final HistoricoConsultaRepository historicoRepository;
    private final ConsultaRepository consultaRepository;
    
    public HistoricoConsultaService(
            HistoricoConsultaRepository historicoRepository,
            ConsultaRepository consultaRepository) {
        this.historicoRepository = historicoRepository;
        this.consultaRepository = consultaRepository;
    }
    
    public void registrarHistorico(Long idConsulta, Long idPaciente, Long idMedico,
                                  StatusConsulta statusAnterior, StatusConsulta statusNovo, 
                                  TipoAcao tipoAcao, Long idUsuarioResponsavel, 
                                  String tipoUsuarioResponsavel, String descricao) {
        HistoricoConsulta historico = HistoricoConsulta.builder()
                .idConsulta(idConsulta)
                .idPaciente(idPaciente)
                .idMedico(idMedico)
                .statusAnterior(statusAnterior)
                .statusNovo(statusNovo)
                .tipoAcao(tipoAcao)
                .idUsuarioResponsavel(idUsuarioResponsavel)
                .tipoUsuarioResponsavel(tipoUsuarioResponsavel)
                .descricao(descricao)
                .dataAlteracao(LocalDateTime.now())
                .build();
        
        historicoRepository.save(historico);
    }
    
    public List<HistoricoConsultaDTO> obterHistoricoConsulta(Long idConsulta) {
        return historicoRepository.findByIdConsultaOrderByDataAlteracaoDesc(idConsulta)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<HistoricoConsultaDTO> obterHistoricoPaciente(Long idPaciente) {
        return historicoRepository.findByIdPacienteOrderByDataAlteracaoDesc(idPaciente)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<HistoricoConsultaDTO> obterHistoricoMedico(Long idMedico) {
        return historicoRepository.findByIdMedicoOrderByDataAlteracaoDesc(idMedico)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<HistoricoConsultaDTO> obterHistoricoConsultaPorPeriodo(Long idConsulta, 
                                                                       LocalDateTime dataInicio, 
                                                                       LocalDateTime dataFim) {
        return historicoRepository.findConsultaHistoricoByPeriodo(idConsulta, dataInicio, dataFim)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<HistoricoConsultaDTO> obterHistoricoPacientePorPeriodo(Long idPaciente, 
                                                                       LocalDateTime dataInicio, 
                                                                       LocalDateTime dataFim) {
        return historicoRepository.findPacienteHistoricoByPeriodo(idPaciente, dataInicio, dataFim)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<HistoricoConsultaDTO> obterHistoricoMedicoPorPeriodo(Long idMedico, 
                                                                     LocalDateTime dataInicio, 
                                                                     LocalDateTime dataFim) {
        return historicoRepository.findMedicoHistoricoByPeriodo(idMedico, dataInicio, dataFim)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<HistoricoConsultaResumoDTO> obterHistoricoConsultaResumo(Long idConsulta) {
        return historicoRepository.findByIdConsultaOrderByDataAlteracaoDesc(idConsulta)
                .stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }
    
    public List<HistoricoConsultaDetalhadoDTO> obterHistoricoConsultaDetalhado(Long idConsulta) {
        return historicoRepository.findByIdConsultaOrderByDataAlteracaoDesc(idConsulta)
                .stream()
                .map(this::toDetalhadoDTO)
                .collect(Collectors.toList());
    }
    
    public void validarAcessoPaciente(Long idUsuario, Long idPaciente) {
        if (!idUsuario.equals(idPaciente)) {
            throw new SecurityException("Paciente não autorizado a acessar histórico de outro paciente");
        }
    }

    public void validarAcessoMedico(Long idMedico, Long idConsulta) {
        consultaRepository.findById(idConsulta).ifPresentOrElse(
                consulta -> {
                    if (!Long.valueOf(consulta.getIdMedico()).equals(idMedico)) {
                        throw new SecurityException(
                                "Médico não autorizado a acessar histórico de consulta de outro médico"
                        );
                    }
                },
                () -> {
                    throw new RuntimeException("Consulta não encontrada");
                }
        );
    }
    
    private HistoricoConsultaDTO toDTO(HistoricoConsulta entity) {
        return HistoricoConsultaDTO.builder()
                .id(entity.getId())
                .idConsulta(entity.getIdConsulta())
                .idPaciente(entity.getIdPaciente())
                .idMedico(entity.getIdMedico())
                .statusAnterior(entity.getStatusAnterior().toString())
                .statusNovo(entity.getStatusNovo().toString())
                .tipoAcao(entity.getTipoAcao().toString())
                .idUsuarioResponsavel(entity.getIdUsuarioResponsavel())
                .tipoUsuarioResponsavel(entity.getTipoUsuarioResponsavel())
                .descricao(entity.getDescricao())
                .dataAlteracao(entity.getDataAlteracao())
                .build();
    }
    
    private HistoricoConsultaResumoDTO toResumoDTO(HistoricoConsulta entity) {
        return HistoricoConsultaResumoDTO.builder()
                .id(entity.getId())
                .idConsulta(entity.getIdConsulta())
                .statusNovo(entity.getStatusNovo().toString())
                .dataAlteracao(entity.getDataAlteracao())
                .build();
    }
    
    private HistoricoConsultaDetalhadoDTO toDetalhadoDTO(HistoricoConsulta entity) {
        return HistoricoConsultaDetalhadoDTO.builder()
                .id(entity.getId())
                .idConsulta(entity.getIdConsulta())
                .idPaciente(entity.getIdPaciente())
                .idMedico(entity.getIdMedico())
                .statusAnterior(entity.getStatusAnterior().toString())
                .statusNovo(entity.getStatusNovo().toString())
                .tipoAcao(entity.getTipoAcao().toString())
                .idUsuarioResponsavel(entity.getIdUsuarioResponsavel())
                .tipoUsuarioResponsavel(entity.getTipoUsuarioResponsavel())
                .descricao(entity.getDescricao())
                .dataAlteracao(entity.getDataAlteracao())
                .build();
    }
}
