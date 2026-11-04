package com.revendaauto.erp.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VeiculoNotFoundException.class)
    public String handleVeiculoNotFound(VeiculoNotFoundException ex, Model model) {
        model.addAttribute("erro", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(PlacaDuplicadaException.class)
    public String handlePlacaDuplicada(PlacaDuplicadaException ex, Model model) {
        model.addAttribute("erro", ex.getMessage());
        return "error/400";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, Model model) {
        model.addAttribute("erro", "Ocorreu um erro inesperado. Tente novamente.");
        return "error/500";
    }
}
