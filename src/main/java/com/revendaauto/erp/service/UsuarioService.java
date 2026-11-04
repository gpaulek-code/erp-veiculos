package com.revendaauto.erp.service;

import com.revendaauto.erp.entity.Usuario;
import java.util.Optional;

public interface UsuarioService {
    Optional<Usuario> buscarPorUsername(String username);
    Usuario salvar(Usuario usuario);
    void alterarSenha(String username, String senhaAtual, String novaSenha);
    void alterarStatus(String username, boolean online);
    void atualizarPerfil(String username, String nomeEmpresa, String email);
}
