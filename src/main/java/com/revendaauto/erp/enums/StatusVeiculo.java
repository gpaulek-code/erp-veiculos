package com.revendaauto.erp.enums;

public enum StatusVeiculo {

    MANUTENCAO("Manutenção"),
    VENDIDO("Vendido"),
    LOJA_1("Loja 1"),
    LOJA_2("Loja 2"),
    LOJA_3("Loja 3");

    private final String descricao;

    StatusVeiculo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
