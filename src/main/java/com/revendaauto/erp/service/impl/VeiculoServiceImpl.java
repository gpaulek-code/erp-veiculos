package com.revendaauto.erp.service.impl;

import com.revendaauto.erp.dto.DashboardDTO;
import com.revendaauto.erp.dto.VeiculoFormDTO;
import com.revendaauto.erp.dto.VeiculoResponseDTO;
import com.revendaauto.erp.entity.Veiculo;
import com.revendaauto.erp.enums.StatusVeiculo;
import com.revendaauto.erp.exception.PlacaDuplicadaException;
import com.revendaauto.erp.exception.VeiculoNotFoundException;
import com.revendaauto.erp.repository.MovimentacaoRepository;
import com.revendaauto.erp.repository.VeiculoRepository;
import com.revendaauto.erp.service.MovimentacaoService;
import com.revendaauto.erp.service.VeiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class VeiculoServiceImpl implements VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final MovimentacaoService movimentacaoService;
    private final MovimentacaoRepository movimentacaoRepository;

    @Override
    @Transactional
    public VeiculoResponseDTO salvar(VeiculoFormDTO dto) {
        if (veiculoRepository.findByPlacaIgnoreCase(dto.getPlaca()).isPresent()) {
            throw new PlacaDuplicadaException(dto.getPlaca());
        }

        Veiculo veiculo = toEntity(dto);
        Veiculo salvo = veiculoRepository.save(veiculo);

        // Registra movimentação inicial (sem status anterior)
        movimentacaoService.registrar(salvo.getId(), null, salvo.getStatus(), "Cadastro inicial");

        return toResponseDTO(salvo);
    }

    @Override
    @Transactional
    public VeiculoResponseDTO atualizar(Long id, VeiculoFormDTO dto) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new VeiculoNotFoundException(id));

        if (veiculoRepository.existsByPlacaIgnoreCaseAndIdNot(dto.getPlaca(), id)) {
            throw new PlacaDuplicadaException(dto.getPlaca());
        }

        StatusVeiculo statusAnterior = veiculo.getStatus();

        veiculo.setPlaca(dto.getPlaca().toUpperCase());
        veiculo.setMarca(dto.getMarca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setAno(dto.getAno());
        veiculo.setCor(dto.getCor());
        veiculo.setValor(dto.getValor());
        veiculo.setStatus(dto.getStatus());

        Veiculo atualizado = veiculoRepository.save(veiculo);

        // Registra movimentação somente se o status mudou
        if (statusAnterior != dto.getStatus()) {
            movimentacaoService.registrar(atualizado.getId(), statusAnterior, atualizado.getStatus(), null);
        }

        return toResponseDTO(atualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public VeiculoResponseDTO buscarPorId(Long id) {
        return veiculoRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new VeiculoNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VeiculoResponseDTO> listar(String placa, String modelo, StatusVeiculo status, Pageable pageable) {
        String placaFiltro  = (placa  != null && !placa.isBlank())  ? placa.trim()  : null;
        String modeloFiltro = (modelo != null && !modelo.isBlank()) ? modelo.trim() : null;
        String placaPattern  = placaFiltro  != null ? "%" + placaFiltro.toUpperCase()  + "%" : null;
        String modeloPattern = modeloFiltro != null ? "%" + modeloFiltro.toUpperCase() + "%" : null;

        return veiculoRepository
                .buscarComFiltros(placaPattern, modeloPattern, status, pageable)
                .map(this::toResponseDTO);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        if (!veiculoRepository.existsById(id)) {
            throw new VeiculoNotFoundException(id);
        }
        // Excluir movimentações vinculadas antes do veículo
        movimentacaoRepository.deleteByVeiculoId(id);
        veiculoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDTO obterDadosDashboard() {
        LocalDateTime limiteManutencao = LocalDateTime.now().minusDays(7);
        LocalDateTime limiteEstoque    = LocalDateTime.now().minusDays(30);

        var manutencaoAlerta = veiculoRepository.findManutencaoAntiga(limiteManutencao)
                .stream().map(this::toResponseDTO).toList();
        var estoqueAlerta = veiculoRepository.findEstoqueParado(limiteEstoque)
                .stream().map(this::toResponseDTO).toList();

        return DashboardDTO.builder()
                .totalVeiculos(veiculoRepository.count())
                .totalVendidos(veiculoRepository.countByStatus(StatusVeiculo.VENDIDO))
                .totalManutencao(veiculoRepository.countByStatus(StatusVeiculo.MANUTENCAO))
                .totalLoja1(veiculoRepository.countByStatus(StatusVeiculo.LOJA_1))
                .totalLoja2(veiculoRepository.countByStatus(StatusVeiculo.LOJA_2))
                .totalLoja3(veiculoRepository.countByStatus(StatusVeiculo.LOJA_3))
                .valorTotalEstoque(veiculoRepository.somarValorEstoqueAtivo(StatusVeiculo.VENDIDO))
                .veiculosManutencaoAlerta(manutencaoAlerta)
                .veiculosEstoqueAlerta(estoqueAlerta)
                .build();
    }

    // ─── Mapeamentos ───────────────────────────────────────────────────────────

    private Veiculo toEntity(VeiculoFormDTO dto) {
        return Veiculo.builder()
                .placa(dto.getPlaca().toUpperCase())
                .marca(dto.getMarca())
                .modelo(dto.getModelo())
                .ano(dto.getAno())
                .cor(dto.getCor())
                .valor(dto.getValor())
                .status(dto.getStatus())
                .build();
    }

    private VeiculoResponseDTO toResponseDTO(Veiculo v) {
        return VeiculoResponseDTO.builder()
                .id(v.getId())
                .placa(v.getPlaca())
                .marca(v.getMarca())
                .modelo(v.getModelo())
                .ano(v.getAno())
                .cor(v.getCor())
                .valor(v.getValor())
                .status(v.getStatus())
                .statusDescricao(v.getStatus().getDescricao())
                .criadoEm(v.getCriadoEm())
                .atualizadoEm(v.getAtualizadoEm())
                .diasNoEstoque(v.getCriadoEm() != null ? ChronoUnit.DAYS.between(v.getCriadoEm(), LocalDateTime.now()) : 0L)
                .build();
    }
}
