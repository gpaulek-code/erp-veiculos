package com.revendaauto.erp.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardDTO {

    private long totalVeiculos;
    private long totalVendidos;
    private long totalManutencao;
    private long totalLoja1;
    private long totalLoja2;
    private long totalLoja3;
    private BigDecimal valorTotalEstoque;

    // Alertas
    private List<VeiculoResponseDTO> veiculosManutencaoAlerta;   // >7 dias em manutenção
    private List<VeiculoResponseDTO> veiculosEstoqueAlerta;      // >30 dias no estoque sem ser vendido
}
