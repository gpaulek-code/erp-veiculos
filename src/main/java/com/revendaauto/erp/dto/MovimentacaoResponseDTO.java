package com.revendaauto.erp.dto;

import com.revendaauto.erp.enums.StatusVeiculo;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoResponseDTO {

    private Long id;
    private Long veiculoId;
    private String veiculoPlaca;
    private String veiculoModelo;
    private StatusVeiculo statusAnterior;
    private String statusAnteriorDescricao;
    private StatusVeiculo statusNovo;
    private String statusNovoDescricao;
    private String observacao;
    private LocalDateTime registradoEm;
}
