package com.revendaauto.erp.dto;

import com.revendaauto.erp.enums.StatusVeiculo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoFormDTO {

    @NotNull(message = "O novo status é obrigatório")
    private StatusVeiculo statusNovo;

    @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
    private String observacao;
}
