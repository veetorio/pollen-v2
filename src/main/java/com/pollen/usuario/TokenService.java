package com.pollen.usuario;

import java.time.Duration;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.pollen.usuario.internal.dto.TokenOutput;
import com.pollen.usuario.internal.dto.UsuarioResponse;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class TokenService {



    private Duration expirantionTime = Duration.ofMinutes(30);
    private final JwtEncoder jwtEncoder;
    private final String issuer = "pollen-v1";

    public Duration getExpirationTime() {
        return expirantionTime;
    }

    public TokenOutput generateToken(UsuarioResponse usuario) {
        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(usuario.matricula().toString())
                .claim("nome", usuario.nome())
                .claim("tipo", usuario.tipo())
                .expiresAt(java.time.Instant.now().plus(expirantionTime))
                .build();
        var token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        TokenOutput tokenOutput = TokenOutput.builder()
                .token(token)
                .expireAt(claims.getExpiresAt().toString())
                .refreshToken(null)
                .build();
        return tokenOutput;
    }
    
}
