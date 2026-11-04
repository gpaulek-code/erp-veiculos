package com.revendaauto.erp.service.impl;

import com.revendaauto.erp.dto.MovimentacaoResponseDTO;
import com.revendaauto.erp.entity.MovimentacaoVeiculo;
import com.revendaauto.erp.entity.Veiculo;
import com.revendaauto.erp.enums.StatusVeiculo;
import com.revendaauto.erp.exception.VeiculoNotFoundException;
import com.revendaauto.erp.repository.MovimentacaoRepository;
import com.revendaauto.erp.repository.VeiculoRepository;
import com.revendaauto.erp.service.MovimentacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovimentacaoServiceImpl implements MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final VeiculoRepository veiculoRepository;

    @Override
    @Transactional
    public void registrar(Long veiculoId, StatusVeiculo statusAnterior,
                          StatusVeiculo statusNovo, String observacao) {
        // Não registra se o status não mudou
        if (statusAnterior == statusNovo) return;

        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new VeiculoNotFoundException(veiculoId));

        MovimentacaoVeiculo mov = MovimentacaoVeiculo.builder()
                .veiculo(veiculo)
                .statusAnterior(statusAnterior)
                .statusNovo(statusNovo)
                .observacao(observacao)
                .build();

        movimentacaoRepository.save(mov);
        log.info("Movimentação registrada — veículo {} : {} → {}", veiculo.getPlaca(), statusAnterior, statusNovo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimentacaoResponseDTO> listarPorVeiculo(Long veiculoId) {
        if (!veiculoRepository.existsById(veiculoId)) {
            throw new VeiculoNotFoundException(veiculoId);
        }
        return movimentacaoRepository
                .findByVeiculoIdOrderByRegistradoEmDesc(veiculoId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimentacaoResponseDTO> listarRecentes(int quantidade) {
        return movimentacaoRepository
                .findAll(PageRequest.of(0, quantidade,
                        org.springframework.data.domain.Sort.by("registradoEm").descending()))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ─── Mapeamento ────────────────────────────────────────────────────────────

    private MovimentacaoResponseDTO toDTO(MovimentacaoVeiculo m) {
        return MovimentacaoResponseDTO.builder()
                .id(m.getId())
                .veiculoId(m.getVeiculo().getId())
                .veiculoPlaca(m.getVeiculo().getPlaca())
                .veiculoModelo(m.getVeiculo().getModelo())
                .statusAnterior(m.getStatusAnterior())
                .statusAnteriorDescricao(m.getStatusAnterior() != null
                        ? m.getStatusAnterior().getDescricao() : "—")
                .statusNovo(m.getStatusNovo())
                .statusNovoDescricao(m.getStatusNovo().getDescricao())
                .observacao(m.getObservacao())
                .registradoEm(m.getRegistradoEm())
                .build();
    }
}
