package br.com.fiap.techchallengefiap.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.DispatcherType;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

import br.com.fiap.techchallengefiap.domain.usuario.repository.UsuarioRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties({SecurityProperties.class, AdminProperties.class})
public class SecurityConfiguration {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }

    @Bean
    SecretKey jwtKey(SecurityProperties p) {
        byte[] secret = Base64.getDecoder().decode(p.jwtSecret());
        if (secret.length < 32) {
            throw new IllegalStateException("JWT_SECRET deve codificar pelo menos 32 bytes.");
        }
        return new SecretKeySpec(secret, "HmacSHA256");
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey key, UsuarioRepository usuarios) {
        var decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> accountValidator = jwt -> {
            String role = jwt.getClaimAsString("role");
            boolean valid = role != null && usuarios.buscarPorUsername(jwt.getSubject())
                    .filter(user -> user.isEnabled() && user.getPapel().name().equals(role))
                    .isPresent();
            return valid
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN,
                    "Conta ausente, desativada ou com papel alterado.", null));
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), accountValidator));
        return decoder;
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");
            if (role == null || role.isBlank()) return List.of();
            return List.of(new SimpleGrantedAuthority("ROLE_" + role));
        });
        return converter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Encaminhamentos internos de erro do Spring (corpo invalido, metodo nao suportado etc.)
                        // precisam passar para manter o status original, em vez de virar 403.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/public/**").permitAll()
                        .requestMatchers("/api/v1/usuarios", "/api/v1/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/clientes", "/api/v1/clientes/**",
                                "/api/v1/veiculos", "/api/v1/veiculos/**",
                                "/api/v1/servicos", "/api/v1/servicos/**",
                                "/api/v1/materiais", "/api/v1/materiais/**",
                                "/api/v1/ordens-servico", "/api/v1/ordens-servico/**")
                        .hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers("/api/v1/clientes", "/api/v1/clientes/**",
                                "/api/v1/veiculos", "/api/v1/veiculos/**",
                                "/api/v1/servicos", "/api/v1/servicos/**",
                                "/api/v1/materiais", "/api/v1/materiais/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/ordens-servico")
                        .hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.GET, "/api/v1/ordens-servico/metricas/**")
                        .hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.GET, "/api/v1/ordens-servico/**")
                        .hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/ordens-servico/*/diagnostico/**",
                                "/api/v1/ordens-servico/*/itens-servico",
                                "/api/v1/ordens-servico/*/itens-material",
                                "/api/v1/ordens-servico/*/orcamentos",
                                "/api/v1/ordens-servico/*/orcamentos/*/disponibilizar",
                                "/api/v1/ordens-servico/*/finalizacao",
                                "/api/v1/ordens-servico/*/entrega")
                        .hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/ordens-servico/*/itens-servico/*",
                                "/api/v1/ordens-servico/*/itens-material/*")
                        .hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers("/api/v1/ordens-servico/*/orcamentos/*/aprovar",
                                "/api/v1/ordens-servico/*/orcamentos/*/rejeitar")
                        .hasRole("ADMIN")
                        .anyRequest().denyAll()
                        )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }
}
