# 🚗 ERP Veículos

Sistema web para gestão de estoque de uma revenda de automóveis, desenvolvido com Java 21 e Spring Boot.

---

## 📋 Sumário

- [Tecnologias](#tecnologias)
- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Como Rodar](#como-rodar)
- [Rotas do Sistema](#rotas-do-sistema)
- [Testes](#testes)
- [Banco de Dados](#banco-de-dados)
- [Próximas Evoluções](#próximas-evoluções)

---

## Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.2.5 | Framework base |
| Spring Data JPA | 3.2.5 | Acesso ao banco |
| Spring Validation | 3.2.5 | Validações de formulário |
| PostgreSQL | 15+ | Banco de dados principal |
| Thymeleaf | 3.1 | Template engine (views HTML) |
| Bootstrap | 5.3 | Estilização e responsividade |
| Bootstrap Icons | 1.11 | Ícones |
| Lombok | latest | Redução de boilerplate |
| H2 | runtime | Banco em memória para testes |
| JUnit 5 | 5.10 | Framework de testes |
| Mockito | 5.7 | Mock de dependências nos testes |
| Maven | 3.9+ | Gerenciamento de dependências |

---

## Funcionalidades

### ✅ Implementadas

**Dashboard**
- Contagem total de veículos no estoque
- Contagem por status (Loja 1, Loja 2, Loja 3, Manutenção, Vendidos)
- Valor total do estoque ativo (excluindo vendidos)
- Feed das últimas 8 movimentações registradas

**Gestão de Veículos**
- Cadastro com validação completa (placa única, campos obrigatórios)
- Edição de todos os campos
- Exclusão com confirmação via modal
- Visualização detalhada de cada veículo
- Listagem paginada (10 por página)
- Filtros combinados: placa, modelo e status
- Placa convertida automaticamente para maiúsculo

**Histórico de Movimentações**
- Registro automático ao cadastrar um veículo (status inicial)
- Registro automático ao alterar o status de um veículo
- Registro manual com observação livre
- Timeline visual do histórico por veículo

**Status disponíveis**
- 🏪 Loja 1
- 🏪 Loja 2
- 🏪 Loja 3
- 🔧 Manutenção
- ✅ Vendido

---

## Arquitetura

O sistema segue a arquitetura em camadas (Layered Architecture) do Spring MVC:

```
┌─────────────────────────────────────────────────┐
│                   BROWSER                        │
└─────────────────┬───────────────────────────────┘
                  │ HTTP
┌─────────────────▼───────────────────────────────┐
│              CONTROLLER LAYER                    │
│   VeiculoController  |  DashboardController      │
│   MovimentacaoController                         │
└─────────────────┬───────────────────────────────┘
                  │ DTOs
┌─────────────────▼───────────────────────────────┐
│               SERVICE LAYER                      │
│   VeiculoService  |  MovimentacaoService         │
│   (interface + impl)  ←  regras de negócio       │
└─────────────────┬───────────────────────────────┘
                  │ Entities
┌─────────────────▼───────────────────────────────┐
│             REPOSITORY LAYER                     │
│   VeiculoRepository  |  MovimentacaoRepository   │
│   (Spring Data JPA)                              │
└─────────────────┬───────────────────────────────┘
                  │ JDBC
┌─────────────────▼───────────────────────────────┐
│              PostgreSQL                          │
│   tabela: veiculos  |  tabela: movimentacoes     │
└─────────────────────────────────────────────────┘
```

---

## Estrutura do Projeto

```
erp-veiculos/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/revendaauto/erp/
    │   │   ├── ErpVeiculosApplication.java     ← Entry point
    │   │   ├── config/
    │   │   │   ├── DataInitializer.java        ← Dados de exemplo
    │   │   │   └── WebMvcConfig.java           ← Locale pt-BR
    │   │   ├── controller/
    │   │   │   ├── DashboardController.java
    │   │   │   ├── VeiculoController.java
    │   │   │   └── MovimentacaoController.java
    │   │   ├── dto/
    │   │   │   ├── VeiculoFormDTO.java
    │   │   │   ├── VeiculoResponseDTO.java
    │   │   │   ├── MovimentacaoFormDTO.java
    │   │   │   ├── MovimentacaoResponseDTO.java
    │   │   │   └── DashboardDTO.java
    │   │   ├── entity/
    │   │   │   ├── Veiculo.java
    │   │   │   └── MovimentacaoVeiculo.java
    │   │   ├── enums/
    │   │   │   └── StatusVeiculo.java
    │   │   ├── exception/
    │   │   │   ├── VeiculoNotFoundException.java
    │   │   │   ├── PlacaDuplicadaException.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── repository/
    │   │   │   ├── VeiculoRepository.java
    │   │   │   └── MovimentacaoRepository.java
    │   │   └── service/
    │   │       ├── VeiculoService.java
    │   │       ├── MovimentacaoService.java
    │   │       └── impl/
    │   │           ├── VeiculoServiceImpl.java
    │   │           └── MovimentacaoServiceImpl.java
    │   └── resources/
    │       ├── application.properties           ← Config principal
    │       ├── application-dev.properties       ← Profile dev
    │       ├── application-prod.properties      ← Profile prod
    │       ├── db/
    │       │   └── schema.sql                   ← Script SQL manual
    │       ├── static/
    │       │   └── css/style.css
    │       └── templates/
    │           ├── fragments/layout.html        ← Navbar, footer, alertas
    │           ├── dashboard/index.html
    │           ├── veiculo/
    │           │   ├── lista.html
    │           │   ├── formulario.html
    │           │   ├── detalhe.html
    │           │   └── historico.html
    │           └── error/
    │               ├── 400.html
    │               ├── 404.html
    │               └── 500.html
    └── test/
        ├── java/com/revendaauto/erp/
        │   ├── ErpVeiculosApplicationTest.java
        │   ├── util/VeiculoTestFactory.java
        │   ├── service/
        │   │   ├── VeiculoServiceTest.java
        │   │   └── MovimentacaoServiceTest.java
        │   └── repository/
        │       ├── VeiculoRepositoryTest.java
        │       └── MovimentacaoRepositoryTest.java
        └── resources/
            └── application.properties           ← Config de teste (H2)
```

---

## Como Rodar

### Pré-requisitos

- Java 21+
- Maven 3.9+
- PostgreSQL 15+

### 1. Clonar o projeto

```bash
git clone https://github.com/seu-usuario/erp-veiculos.git
cd erp-veiculos
```

### 2. Criar o banco de dados

```sql
-- No psql ou qualquer client PostgreSQL
CREATE DATABASE erp_veiculos;
```

### 3. Configurar credenciais

Edite `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/erp_veiculos
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA
```

### 4. Rodar a aplicação

```bash
mvn spring-boot:run
```

### 5. Acessar no navegador

```
http://localhost:8080
```

> O sistema vai criar as tabelas automaticamente e inserir **10 veículos de exemplo** na primeira execução.

---

### Rodar com profile dev (banco separado)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Ajuste as credenciais em `application-dev.properties`.

---

## Rotas do Sistema

| Método | Rota | Descrição |
|---|---|---|
| GET | `/` | Dashboard |
| GET | `/veiculos` | Listagem com filtros |
| GET | `/veiculos/novo` | Formulário de cadastro |
| POST | `/veiculos/novo` | Salvar novo veículo |
| GET | `/veiculos/{id}` | Detalhe do veículo |
| GET | `/veiculos/{id}/editar` | Formulário de edição |
| POST | `/veiculos/{id}/editar` | Atualizar veículo |
| POST | `/veiculos/{id}/excluir` | Excluir veículo |
| GET | `/veiculos/{id}/movimentacoes` | Histórico de movimentações |
| POST | `/veiculos/{id}/movimentacoes` | Registrar nova movimentação |

---

## Testes

### Rodar todos os testes

```bash
mvn test
```

### Cobertura por camada

| Classe de Teste | Tipo | Testes |
|---|---|---|
| `VeiculoServiceTest` | Unitário (Mockito) | 11 |
| `MovimentacaoServiceTest` | Unitário (Mockito) | 9 |
| `VeiculoRepositoryTest` | Repositório (H2) | 12 |
| `MovimentacaoRepositoryTest` | Repositório (H2) | 4 |
| `ErpVeiculosApplicationTest` | Contexto Spring | 1 |

> Os testes de repositório usam banco H2 em memória — não precisam de PostgreSQL rodando.

---

## Banco de Dados

### Tabelas geradas automaticamente pelo Hibernate

**veiculos**

| Coluna | Tipo | Observação |
|---|---|---|
| id | BIGSERIAL | PK, auto-incremento |
| placa | VARCHAR(8) | Único, maiúsculo |
| marca | VARCHAR(50) | Obrigatório |
| modelo | VARCHAR(100) | Obrigatório |
| ano | INTEGER | Entre 1950 e 2100 |
| cor | VARCHAR(30) | Obrigatório |
| quilometragem | INTEGER | >= 0 |
| valor | NUMERIC(12,2) | > 0 |
| status | VARCHAR(20) | Enum: LOJA_1, LOJA_2, LOJA_3, MANUTENCAO, VENDIDO |
| criado_em | TIMESTAMP | Preenchido automaticamente |
| atualizado_em | TIMESTAMP | Atualizado automaticamente |

**movimentacoes**

| Coluna | Tipo | Observação |
|---|---|---|
| id | BIGSERIAL | PK, auto-incremento |
| veiculo_id | BIGINT | FK → veiculos.id |
| status_anterior | VARCHAR(20) | Nullable (cadastro inicial) |
| status_novo | VARCHAR(20) | Obrigatório |
| observacao | VARCHAR(255) | Opcional |
| registrado_em | TIMESTAMP | Preenchido automaticamente |

---

## Próximas Evoluções

Funcionalidades planejadas para versões futuras:

- **Módulo de Clientes** — cadastro de compradores vinculados a veículos vendidos
- **Exportação de relatórios** — PDF e Excel do estoque atual
- **Autenticação** — login com Spring Security e controle de acesso por perfil
- **Upload de fotos** — galeria de imagens por veículo
- **API REST** — endpoints JSON para integração com outros sistemas
- **Notificações** — alertas de veículos há muito tempo em manutenção

---

## Autor

Desenvolvido como projeto de demonstração de arquitetura Spring Boot com boas práticas.
