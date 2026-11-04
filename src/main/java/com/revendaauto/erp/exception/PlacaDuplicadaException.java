package com.revendaauto.erp.exception;

public class PlacaDuplicadaException extends RuntimeException {

    public PlacaDuplicadaException(String placa) {
        super("Já existe um veículo cadastrado com a placa: " + placa);
    }
}
