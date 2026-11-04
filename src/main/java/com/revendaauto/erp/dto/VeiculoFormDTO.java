package com.revendaauto.erp.dto;

import com.revendaauto.erp.enums.StatusVeiculo;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VeiculoFormDTO {

    private Long id;

    @NotBlank(message = "Placa é obrigatória")
    @Size(min = 7, max = 8, message = "Placa deve ter entre 7 e 8 caracteres")
    private String placa;

    @NotBlank(message = "Marca é obrigatória")
    @Size(max = 50, message = "Marca deve ter no máximo 50 caracteres")
    private String marca;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 100, message = "Modelo deve ter no máximo 100 caracteres")
    private String modelo;

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1950, message = "Ano deve ser maior que 1950")
    @Max(value = 2100, message = "Ano inválido")
    private Integer ano;

    @NotBlank(message = "Cor é obrigatória")
    @Size(max = 30, message = "Cor deve ter no máximo 30 caracteres")
    private String cor;


    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;

    @NotNull(message = "Status é obrigatório")
    private StatusVeiculo status;
}
