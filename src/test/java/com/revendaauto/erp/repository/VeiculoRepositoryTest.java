package com.revendaauto.erp.repository;

import com.revendaauto.erp.entity.Veiculo;
import com.revendaauto.erp.enums.StatusVeiculo;
import com.revendaauto.erp.util.VeiculoTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("VeiculoRepository — Testes de Repositório")
class VeiculoRepositoryTest {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @BeforeEach
    void setUp() {
        veiculoRepository.deleteAll();

        veiculoRepository.save(VeiculoTestFactory.veiculoEntidade("ABC1D23", StatusVeiculo.LOJA_1));
        veiculoRepository.save(VeiculoTestFactory.veiculoEntidade("BCD2E34", StatusVeiculo.LOJA_2));
        veiculoRepository.save(VeiculoTestFactory.veiculoEntidade("CDE3F45", StatusVeiculo.MANUTENCAO));
        veiculoRepository.save(VeiculoTestFactory.veiculoEntidade("DEF4G56", StatusVeiculo.VENDIDO));
        veiculoRepository.save(VeiculoTestFactory.veiculoEntidade("EFG5H67", StatusVeiculo.VENDIDO));
    }

    // ═══════════════════════════════════════════════════════════
    // findByPlacaIgnoreCase
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("findByPlacaIgnoreCase()")
    class FindByPlaca {

        @Test
        @DisplayName("Deve encontrar veículo por placa em maiúsculo")
        void deveEncontrarPorPlacaMaiusculo() {
            Optional<Veiculo> resultado = veiculoRepository.findByPlacaIgnoreCase("ABC1D23");
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getPlaca()).isEqualTo("ABC1D23");
        }

        @Test
        @DisplayName("Deve encontrar veículo por placa em minúsculo")
        void deveEncontrarPorPlacaMinusculo() {
            Optional<Veiculo> resultado = veiculoRepository.findByPlacaIgnoreCase("abc1d23");
            assertThat(resultado).isPresent();
        }

        @Test
        @DisplayName("Deve retornar vazio para placa inexistente")
        void deveRetornarVazioParaPlacaInexistente() {
            Optional<Veiculo> resultado = veiculoRepository.findByPlacaIgnoreCase("ZZZ9Z99");
            assertThat(resultado).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // existsByPlacaIgnoreCaseAndIdNot
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("existsByPlacaIgnoreCaseAndIdNot()")
    class ExistsByPlaca {

        @Test
        @DisplayName("Deve retornar true quando placa pertence a outro veículo")
        void deveRetornarTrueParaPlacaDuplicada() {
            Veiculo v = veiculoRepository.findByPlacaIgnoreCase("ABC1D23").orElseThrow();
            // Buscamos a placa do veículo ABC1D23 mas excluímos o BCD2E34 — deve achar conflito
            Veiculo outro = veiculoRepository.findByPlacaIgnoreCase("BCD2E34").orElseThrow();

            boolean existe = veiculoRepository.existsByPlacaIgnoreCaseAndIdNot("ABC1D23", outro.getId());
            assertThat(existe).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando placa pertence ao próprio veículo")
        void deveRetornarFalseParaProprioVeiculo() {
            Veiculo v = veiculoRepository.findByPlacaIgnoreCase("ABC1D23").orElseThrow();

            boolean existe = veiculoRepository.existsByPlacaIgnoreCaseAndIdNot("ABC1D23", v.getId());
            assertThat(existe).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // buscarComFiltros
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("buscarComFiltros()")
    class BuscarComFiltros {

        @Test
        @DisplayName("Deve retornar todos os veículos quando filtros forem nulos")
        void deveRetornarTodosComFiltrosNulos() {
            Page<Veiculo> page = veiculoRepository.buscarComFiltros(null, null, null, PageRequest.of(0, 10));
            assertThat(page.getTotalElements()).isEqualTo(5);
        }

        @Test
        @DisplayName("Deve filtrar por placa parcial")
        void deveFiltrarPorPlaca() {
            Page<Veiculo> page = veiculoRepository.buscarComFiltros("%ABC%", null, null, PageRequest.of(0, 10));
            assertThat(page.getTotalElements()).isEqualTo(1);
            assertThat(page.getContent().get(0).getPlaca()).isEqualTo("ABC1D23");
        }

        @Test
        @DisplayName("Deve filtrar por modelo parcial ignorando case")
        void deveFiltrarPorModelo() {
            Page<Veiculo> page = veiculoRepository.buscarComFiltros(null, "%GOL%", null, PageRequest.of(0, 10));
            // Todos os veículos da factory têm modelo "Gol 1.0"
            assertThat(page.getTotalElements()).isEqualTo(5);
        }

        @Test
        @DisplayName("Deve filtrar por status VENDIDO")
        void deveFiltrarPorStatus() {
            Page<Veiculo> page = veiculoRepository.buscarComFiltros(null, null, StatusVeiculo.VENDIDO, PageRequest.of(0, 10));
            assertThat(page.getTotalElements()).isEqualTo(2);
            assertThat(page.getContent()).allMatch(v -> v.getStatus() == StatusVeiculo.VENDIDO);
        }

        @Test
        @DisplayName("Deve filtrar combinando placa e status")
        void deveFiltrarCombinandoPlacaEStatus() {
            Page<Veiculo> page = veiculoRepository.buscarComFiltros("%DEF%", null, StatusVeiculo.VENDIDO, PageRequest.of(0, 10));
            assertThat(page.getTotalElements()).isEqualTo(1);
            assertThat(page.getContent().get(0).getPlaca()).isEqualTo("DEF4G56");
        }

        @Test
        @DisplayName("Deve retornar vazio quando nenhum veículo bater com os filtros")
        void deveRetornarVazioSemResultados() {
            Page<Veiculo> page = veiculoRepository.buscarComFiltros("%ZZZ%", null, null, PageRequest.of(0, 10));
            assertThat(page.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Deve respeitar paginação")
        void deveRespeitarPaginacao() {
            Page<Veiculo> page = veiculoRepository.buscarComFiltros(null, null, null, PageRequest.of(0, 2));
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalElements()).isEqualTo(5);
            assertThat(page.getTotalPages()).isEqualTo(3);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // countByStatus
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("countByStatus()")
    class CountByStatus {

        @Test
        @DisplayName("Deve contar corretamente veículos vendidos")
        void deveContarVendidos() {
            assertThat(veiculoRepository.countByStatus(StatusVeiculo.VENDIDO)).isEqualTo(2);
        }

        @Test
        @DisplayName("Deve contar corretamente veículos em manutenção")
        void deveContarManutencao() {
            assertThat(veiculoRepository.countByStatus(StatusVeiculo.MANUTENCAO)).isEqualTo(1);
        }

        @Test
        @DisplayName("Deve retornar zero para status sem veículos")
        void deveRetornarZeroParaStatusSemVeiculos() {
            assertThat(veiculoRepository.countByStatus(StatusVeiculo.LOJA_3)).isZero();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // somarValorEstoqueAtivo
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("somarValorEstoqueAtivo()")
    class SomarValor {

        @Test
        @DisplayName("Deve somar valor dos veículos excluindo os vendidos")
        void deveSomarExcluindoVendidos() {
            // 3 veículos não vendidos × R$ 45.000 = R$ 135.000
            BigDecimal total = veiculoRepository.somarValorEstoqueAtivo(StatusVeiculo.VENDIDO);
            assertThat(total).isEqualByComparingTo(new BigDecimal("135000.00"));
        }
    }
}
