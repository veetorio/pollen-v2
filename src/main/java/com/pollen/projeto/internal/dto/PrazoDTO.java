package com.pollen.projeto.internal.dto;

import java.time.LocalDateTime;

public record PrazoDTO(
        LocalDateTime inicio,
        LocalDateTime fim
) {}
