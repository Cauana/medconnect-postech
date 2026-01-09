package com.adjt.medconnect.serviconotificacao.event;

import java.time.LocalDateTime;

public class ConsultaEvent {
    private long idConsulta;
    private String emailPaciente;
    private String nomePaciente;
    private String nomeMedico;
    private LocalDateTime dataHora;
    private String status;
    private String agendadoPorRole;
    private String agendadoPorNome;

    public ConsultaEvent() {}

    public long getIdConsulta() {
        return idConsulta;
    }

    public void setIdConsulta(long idConsulta) {
        this.idConsulta = idConsulta;
    }

    public String getEmailPaciente() {
        return emailPaciente;
    }

    public void setEmailPaciente(String emailPaciente) {
        this.emailPaciente = emailPaciente;
    }

    public String getNomePaciente() {
        return nomePaciente;
    }

    public void setNomePaciente(String nomePaciente) {
        this.nomePaciente = nomePaciente;
    }

    public String getNomeMedico() {
        return nomeMedico;
    }

    public void setNomeMedico(String nomeMedico) {
        this.nomeMedico = nomeMedico;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getAgendadoPorRole() {
        return agendadoPorRole;
    }
    
    public void setAgendadoPorRole(String agendadoPorRole) {
        this.agendadoPorRole = agendadoPorRole;
    }
    
    public String getAgendadoPorNome() {
        return agendadoPorNome;
    }
    
    public void setAgendadoPorNome(String agendadoPorNome) {
        this.agendadoPorNome = agendadoPorNome;
    }
}
