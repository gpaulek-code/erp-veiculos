package com.revendaauto.erp.repository;

import com.revendaauto.erp.entity.MovimentacaoVeiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentacaoRepository extends JpaRepository<MovimentacaoVeiculo, Long> {

    // Busca todo o histórico de um veículo, do mais recente ao mais antigo
    List<MovimentacaoVeiculo> findByVeiculoIdOrderByRegistradoEmDesc(Long veiculoId);
    void deleteByVeiculoId(Long veiculoId);
}
