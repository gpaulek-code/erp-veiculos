package com.revendaauto.erp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/chamado")
public class ChamadoController {

    @GetMapping
    public String form() {
        return "chamado/form";
    }

    @PostMapping("/enviar")
    public String enviar(@RequestParam String assunto,
                         @RequestParam String mensagem,
                         RedirectAttributes ra) {
        // Futuramente integrar com email/ticket
        ra.addFlashAttribute("sucesso", "Chamado enviado com sucesso! Em breve nossa equipe entrará em contato.");
        return "redirect:/chamado";
    }
}
