package br.com.fiap.techchallengefiap.presentation.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MaterialEstoqueIntegrationTest {
    @Autowired
    private MockMvc mvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String token;

    @BeforeEach
    void autenticar() throws Exception {
        token = objectMapper.readTree(mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"test-bootstrap-admin","password":"test-only-password-1234"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void edicaoDeCadastroNaoSobrescreveSaldo() throws Exception {
        String codigo = "MAT-" + UUID.randomUUID();
        long id = enviar(post("/api/v1/materiais"), """
                {"codigo":"%s","tipo":"PECA","nome":"Filtro","valorUnitario":45.90,"unidadeMedida":"UN","quantidadeEstoque":10,"estoqueMinimo":2}
                """.formatted(codigo), status().isCreated()).get("id").asLong();

        JsonNode editado = enviar(put("/api/v1/materiais/" + id), """
                {"codigo":"%s","tipo":"PECA","nome":"Filtro de oleo","valorUnitario":45.90,"unidadeMedida":"UN","quantidadeEstoque":99,"estoqueMinimo":2}
                """.formatted(codigo), status().isOk());

        assertEquals("Filtro de oleo", editado.get("nome").asText());
        assertEquals(10, editado.get("quantidadeEstoque").asDouble());
    }

    @Test
    void entradaDeEstoqueSomaAoSaldoAtual() throws Exception {
        long id = criarMaterial("10");

        JsonNode material = enviar(post("/api/v1/materiais/" + id + "/entradas-estoque"),
                "{\"quantidade\":5.5}", status().isOk());

        assertEquals(15.5, material.get("quantidadeEstoque").asDouble());
    }

    @Test
    void entradaDeEstoqueExigeQuantidadePositiva() throws Exception {
        long id = criarMaterial("10");

        enviar(post("/api/v1/materiais/" + id + "/entradas-estoque"), "{\"quantidade\":0}", status().isBadRequest());
        enviar(post("/api/v1/materiais/" + id + "/entradas-estoque"), "{}", status().isBadRequest());

        assertEquals(10, enviar(get("/api/v1/materiais/" + id), null, status().isOk()).get("quantidadeEstoque").asDouble());
    }

    private long criarMaterial(String saldo) throws Exception {
        return enviar(post("/api/v1/materiais"), """
                {"codigo":"MAT-%s","tipo":"PECA","nome":"Filtro","valorUnitario":45.90,"unidadeMedida":"UN","quantidadeEstoque":%s,"estoqueMinimo":2}
                """.formatted(UUID.randomUUID(), saldo), status().isCreated()).get("id").asLong();
    }

    private JsonNode enviar(MockHttpServletRequestBuilder request, String corpo, ResultMatcher esperado) throws Exception {
        request.header("Authorization", "Bearer " + token);
        if (corpo != null) request.contentType(MediaType.APPLICATION_JSON).content(corpo);
        String resposta = mvc.perform(request).andExpect(esperado).andReturn().getResponse().getContentAsString();
        return resposta.isEmpty() ? null : objectMapper.readTree(resposta);
    }
}
