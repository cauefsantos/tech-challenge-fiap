package br.com.fiap.techchallengefiap.presentation.rest.auth;

import br.com.fiap.techchallengefiap.infrastructure.security.SecurityProperties;
import jakarta.validation.Valid;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder encoder;
    private final SecurityProperties properties;

    public AuthenticationController(AuthenticationManager am, JwtEncoder encoder, SecurityProperties properties) {
        this.authenticationManager = am;
        this.encoder = encoder;
        this.properties = properties;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        Instant now = Instant.now();
        Instant expires = now.plus(properties.tokenValidity());
        String scope = auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.joining(" "));
        var claims = JwtClaimsSet.builder().issuer("oficina-api").issuedAt(now).expiresAt(expires).subject(auth.getName()).claim("scope", scope).build();
        var header = JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", properties.tokenValidity().toSeconds());
    }
}
