package com.revendaauto.erp.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ImportacaoController — Testes Unitários")
class ImportacaoControllerTest {

    @Test
    @DisplayName("Deve detectar a marca corretamente com base no modelo/veículo")
    void deveDetectarMarcaComBaseNoModelo() {
        // Chevrolet
        assertThat(ImportacaoController.detectarMarca("Astra GL")).isEqualTo("Chevrolet");
        assertThat(ImportacaoController.detectarMarca("Onix 1.0 Turbo")).isEqualTo("Chevrolet");
        
        // Volkswagen
        assertThat(ImportacaoController.detectarMarca("Gol 1.0")).isEqualTo("Volkswagen");
        assertThat(ImportacaoController.detectarMarca("T-Cross Comfortline")).isEqualTo("Volkswagen");
        
        // Fiat
        assertThat(ImportacaoController.detectarMarca("Uno Mille")).isEqualTo("Fiat");
        assertThat(ImportacaoController.detectarMarca("Toro Freedom")).isEqualTo("Fiat");
        
        // Ford
        assertThat(ImportacaoController.detectarMarca("Ka SE 1.0")).isEqualTo("Ford");
        assertThat(ImportacaoController.detectarMarca("Mustang GT")).isEqualTo("Ford");
        
        // Toyota
        assertThat(ImportacaoController.detectarMarca("Corolla Altis")).isEqualTo("Toyota");
        assertThat(ImportacaoController.detectarMarca("Hilux SRX")).isEqualTo("Toyota");
        
        // Honda
        assertThat(ImportacaoController.detectarMarca("Civic Touring")).isEqualTo("Honda");
        assertThat(ImportacaoController.detectarMarca("HR-V EXL")).isEqualTo("Honda");
        
        // Outros / Não cadastrados
        assertThat(ImportacaoController.detectarMarca("Carro Desconhecido")).isEqualTo("Não informado");
        assertThat(ImportacaoController.detectarMarca("")).isEqualTo("Não informado");
        assertThat(ImportacaoController.detectarMarca(null)).isEqualTo("Não informado");
    }
}
