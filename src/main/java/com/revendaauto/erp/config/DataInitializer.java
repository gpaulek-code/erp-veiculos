package com.revendaauto.erp.config;

import com.revendaauto.erp.entity.Usuario;
import com.revendaauto.erp.entity.Veiculo;
import com.revendaauto.erp.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.revendaauto.erp.enums.StatusVeiculo;
import com.revendaauto.erp.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Popula o banco com dados de exemplo apenas quando o banco está vazio.
 * Ativo somente nos profiles "default" e "dev" (nunca em "prod").
 */
@Slf4j
@Component
@Profile({"default", "dev"})
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final VeiculoRepository veiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Cria usuário admin padrão se não existir
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            usuarioRepository.save(Usuario.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ROLE_ADMIN")
                    .build());
            log.info("Usuário admin criado (senha: admin123)");
        }

        if (veiculoRepository.count() > 0) {
            log.info("Banco já possui dados — DataInitializer ignorado.");
            return;
        }

        log.info("Populando banco com dados de exemplo...");

        List<Veiculo> veiculos = List.of(
                veiculo("ABC1D23", "Volkswagen", "Gol 1.0",        2020, "Branco",   "38500.00",  StatusVeiculo.LOJA_1),
                veiculo("BCD2E34", "Fiat",       "Uno 1.0",         2018, "Prata",    "28900.00",  StatusVeiculo.LOJA_1),
                veiculo("CDE3F45", "Chevrolet",  "Onix 1.0 Turbo",  2022, "Preto",    "72000.00",  StatusVeiculo.LOJA_2),
                veiculo("DEF4G56", "Toyota",     "Corolla 2.0",     2021, "Cinza",    "115000.00", StatusVeiculo.LOJA_2),
                veiculo("EFG5H67", "Honda",      "Civic EXL",       2019, "Azul",     "98500.00",  StatusVeiculo.LOJA_3),
                veiculo("FGH6I78", "Hyundai",    "HB20 1.0",        2023, "Vermelho", "69900.00",  StatusVeiculo.LOJA_3),
                veiculo("GHI7J89", "Renault",    "Kwid 1.0",        2021, "Laranja",  "45000.00",  StatusVeiculo.MANUTENCAO),
                veiculo("HIJ8K90", "Jeep",       "Renegade 1.8",    2020, "Branco",   "88000.00",  StatusVeiculo.MANUTENCAO),
                veiculo("IJK9L01", "Ford",       "Ka 1.0",          2019, "Prata",    "35000.00",  StatusVeiculo.VENDIDO),
                veiculo("JKL0M12", "Chevrolet",  "Tracker 1.0",     2022, "Preto",    "105000.00", StatusVeiculo.VENDIDO)
        );

        veiculoRepository.saveAll(veiculos);
        log.info("{} veículos inseridos com sucesso.", veiculos.size());
    }

    private Veiculo veiculo(String placa, String marca, String modelo,
                            int ano, String cor,
                            String valor, StatusVeiculo status) {
        return Veiculo.builder()
                .placa(placa)
                .marca(marca)
                .modelo(modelo)
                .ano(ano)
                .cor(cor)
                .valor(new BigDecimal(valor))
                .status(status)
                .build();
    }
}
