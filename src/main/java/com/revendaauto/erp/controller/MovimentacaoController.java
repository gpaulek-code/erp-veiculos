package com.revendaauto.erp.controller;

import com.revendaauto.erp.dto.MovimentacaoFormDTO;
import com.revendaauto.erp.dto.VeiculoResponseDTO;
import com.revendaauto.erp.enums.StatusVeiculo;
import com.revendaauto.erp.service.MovimentacaoService;
import com.revendaauto.erp.service.VeiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/veiculos/{id}/movimentacoes")
@RequiredArgsConstructor
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;
    private final VeiculoService veiculoService;

    // Tela de histórico + formulário de nova movimentação
    @GetMapping
    public String historico(@PathVariable Long id, Model model) {
        VeiculoResponseDTO veiculo = veiculoService.buscarPorId(id);
        model.addAttribute("veiculo", veiculo);
        model.addAttribute("movimentacoes", movimentacaoService.listarPorVeiculo(id));
        model.addAttribute("form", new MovimentacaoFormDTO());
        model.addAttribute("statusList", StatusVeiculo.values());
        return "veiculo/historico";
    }

    // Registra nova movimentação manual
    @PostMapping
    public String registrar(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") MovimentacaoFormDTO form,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            VeiculoResponseDTO veiculo = veiculoService.buscarPorId(id);
            model.addAttribute("veiculo", veiculo);
            model.addAttribute("movimentacoes", movimentacaoService.listarPorVeiculo(id));
            model.addAttribute("statusList", StatusVeiculo.values());
            return "veiculo/historico";
        }

        VeiculoResponseDTO veiculo = veiculoService.buscarPorId(id);
        movimentacaoService.registrar(id, veiculo.getStatus(), form.getStatusNovo(), form.getObservacao());

        // Atualiza também o status atual do veículo
        var formDTO = new com.revendaauto.erp.dto.VeiculoFormDTO();
        formDTO.setId(veiculo.getId());
        formDTO.setPlaca(veiculo.getPlaca());
        formDTO.setMarca(veiculo.getMarca());
        formDTO.setModelo(veiculo.getModelo());
        formDTO.setAno(veiculo.getAno());
        formDTO.setCor(veiculo.getCor());
        formDTO.setValor(veiculo.getValor());
        formDTO.setStatus(form.getStatusNovo());
        veiculoService.atualizar(id, formDTO);

        redirectAttributes.addFlashAttribute("sucesso", "Movimentação registrada com sucesso!");
        return "redirect:/veiculos/" + id + "/movimentacoes";
    }
}
