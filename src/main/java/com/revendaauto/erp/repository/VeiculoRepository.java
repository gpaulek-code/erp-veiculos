package com.revendaauto.erp.repository;

import com.revendaauto.erp.entity.Veiculo;
import com.revendaauto.erp.enums.StatusVeiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    // Busca por placa exata (case-insensitive)
    Optional<Veiculo> findByPlacaIgnoreCase(String placa);

    // Verifica duplicidade de placa (útil na edição)
    boolean existsByPlacaIgnoreCaseAndIdNot(String placa, Long id);

    // Listagem com filtros combinados + paginação
    @Query("""
            SELECT v FROM Veiculo v
            WHERE (:placaPattern IS NULL OR UPPER(v.placa) LIKE :placaPattern)
            AND   (:modeloPattern IS NULL OR UPPER(v.modelo) LIKE :modeloPattern)
            AND   (:status IS NULL OR v.status = :status)
            """)
    Page<Veiculo> buscarComFiltros(
            @Param("placaPattern") String placaPattern,
            @Param("modeloPattern") String modeloPattern,
            @Param("status") StatusVeiculo status,
            Pageable pageable
    );

    // Contagens para o dashboard
    long countByStatus(StatusVeiculo status);

    @Query("SELECT COALESCE(SUM(v.valor), 0) FROM Veiculo v WHERE v.status <> :statusExcluido")
    BigDecimal somarValorEstoqueAtivo(@Param("statusExcluido") StatusVeiculo statusExcluido);

    // Veículos em manutenção há mais de X dias
    @Query("SELECT v FROM Veiculo v WHERE v.status = 'MANUTENCAO' AND v.atualizadoEm < :limite")
    List<Veiculo> findManutencaoAntiga(@Param("limite") LocalDateTime limite);

    // Veículos no estoque (não vendidos) há mais de X dias
    @Query("SELECT v FROM Veiculo v WHERE v.status <> 'VENDIDO' AND v.criadoEm < :limite")
    List<Veiculo> findEstoqueParado(@Param("limite") LocalDateTime limite);
}
