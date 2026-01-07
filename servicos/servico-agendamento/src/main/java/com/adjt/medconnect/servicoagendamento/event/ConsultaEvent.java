package com.adjt.medconnect.servicoagendamento.event;

import java.time.LocalDateTime;

import com.adjt.medconnect.servicoagendamento.model.Usuario;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConsultaEvent {
    private long idConsulta;
    private String emailPaciente;
    private String nomePaciente;
    private String nomeMedico;
    private LocalDateTime dataHora;
    private String status;
    private String agendadoPorRole;
    private String agendadoPorNome;

    
}
