package com.pollen.usuario.internal.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
public record LoginDto(
    String senha,
    String email
) {}
