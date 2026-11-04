package com.revendaauto.erp.service;

import com.revendaauto.erp.dto.MovimentacaoResponseDTO;
import com.revendaauto.erp.enums.StatusVeiculo;

import java.util.List;

public interface MovimentacaoService {

    // Registra uma movimentação — chamado internamente ao salvar/atualizar veículo
    void registrar(Long veiculoId, StatusVeiculo statusAnterior, StatusVeiculo statusNovo, String observacao);

    // Lista todo o histórico de um veículo específico
    List<MovimentacaoResponseDTO> listarPorVeiculo(Long veiculoId);

    // Lista as últimas movimentações de todos os veículos (para o dashboard)
    List<MovimentacaoResponseDTO> listarRecentes(int quantidade);
}
