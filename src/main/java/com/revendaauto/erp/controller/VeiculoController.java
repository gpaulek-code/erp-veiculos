package com.revendaauto.erp.controller;

import com.revendaauto.erp.dto.VeiculoFormDTO;
import com.revendaauto.erp.dto.VeiculoResponseDTO;
import com.revendaauto.erp.enums.StatusVeiculo;
import com.revendaauto.erp.service.VeiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService veiculoService;

    // ─── Listagem ──────────────────────────────────────────────────────────────

    @GetMapping
    public String listar(
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) StatusVeiculo status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<VeiculoResponseDTO> veiculos = veiculoService.listar(placa, modelo, status, pageable);

        model.addAttribute("veiculos", veiculos);
        model.addAttribute("statusList", StatusVeiculo.values());
        model.addAttribute("placa", placa);
        model.addAttribute("modelo", modelo);
        model.addAttribute("statusSelecionado", status);

        return "veiculo/lista";
    }

    // ─── Cadastro ──────────────────────────────────────────────────────────────

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("veiculo", new VeiculoFormDTO());
        model.addAttribute("statusList", StatusVeiculo.values());
        model.addAttribute("titulo", "Novo Veículo");
        return "veiculo/formulario";
    }

    @PostMapping("/novo")
    public String salvar(
            @Valid @ModelAttribute("veiculo") VeiculoFormDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            model.addAttribute("statusList", StatusVeiculo.values());
            model.addAttribute("titulo", "Novo Veículo");
            return "veiculo/formulario";
        }

        veiculoService.salvar(dto);
        redirectAttributes.addFlashAttribute("sucesso", "Veículo cadastrado com sucesso!");
        return "redirect:/veiculos";
    }

    // ─── Edição ────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        VeiculoResponseDTO response = veiculoService.buscarPorId(id);

        VeiculoFormDTO form = new VeiculoFormDTO();
        form.setId(response.getId());
        form.setPlaca(response.getPlaca());
        form.setMarca(response.getMarca());
        form.setModelo(response.getModelo());
        form.setAno(response.getAno());
        form.setCor(response.getCor());
        form.setValor(response.getValor());
        form.setStatus(response.getStatus());

        model.addAttribute("veiculo", form);
        model.addAttribute("statusList", StatusVeiculo.values());
        model.addAttribute("titulo", "Editar Veículo");
        return "veiculo/formulario";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("veiculo") VeiculoFormDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            model.addAttribute("statusList", StatusVeiculo.values());
            model.addAttribute("titulo", "Editar Veículo");
            return "veiculo/formulario";
        }

        veiculoService.atualizar(id, dto);
        redirectAttributes.addFlashAttribute("sucesso", "Veículo atualizado com sucesso!");
        return "redirect:/veiculos";
    }

    // ─── Detalhes ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("veiculo", veiculoService.buscarPorId(id));
        return "veiculo/detalhe";
    }

    // ─── Exclusão ──────────────────────────────────────────────────────────────

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        veiculoService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Veículo excluído com sucesso!");
        return "redirect:/veiculos";
    }
}
