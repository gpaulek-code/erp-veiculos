package com.revendaauto.erp.service;

import com.revendaauto.erp.dto.DashboardDTO;
import com.revendaauto.erp.dto.VeiculoFormDTO;
import com.revendaauto.erp.dto.VeiculoResponseDTO;
import com.revendaauto.erp.enums.StatusVeiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VeiculoService {

    VeiculoResponseDTO salvar(VeiculoFormDTO dto);

    VeiculoResponseDTO atualizar(Long id, VeiculoFormDTO dto);

    VeiculoResponseDTO buscarPorId(Long id);

    Page<VeiculoResponseDTO> listar(String placa, String modelo, StatusVeiculo status, Pageable pageable);

    void excluir(Long id);

    DashboardDTO obterDadosDashboard();
}
