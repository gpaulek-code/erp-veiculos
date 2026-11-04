package com.revendaauto.erp.service.impl;

import com.revendaauto.erp.entity.Usuario;
import com.revendaauto.erp.repository.UsuarioRepository;
import com.revendaauto.erp.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    public void alterarSenha(String username, String senhaAtual, String novaSenha) {
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (!passwordEncoder.matches(senhaAtual, u.getPassword())) {
            throw new RuntimeException("Senha atual incorreta");
        }
        u.setPassword(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(u);
    }

    @Override
    public void alterarStatus(String username, boolean online) {
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        u.setOnline(online);
        usuarioRepository.save(u);
    }

    @Override
    public void atualizarPerfil(String username, String nomeEmpresa, String email) {
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        u.setNomeEmpresa(nomeEmpresa);
        u.setEmail(email);
        usuarioRepository.save(u);
    }
}
