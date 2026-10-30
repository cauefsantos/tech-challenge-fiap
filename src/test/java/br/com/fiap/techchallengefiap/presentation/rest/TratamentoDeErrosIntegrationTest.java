package br.com.fiap.techchallengefiap.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Requisicoes malformadas devem responder com o status correto (400/405), e nao 403.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TratamentoDeErrosIntegrationTest {
    @Autowired
    private MockMvc mvc;
    private String token;

    @BeforeEach
    void autenticar() throws Exception {
        String resposta = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"test-bootstrap-admin","password":"test-only-password-1234"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = new ObjectMapper().readTree(resposta).get("accessToken").asText();
    }

    @Test
    void valorDeEnumInexistenteRetorna400ComMensagem() throws Exception {
        mvc.perform(post("/api/v1/materiais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"PEC-ENUM","tipo":"OUTRO","nome":"X","valorUnitario":1,"unidadeMedida":"UN","quantidadeEstoque":1,"estoqueMinimo":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Corpo da requisicao invalido")));
    }

    @Test
    void jsonMalformadoRetorna400() throws Exception {
        mvc.perform(post("/api/v1/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{isso nao e json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tipoDeCampoErradoRetorna400() throws Exception {
        mvc.perform(post("/api/v1/servicos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"SRV-TIPO","nome":"X","valorBase":"caro","tempoEstimadoSegundos":60}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void metodoNaoSuportadoRetorna405() throws Exception {
        mvc.perform(patch("/api/v1/clientes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void corpoMalformadoSemTokenContinuaRetornando401() throws Exception {
        mvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{isso nao e json"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaDeErroNaoFicaAbertaParaAcessoDireto() throws Exception {
        mvc.perform(get("/error").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
