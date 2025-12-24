package com.adjt.medconnect.servicoagendamento.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private long idPaciente;
    
    @Column(nullable = false)
    private long idMedico;

    @Column(nullable = false)
    private LocalDateTime dataHora;
    
    @Column(nullable = true)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    private StatusConsulta status;

}