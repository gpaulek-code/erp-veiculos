package com.revendaauto.erp;

import com.revendaauto.erp.controller.DashboardController;
import com.revendaauto.erp.controller.MovimentacaoController;
import com.revendaauto.erp.controller.VeiculoController;
import com.revendaauto.erp.repository.MovimentacaoRepository;
import com.revendaauto.erp.repository.VeiculoRepository;
import com.revendaauto.erp.service.MovimentacaoService;
import com.revendaauto.erp.service.VeiculoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("ErpVeiculosApplication — Teste de Contexto")
class ErpVeiculosApplicationTest {

    @Autowired private VeiculoController veiculoController;
    @Autowired private DashboardController dashboardController;
    @Autowired private MovimentacaoController movimentacaoController;
    @Autowired private VeiculoService veiculoService;
    @Autowired private MovimentacaoService movimentacaoService;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private MovimentacaoRepository movimentacaoRepository;

    @Test
    @DisplayName("Contexto Spring deve subir sem erros e todos os beans devem ser injetados")
    void contextLoads() {
        assertThat(veiculoController).isNotNull();
        assertThat(dashboardController).isNotNull();
        assertThat(movimentacaoController).isNotNull();
        assertThat(veiculoService).isNotNull();
        assertThat(movimentacaoService).isNotNull();
        assertThat(veiculoRepository).isNotNull();
        assertThat(movimentacaoRepository).isNotNull();
    }
}
