package com.revendaauto.erp.controller;

import com.revendaauto.erp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String erro,
            @RequestParam(required = false) String logout,
            Model model) {
        if (erro != null) model.addAttribute("erro", "Usuário ou senha inválidos.");
        if (logout != null) model.addAttribute("sucesso", "Você saiu com sucesso.");
        return "login";
    }

    @GetMapping("/esqueci-senha")
    public String esqueciSenhaForm() {
        return "esqueci-senha";
    }

    @PostMapping("/esqueci-senha")
    public String esqueciSenhaSubmit(@RequestParam String email, Model model) {
        // Por segurança sempre exibe mensagem de sucesso independente se email existe
        model.addAttribute("sucesso",
            "Se esse e-mail estiver cadastrado, você receberá as instruções em breve. " +
            "Caso não receba, entre em contato com o administrador do sistema.");
        return "esqueci-senha";
    }
}
