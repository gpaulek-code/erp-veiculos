-- ============================================================
-- ERP Veículos — Script de criação do banco de dados
-- PostgreSQL 15+
-- Executar manualmente caso prefira não usar ddl-auto=update
-- ============================================================

-- Cria o banco (rodar fora de uma transação)
-- CREATE DATABASE erp_veiculos;

-- Conectar ao banco erp_veiculos antes de continuar

-- ─── Tipo ENUM para status ─────────────────────────────────
DO $$ BEGIN
    CREATE TYPE status_veiculo AS ENUM (
        'MANUTENCAO',
        'VENDIDO',
        'LOJA_1',
        'LOJA_2',
        'LOJA_3'
    );
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

-- ─── Tabela de veículos ────────────────────────────────────
CREATE TABLE IF NOT EXISTS veiculos (
    id              BIGSERIAL       PRIMARY KEY,
    placa           VARCHAR(8)      NOT NULL UNIQUE,
    marca           VARCHAR(50)     NOT NULL,
    modelo          VARCHAR(100)    NOT NULL,
    ano             INTEGER         NOT NULL CHECK (ano BETWEEN 1950 AND 2100),
    cor             VARCHAR(30)     NOT NULL,
    valor           NUMERIC(12, 2)  NOT NULL CHECK (valor > 0),
    status          VARCHAR(20)     NOT NULL,
    criado_em       TIMESTAMP       NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- ─── Índices para otimizar os filtros de busca ─────────────
CREATE INDEX IF NOT EXISTS idx_veiculos_placa   ON veiculos (UPPER(placa));
CREATE INDEX IF NOT EXISTS idx_veiculos_modelo  ON veiculos (UPPER(modelo));
CREATE INDEX IF NOT EXISTS idx_veiculos_status  ON veiculos (status);

-- ─── Dados de exemplo ──────────────────────────────────────
INSERT INTO veiculos (placa, marca, modelo, ano, cor, valor, status)
VALUES
    ('ABC1D23', 'Volkswagen', 'Gol 1.0',        2020, 'Branco',    45000,  38500.00, 'LOJA_1'),
    ('BCD2E34', 'Fiat',       'Uno 1.0',         2018, 'Prata',     82000,  28900.00, 'LOJA_1'),
    ('CDE3F45', 'Chevrolet',  'Onix 1.0 Turbo',  2022, 'Preto',     12500,  72000.00, 'LOJA_2'),
    ('DEF4G56', 'Toyota',     'Corolla 2.0',     2021, 'Cinza',     30200, 115000.00, 'LOJA_2'),
    ('EFG5H67', 'Honda',      'Civic EXL',       2019, 'Azul',      58000,  98500.00, 'LOJA_3'),
    ('FGH6I78', 'Hyundai',    'HB20 1.0',        2023, 'Vermelho',   8000,  69900.00, 'LOJA_3'),
    ('GHI7J89', 'Renault',    'Kwid 1.0',        2021, 'Laranja',   35600,  45000.00, 'MANUTENCAO'),
    ('HIJ8K90', 'Jeep',       'Renegade 1.8',    2020, 'Branco',    61000,  88000.00, 'MANUTENCAO'),
    ('IJK9L01', 'Ford',       'Ka 1.0',          2019, 'Prata',     92000,  35000.00, 'VENDIDO'),
    ('JKL0M12', 'Chevrolet',  'Tracker 1.0',     2022, 'Preto',     22000, 105000.00, 'VENDIDO')
ON CONFLICT (placa) DO NOTHING;
