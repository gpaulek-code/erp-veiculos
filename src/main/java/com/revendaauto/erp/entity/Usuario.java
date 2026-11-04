package com.revendaauto.erp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String role; // ROLE_ADMIN, ROLE_USER

    @Column(length = 100)
    private String nomeEmpresa;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "boolean default true")
    private boolean online = true;
}
