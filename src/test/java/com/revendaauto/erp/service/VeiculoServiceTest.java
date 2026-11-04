package com.revendaauto.erp.service;

import com.revendaauto.erp.dto.DashboardDTO;
import com.revendaauto.erp.dto.VeiculoFormDTO;
import com.revendaauto.erp.dto.VeiculoResponseDTO;
import com.revendaauto.erp.entity.Veiculo;
import com.revendaauto.erp.enums.StatusVeiculo;
import com.revendaauto.erp.exception.PlacaDuplicadaException;
import com.revendaauto.erp.exception.VeiculoNotFoundException;
import com.revendaauto.erp.repository.VeiculoRepository;
import com.revendaauto.erp.service.impl.VeiculoServiceImpl;
import com.revendaauto.erp.util.VeiculoTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.revendaauto.erp.repository.MovimentacaoRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("VeiculoService — Testes Unitários")
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private MovimentacaoService movimentacaoService;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @InjectMocks
    private VeiculoServiceImpl veiculoService;

    private Veiculo veiculoPadrao;
    private VeiculoFormDTO formPadrao;

    @BeforeEach
    void setUp() {
        veiculoPadrao = VeiculoTestFactory.veiculoEntidade("ABC1D23", StatusVeiculo.LOJA_1);
        veiculoPadrao.setId(1L);

        formPadrao = VeiculoTestFactory.veiculoFormDTO("ABC1D23", StatusVeiculo.LOJA_1);
    }

    // ═══════════════════════════════════════════════════════════
    // SALVAR
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("salvar()")
    class Salvar {

        @Test
        @DisplayName("Deve salvar veículo com dados válidos e registrar movimentação inicial")
        void deveSalvarComSucesso() {
            when(veiculoRepository.findByPlacaIgnoreCase("ABC1D23")).thenReturn(Optional.empty());
            when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculoPadrao);

            VeiculoResponseDTO response = veiculoService.salvar(formPadrao);

            assertThat(response).isNotNull();
            assertThat(response.getPlaca()).isEqualTo("ABC1D23");
            assertThat(response.getStatus()).isEqualTo(StatusVeiculo.LOJA_1);

            verify(veiculoRepository).save(any(Veiculo.class));
            verify(movimentacaoService).registrar(eq(1L), isNull(), eq(StatusVeiculo.LOJA_1), anyString());
        }

        @Test
        @DisplayName("Deve lançar PlacaDuplicadaException quando placa já existir")
        void deveLancarExcecaoPlacaDuplicada() {
            when(veiculoRepository.findByPlacaIgnoreCase("ABC1D23"))
                    .thenReturn(Optional.of(veiculoPadrao));

            assertThatThrownBy(() -> veiculoService.salvar(formPadrao))
                    .isInstanceOf(PlacaDuplicadaException.class)
                    .hasMessageContaining("ABC1D23");

            verify(veiculoRepository, never()).save(any());
            verify(movimentacaoService, never()).registrar(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Deve converter placa para maiúsculo ao salvar")
        void deveConverterPlacaParaMaiusculo() {
            formPadrao.setPlaca("abc1d23");
            when(veiculoRepository.findByPlacaIgnoreCase("abc1d23")).thenReturn(Optional.empty());
            when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(inv -> {
                Veiculo v = inv.getArgument(0);
                v.setId(1L);
                return v;
            });

            VeiculoResponseDTO response = veiculoService.salvar(formPadrao);

            assertThat(response.getPlaca()).isEqualTo("ABC1D23");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ATUALIZAR
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar veículo e registrar movimentação quando status mudar")
        void deveAtualizarERegistrarMovimentacao() {
            formPadrao.setStatus(StatusVeiculo.MANUTENCAO); // mudando de LOJA_1 para MANUTENCAO

            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoPadrao));
            when(veiculoRepository.existsByPlacaIgnoreCaseAndIdNot("ABC1D23", 1L)).thenReturn(false);
            when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculoPadrao);

            veiculoService.atualizar(1L, formPadrao);

            verify(veiculoRepository).save(any(Veiculo.class));
            verify(movimentacaoService).registrar(eq(1L), eq(StatusVeiculo.LOJA_1), eq(StatusVeiculo.MANUTENCAO), isNull());
        }

        @Test
        @DisplayName("Não deve registrar movimentação quando status não mudar")
        void naoDeveRegistrarMovimentacaoSemMudancaDeStatus() {
            formPadrao.setStatus(StatusVeiculo.LOJA_1); // mesmo status

            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoPadrao));
            when(veiculoRepository.existsByPlacaIgnoreCaseAndIdNot("ABC1D23", 1L)).thenReturn(false);
            when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculoPadrao);

            veiculoService.atualizar(1L, formPadrao);

            verify(movimentacaoService, never()).registrar(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Deve lançar VeiculoNotFoundException ao atualizar ID inexistente")
        void deveLancarExcecaoVeiculoNaoEncontrado() {
            when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> veiculoService.atualizar(99L, formPadrao))
                    .isInstanceOf(VeiculoNotFoundException.class)
                    .hasMessageContaining("99");

            verify(veiculoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar PlacaDuplicadaException ao atualizar com placa de outro veículo")
        void deveLancarExcecaoPlacaDuplicadaAoAtualizar() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoPadrao));
            when(veiculoRepository.existsByPlacaIgnoreCaseAndIdNot("ABC1D23", 1L)).thenReturn(true);

            assertThatThrownBy(() -> veiculoService.atualizar(1L, formPadrao))
                    .isInstanceOf(PlacaDuplicadaException.class);

            verify(veiculoRepository, never()).save(any());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BUSCAR POR ID
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar DTO quando veículo existir")
        void deveRetornarDtoQuandoEncontrar() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoPadrao));

            VeiculoResponseDTO response = veiculoService.buscarPorId(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getPlaca()).isEqualTo("ABC1D23");
            assertThat(response.getStatusDescricao()).isEqualTo("Loja 1");
        }

        @Test
        @DisplayName("Deve lançar VeiculoNotFoundException quando ID não existir")
        void deveLancarExcecaoQuandoNaoEncontrar() {
            when(veiculoRepository.findById(42L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> veiculoService.buscarPorId(42L))
                    .isInstanceOf(VeiculoNotFoundException.class)
                    .hasMessageContaining("42");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LISTAR
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("listar()")
    class Listar {

        @Test
        @DisplayName("Deve retornar página de veículos com filtros nulos")
        void deveRetornarPaginaComFiltrosNulos() {
            Page<Veiculo> paginaMock = new PageImpl<>(List.of(veiculoPadrao));
            when(veiculoRepository.buscarComFiltros(isNull(), isNull(), isNull(), any()))
                    .thenReturn(paginaMock);

            Page<VeiculoResponseDTO> resultado = veiculoService.listar(null, null, null, PageRequest.of(0, 10));

            assertThat(resultado.getTotalElements()).isEqualTo(1);
            assertThat(resultado.getContent().get(0).getPlaca()).isEqualTo("ABC1D23");
        }

        @Test
        @DisplayName("Deve ignorar filtros em branco e passar null ao repositório")
        void deveIgnorarFiltrosEmBranco() {
            Page<Veiculo> paginaMock = new PageImpl<>(List.of());
            when(veiculoRepository.buscarComFiltros(isNull(), isNull(), isNull(), any()))
                    .thenReturn(paginaMock);

            veiculoService.listar("  ", "  ", null, PageRequest.of(0, 10));

            verify(veiculoRepository).buscarComFiltros(isNull(), isNull(), isNull(), any());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EXCLUIR
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("excluir()")
    class Excluir {

        @Test
        @DisplayName("Deve excluir veículo existente com sucesso")
        void deveExcluirComSucesso() {
            when(veiculoRepository.existsById(1L)).thenReturn(true);

            veiculoService.excluir(1L);

            verify(movimentacaoRepository).deleteByVeiculoId(1L);
            verify(veiculoRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Deve lançar VeiculoNotFoundException ao excluir ID inexistente")
        void deveLancarExcecaoAoExcluirInexistente() {
            when(veiculoRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> veiculoService.excluir(99L))
                    .isInstanceOf(VeiculoNotFoundException.class);

            verify(veiculoRepository, never()).deleteById(any());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DASHBOARD
    // ═══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("obterDadosDashboard()")
    class Dashboard {

        @Test
        @DisplayName("Deve retornar totais corretos de cada status")
        void deveRetornarTotaisCorretos() {
            when(veiculoRepository.count()).thenReturn(10L);
            when(veiculoRepository.countByStatus(StatusVeiculo.VENDIDO)).thenReturn(3L);
            when(veiculoRepository.countByStatus(StatusVeiculo.MANUTENCAO)).thenReturn(2L);
            when(veiculoRepository.countByStatus(StatusVeiculo.LOJA_1)).thenReturn(2L);
            when(veiculoRepository.countByStatus(StatusVeiculo.LOJA_2)).thenReturn(2L);
            when(veiculoRepository.countByStatus(StatusVeiculo.LOJA_3)).thenReturn(1L);
            when(veiculoRepository.somarValorEstoqueAtivo(StatusVeiculo.VENDIDO))
                    .thenReturn(new BigDecimal("350000.00"));

            DashboardDTO dto = veiculoService.obterDadosDashboard();

            assertThat(dto.getTotalVeiculos()).isEqualTo(10L);
            assertThat(dto.getTotalVendidos()).isEqualTo(3L);
            assertThat(dto.getTotalManutencao()).isEqualTo(2L);
            assertThat(dto.getTotalLoja1()).isEqualTo(2L);
            assertThat(dto.getTotalLoja2()).isEqualTo(2L);
            assertThat(dto.getTotalLoja3()).isEqualTo(1L);
            assertThat(dto.getValorTotalEstoque()).isEqualByComparingTo("350000.00");
        }
    }
}
