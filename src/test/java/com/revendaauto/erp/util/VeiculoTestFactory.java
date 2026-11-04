package com.revendaauto.erp.util;

import com.revendaauto.erp.dto.VeiculoFormDTO;
import com.revendaauto.erp.entity.Veiculo;
import com.revendaauto.erp.enums.StatusVeiculo;

import java.math.BigDecimal;

/**
 * Fábrica de objetos de teste para evitar repetição nos testes.
 */
public class VeiculoTestFactory {

    public static Veiculo veiculoEntidade(String placa, StatusVeiculo status) {
        return Veiculo.builder()
                .placa(placa)
                .marca("Volkswagen")
                .modelo("Gol 1.0")
                .ano(2022)
                .cor("Branco")
                .valor(new BigDecimal("45000.00"))
                .status(status)
                .build();
    }

    public static VeiculoFormDTO veiculoFormDTO(String placa, StatusVeiculo status) {
        VeiculoFormDTO dto = new VeiculoFormDTO();
        dto.setPlaca(placa);
        dto.setMarca("Volkswagen");
        dto.setModelo("Gol 1.0");
        dto.setAno(2022);
        dto.setCor("Branco");
        dto.setValor(new BigDecimal("45000.00"));
        dto.setStatus(status);
        return dto;
    }
}
