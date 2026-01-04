package com.adjt.medconnect.servicoagendamento.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adjt.medconnect.servicoagendamento.model.Consulta;
import com.adjt.medconnect.servicoagendamento.model.StatusConsulta;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByIdPaciente(long idPaciente);

    List<Consulta> findByIdMedico(long idMedico);

    List<Consulta> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    List<Consulta> findByStatus(StatusConsulta status);

}
