package com.revendaauto.erp.service;

import com.revendaauto.erp.dto.MovimentacaoResponseDTO;
import com.revendaauto.erp.entity.MovimentacaoVeiculo;
import com.revendaauto.erp.entity.Veiculo;
import com.revendaauto.erp.enums.StatusVeiculo;
import com.revendaauto.erp.exception.VeiculoNotFoundException;
import com.revendaauto.erp.repository.MovimentacaoRepository;
import com.revendaauto.erp.repository.VeiculoRepository;
import com.revendaauto.erp.service.impl.MovimentacaoServiceImpl;
import com.revendaauto.erp.util.VeiculoTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovimentacaoService — Testes Unitários")
class MovimentacaoServiceTest {

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private MovimentacaoServiceImpl movimentacaoService;

    private Veiculo veiculoPadrao;

    @BeforeEach
    void setUp() {
        veiculoPadrao = VeiculoTestFactory.veiculoEntidade("ABC1D23", StatusVeiculo.LOJA_1);
        veiculoPadrao.setId(1L);
    }

    // ═══════════════════════════════════════════════════════════
    // REGISTRAR
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("registrar()")
    class Registrar {

        @Test
        @DisplayName("Deve registrar movimentação quando status mudar")
        void deveRegistrarQuandoStatusMudar() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoPadrao));
            when(movimentacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            movimentacaoService.registrar(1L, StatusVeiculo.LOJA_1, StatusVeiculo.MANUTENCAO, "Revisão preventiva");

            verify(movimentacaoRepository).save(argThat(mov ->
                    mov.getStatusAnterior() == StatusVeiculo.LOJA_1 &&
                    mov.getStatusNovo()     == StatusVeiculo.MANUTENCAO &&
                    "Revisão preventiva".equals(mov.getObservacao())
            ));
        }

        @Test
        @DisplayName("Não deve registrar movimentação quando status for igual")
        void naoDeveRegistrarQuandoStatusForIgual() {
            movimentacaoService.registrar(1L, StatusVeiculo.LOJA_1, StatusVeiculo.LOJA_1, null);

            verify(movimentacaoRepository, never()).save(any());
            verify(veiculoRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Deve registrar movimentação de cadastro inicial com statusAnterior nulo")
        void deveRegistrarCadastroInicial() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoPadrao));
            when(movimentacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            movimentacaoService.registrar(1L, null, StatusVeiculo.LOJA_1, "Cadastro inicial");

            verify(movimentacaoRepository).save(argThat(mov ->
                    mov.getStatusAnterior() == null &&
                    mov.getStatusNovo()     == StatusVeiculo.LOJA_1
            ));
        }

        @Test
        @DisplayName("Deve lançar VeiculoNotFoundException quando veículo não existir")
        void deveLancarExcecaoQuandoVeiculoNaoExistir() {
            when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    movimentacaoService.registrar(99L, StatusVeiculo.LOJA_1, StatusVeiculo.MANUTENCAO, null))
                    .isInstanceOf(VeiculoNotFoundException.class)
                    .hasMessageContaining("99");

            verify(movimentacaoRepository, never()).save(any());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LISTAR POR VEÍCULO
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("listarPorVeiculo()")
    class ListarPorVeiculo {

        @Test
        @DisplayName("Deve retornar lista de movimentações do veículo ordenada por data desc")
        void deveRetornarListaOrdenada() {
            MovimentacaoVeiculo m1 = movimentacao(veiculoPadrao, null, StatusVeiculo.LOJA_1, "Cadastro inicial");
            MovimentacaoVeiculo m2 = movimentacao(veiculoPadrao, StatusVeiculo.LOJA_1, StatusVeiculo.MANUTENCAO, "Revisão");

            when(veiculoRepository.existsById(1L)).thenReturn(true);
            when(movimentacaoRepository.findByVeiculoIdOrderByRegistradoEmDesc(1L))
                    .thenReturn(List.of(m2, m1));

            List<MovimentacaoResponseDTO> resultado = movimentacaoService.listarPorVeiculo(1L);

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getStatusNovo()).isEqualTo(StatusVeiculo.MANUTENCAO);
            assertThat(resultado.get(1).getStatusNovo()).isEqualTo(StatusVeiculo.LOJA_1);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando veículo não tiver movimentações")
        void deveRetornarListaVazia() {
            when(veiculoRepository.existsById(1L)).thenReturn(true);
            when(movimentacaoRepository.findByVeiculoIdOrderByRegistradoEmDesc(1L))
                    .thenReturn(List.of());

            List<MovimentacaoResponseDTO> resultado = movimentacaoService.listarPorVeiculo(1L);

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar VeiculoNotFoundException quando veículo não existir")
        void deveLancarExcecaoQuandoVeiculoNaoExistir() {
            when(veiculoRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> movimentacaoService.listarPorVeiculo(99L))
                    .isInstanceOf(VeiculoNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("Deve mapear statusAnteriorDescricao como '—' quando statusAnterior for nulo")
        void deveMapearDescricaoNulaCorretamente() {
            MovimentacaoVeiculo m = movimentacao(veiculoPadrao, null, StatusVeiculo.LOJA_2, "Cadastro");

            when(veiculoRepository.existsById(1L)).thenReturn(true);
            when(movimentacaoRepository.findByVeiculoIdOrderByRegistradoEmDesc(1L))
                    .thenReturn(List.of(m));

            List<MovimentacaoResponseDTO> resultado = movimentacaoService.listarPorVeiculo(1L);

            assertThat(resultado.get(0).getStatusAnteriorDescricao()).isEqualTo("—");
            assertThat(resultado.get(0).getStatusNovoDescricao()).isEqualTo("Loja 2");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LISTAR RECENTES
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("listarRecentes()")
    class ListarRecentes {

        @Test
        @DisplayName("Deve retornar quantidade solicitada de movimentações recentes")
        void deveRetornarQuantidadeSolicitada() {
            MovimentacaoVeiculo m1 = movimentacao(veiculoPadrao, null, StatusVeiculo.LOJA_1, "Cadastro");
            MovimentacaoVeiculo m2 = movimentacao(veiculoPadrao, StatusVeiculo.LOJA_1, StatusVeiculo.VENDIDO, "Venda");

            when(movimentacaoRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(m2, m1)));

            List<MovimentacaoResponseDTO> resultado = movimentacaoService.listarRecentes(5);

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getStatusNovo()).isEqualTo(StatusVeiculo.VENDIDO);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver movimentações")
        void deveRetornarVazioSemMovimentacoes() {
            when(movimentacaoRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            List<MovimentacaoResponseDTO> resultado = movimentacaoService.listarRecentes(8);

            assertThat(resultado).isEmpty();
        }
    }

    // ─── Helper ────────────────────────────────────────────────────────────────

    private MovimentacaoVeiculo movimentacao(Veiculo veiculo,
                                              StatusVeiculo anterior,
                                              StatusVeiculo novo,
                                              String observacao) {
        MovimentacaoVeiculo m = MovimentacaoVeiculo.builder()
                .id(1L)
                .veiculo(veiculo)
                .statusAnterior(anterior)
                .statusNovo(novo)
                .observacao(observacao)
                .build();
        // Simula @PrePersist
        try {
            var field = MovimentacaoVeiculo.class.getDeclaredField("registradoEm");
            field.setAccessible(true);
            field.set(m, LocalDateTime.now());
        } catch (Exception ignored) {}
        return m;
    }
}
