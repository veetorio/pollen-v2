package com.pollen.usuario.internal.dto;

import lombok.Builder;

@Builder 
public record TokenOutput(
        String token,
        String expireAt,
        String refreshToken
) {}
