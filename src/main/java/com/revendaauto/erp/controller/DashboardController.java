package com.revendaauto.erp.controller;

import com.revendaauto.erp.service.MovimentacaoService;
import com.revendaauto.erp.service.VeiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class DashboardController {

    private final VeiculoService veiculoService;
    private final MovimentacaoService movimentacaoService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("dashboard", veiculoService.obterDadosDashboard());
        model.addAttribute("movimentacoesRecentes", movimentacaoService.listarRecentes(8));
        return "dashboard/index";
    }
}
