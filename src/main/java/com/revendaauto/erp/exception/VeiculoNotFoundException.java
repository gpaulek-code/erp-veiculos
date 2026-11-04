package com.revendaauto.erp.exception;

public class VeiculoNotFoundException extends RuntimeException {

    public VeiculoNotFoundException(Long id) {
        super("Veículo não encontrado com ID: " + id);
    }

    public VeiculoNotFoundException(String placa) {
        super("Veículo não encontrado com placa: " + placa);
    }
}
