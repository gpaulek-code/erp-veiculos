package com.revendaauto.erp.dto;

import com.revendaauto.erp.enums.StatusVeiculo;
import lombok.*;
import java.time.temporal.ChronoUnit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VeiculoResponseDTO {

    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private Integer ano;
    private String cor;
    private BigDecimal valor;
    private StatusVeiculo status;
    private String statusDescricao;
    private LocalDateTime criadoEm;
    private long diasNoEstoque;
    private LocalDateTime atualizadoEm;
}
