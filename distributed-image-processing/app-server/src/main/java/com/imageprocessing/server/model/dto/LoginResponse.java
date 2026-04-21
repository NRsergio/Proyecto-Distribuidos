package com.imageprocessing.server.model.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class LoginResponse {
    private String token;
    private Long idUsuario;
    private String nombre;
    private String email;
}
