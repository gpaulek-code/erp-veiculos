package com.revendaauto.erp.controller;

import com.revendaauto.erp.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String perfil(@AuthenticationPrincipal UserDetails user, Model model) {
        usuarioService.buscarPorUsername(user.getUsername()).ifPresent(u -> model.addAttribute("usuario", u));
        return "perfil/index";
    }

    @PostMapping("/atualizar")
    public String atualizar(@AuthenticationPrincipal UserDetails user,
                            @RequestParam String nomeEmpresa,
                            @RequestParam String email,
                            RedirectAttributes ra) {
        try {
            usuarioService.atualizarPerfil(user.getUsername(), nomeEmpresa, email);
            ra.addFlashAttribute("sucesso", "Perfil atualizado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/perfil";
    }

    @PostMapping("/senha")
    public String alterarSenha(@AuthenticationPrincipal UserDetails user,
                               @RequestParam String senhaAtual,
                               @RequestParam String novaSenha,
                               @RequestParam String confirmarSenha,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               RedirectAttributes ra) {
        if (!novaSenha.equals(confirmarSenha)) {
            ra.addFlashAttribute("erroSenha", "As senhas não coincidem.");
            return "redirect:/perfil";
        }
        try {
            usuarioService.alterarSenha(user.getUsername(), senhaAtual, novaSenha);
            // Invalida sessão e cookie remember-me para forçar novo login
            SecurityContextHolder.clearContext();
            request.getSession().invalidate();
            Cookie cookie = new Cookie("remember-me", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
            ra.addFlashAttribute("sucesso", "Senha alterada! Faça login novamente.");
            return "redirect:/login";
        } catch (Exception e) {
            ra.addFlashAttribute("erroSenha", e.getMessage());
            return "redirect:/perfil";
        }
    }

    @PostMapping("/status")
    @ResponseBody
    public String alterarStatus(@AuthenticationPrincipal UserDetails user,
                                @RequestParam boolean online) {
        usuarioService.alterarStatus(user.getUsername(), online);
        return "ok";
    }
}
