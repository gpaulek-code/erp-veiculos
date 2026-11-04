package com.revendaauto.erp.repository;

import com.revendaauto.erp.entity.MovimentacaoVeiculo;
import com.revendaauto.erp.entity.Veiculo;
import com.revendaauto.erp.enums.StatusVeiculo;
import com.revendaauto.erp.util.VeiculoTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("MovimentacaoRepository — Testes de Repositório")
class MovimentacaoRepositoryTest {

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private TestEntityManager em;

    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        veiculo = veiculoRepository.save(
                VeiculoTestFactory.veiculoEntidade("ABC1D23", StatusVeiculo.LOJA_1));

        // Salva 3 movimentações com timestamps distintos e ordenados
        movimentacaoRepository.save(movimentacao(null, StatusVeiculo.LOJA_1, "Cadastro", LocalDateTime.now().minusMinutes(10)));
        movimentacaoRepository.save(movimentacao(StatusVeiculo.LOJA_1, StatusVeiculo.MANUTENCAO, "Revisão", LocalDateTime.now().minusMinutes(5)));
        movimentacaoRepository.save(movimentacao(StatusVeiculo.MANUTENCAO, StatusVeiculo.VENDIDO, "Venda realizada", LocalDateTime.now()));

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("Deve retornar movimentações do veículo ordenadas da mais recente para a mais antiga")
    void deveRetornarOrdenadoPorDataDesc() {
        List<MovimentacaoVeiculo> resultado =
                movimentacaoRepository.findByVeiculoIdOrderByRegistradoEmDesc(veiculo.getId());

        assertThat(resultado).hasSize(3);
        assertThat(resultado.get(0).getStatusNovo()).isEqualTo(StatusVeiculo.VENDIDO);
        assertThat(resultado.get(1).getStatusNovo()).isEqualTo(StatusVeiculo.MANUTENCAO);
        assertThat(resultado.get(2).getStatusNovo()).isEqualTo(StatusVeiculo.LOJA_1);
    }

    @Test
    @DisplayName("Deve retornar lista vazia para veículo sem movimentações")
    void deveRetornarVazioParaVeiculoSemMovimentacoes() {
        Veiculo outro = veiculoRepository.save(
                VeiculoTestFactory.veiculoEntidade("ZZZ9Z99", StatusVeiculo.LOJA_2));

        List<MovimentacaoVeiculo> resultado =
                movimentacaoRepository.findByVeiculoIdOrderByRegistradoEmDesc(outro.getId());

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve persistir observação corretamente")
    void devePersistirObservacao() {
        List<MovimentacaoVeiculo> resultado =
                movimentacaoRepository.findByVeiculoIdOrderByRegistradoEmDesc(veiculo.getId());

        assertThat(resultado.get(0).getObservacao()).isEqualTo("Venda realizada");
        assertThat(resultado.get(2).getObservacao()).isEqualTo("Cadastro");
    }

    @Test
    @DisplayName("Deve persistir statusAnterior nulo para cadastro inicial")
    void devePersistirStatusAnteriorNulo() {
        List<MovimentacaoVeiculo> resultado =
                movimentacaoRepository.findByVeiculoIdOrderByRegistradoEmDesc(veiculo.getId());

        MovimentacaoVeiculo cadastro = resultado.get(2); // o mais antigo
        assertThat(cadastro.getStatusAnterior()).isNull();
        assertThat(cadastro.getStatusNovo()).isEqualTo(StatusVeiculo.LOJA_1);
    }

    @Test
    @DisplayName("Deve não retornar movimentações de outro veículo")
    void naoDeveRetornarMovimentacoesDeOutroVeiculo() {
        Veiculo outro = veiculoRepository.save(
                VeiculoTestFactory.veiculoEntidade("XYZ1A23", StatusVeiculo.LOJA_3));
        movimentacaoRepository.save(movimentacaoDeVeiculo(outro, null, StatusVeiculo.LOJA_3, "Cadastro outro"));
        em.flush();

        List<MovimentacaoVeiculo> resultado =
                movimentacaoRepository.findByVeiculoIdOrderByRegistradoEmDesc(veiculo.getId());

        assertThat(resultado).hasSize(3);
        assertThat(resultado).noneMatch(m -> m.getVeiculo().getId().equals(outro.getId()));
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private MovimentacaoVeiculo movimentacao(StatusVeiculo anterior, StatusVeiculo novo, String obs) {
        return movimentacao(anterior, novo, obs, LocalDateTime.now());
    }

    private MovimentacaoVeiculo movimentacao(StatusVeiculo anterior, StatusVeiculo novo, String obs, LocalDateTime registradoEm) {
        return MovimentacaoVeiculo.builder()
                .veiculo(veiculo)
                .statusAnterior(anterior)
                .statusNovo(novo)
                .observacao(obs)
                .registradoEm(registradoEm)
                .build();
    }

    private MovimentacaoVeiculo movimentacaoDeVeiculo(Veiculo v, StatusVeiculo anterior,
                                                       StatusVeiculo novo, String obs) {
        return MovimentacaoVeiculo.builder()
                .veiculo(v)
                .statusAnterior(anterior)
                .statusNovo(novo)
                .observacao(obs)
                .build();
    }
}
