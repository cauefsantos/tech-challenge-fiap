package br.com.fiap.techchallengefiap.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Fluxo pela API identificando cliente por CPF/CNPJ e veiculo por placa, sem ids numericos.
 */
@SpringBootTest
@AutoConfigureMockMvc
class IdentificacaoPorDocumentoEPlacaIntegrationTest {
    // Cadastrados apenas pelo teste de cadastro.
    private static final String CPF = "24681357928";
    private static final String PLACA = "BRA2E19";
    // Cadastros de apoio, garantidos antes de cada teste.
    private static final String CPF_DONO = "39053344705";
    private static final String PLACA_DONO = "XYZ1A23";
    private static final String CNPJ = "11222333000181";
    private static final String PLACA_OUTRO_DONO = "XYZ9A87";

    @Autowired
    private MockMvc mvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String token;

    @BeforeEach
    void prepararCenario() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"test-bootstrap-admin","password":"test-only-password-1234"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        token = objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
        criarSeNaoExistir("/api/v1/clientes/" + CPF_DONO, "/api/v1/clientes", """
                {"tipoPessoa":"FISICA","documento":"%s","nome":"Carlos Lima"}
                """.formatted(CPF_DONO));
        criarSeNaoExistir("/api/v1/clientes/" + CNPJ, "/api/v1/clientes", """
                {"tipoPessoa":"JURIDICA","documento":"%s","nome":"Transportes Ltda"}
                """.formatted(CNPJ));
        criarSeNaoExistir("/api/v1/veiculos/" + PLACA_DONO, "/api/v1/veiculos", """
                {"documentoCliente":"%s","placa":"%s","marca":"Fiat","modelo":"Uno","ano":2020}
                """.formatted(CPF_DONO, PLACA_DONO));
        criarSeNaoExistir("/api/v1/veiculos/" + PLACA_OUTRO_DONO, "/api/v1/veiculos", """
                {"documentoCliente":"%s","placa":"%s","marca":"Volvo","modelo":"FH","ano":2022}
                """.formatted(CNPJ, PLACA_OUTRO_DONO));
    }

    @Test
    void cadastraClienteVeiculoEOrdemDeServicoPorDocumentoEPlaca() throws Exception {
        mvc.perform(json(post("/api/v1/clientes"), """
                        {"tipoPessoa":"FISICA","documento":"246.813.579-28","nome":"Joana Souza"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/clientes/" + CPF))
                .andExpect(jsonPath("$.documento").value(CPF))
                .andExpect(jsonPath("$.id").doesNotExist());

        mvc.perform(autenticado(get("/api/v1/clientes/" + CPF)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joana Souza"));
        mvc.perform(autenticado(get("/api/v1/clientes/246.813.579-28")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documento").value(CPF));
        mvc.perform(autenticado(get("/api/v1/clientes/12345678909")))
                .andExpect(status().isNotFound());

        mvc.perform(json(post("/api/v1/veiculos"), """
                        {"documentoCliente":"246.813.579-28","placa":"bra-2e19","marca":"Fiat","modelo":"Uno","ano":2020}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/veiculos/" + PLACA))
                .andExpect(jsonPath("$.placa").value(PLACA))
                .andExpect(jsonPath("$.documentoCliente").value(CPF))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.clienteId").doesNotExist());
        mvc.perform(autenticado(get("/api/v1/veiculos/bra2e19")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentoCliente").value(CPF));
        mvc.perform(json(put("/api/v1/veiculos/" + PLACA), """
                        {"documentoCliente":"%s","placa":"%s","marca":"Fiat","modelo":"Uno Way","ano":2020}
                        """.formatted(CPF, PLACA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Uno Way"));

        MvcResult criada = mvc.perform(json(post("/api/v1/ordens-servico"), """
                        {"documentoCliente":"246.813.579-28","placa":"BRA-2E19"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentoCliente").value(CPF))
                .andExpect(jsonPath("$.placa").value(PLACA))
                .andExpect(jsonPath("$.numero").isNotEmpty())
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.clienteId").doesNotExist())
                .andExpect(jsonPath("$.veiculoId").doesNotExist())
                .andReturn();
        long osId = objectMapper.readTree(criada.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(autenticado(get("/api/v1/ordens-servico/" + osId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentoCliente").value(CPF))
                .andExpect(jsonPath("$.placa").value(PLACA));
        mvc.perform(autenticado(post("/api/v1/ordens-servico/" + osId + "/diagnostico/inicio")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"))
                .andExpect(jsonPath("$.placa").value(PLACA));
    }

    @Test
    void recusaOrdemDeServicoComDadosQueNaoIdentificamClienteEVeiculo() throws Exception {
        mvc.perform(json(post("/api/v1/ordens-servico"), """
                        {"documentoCliente":"%s","placa":"%s"}
                        """.formatted(CPF_DONO, PLACA_OUTRO_DONO)))
                .andExpect(status().isBadRequest());
        mvc.perform(json(post("/api/v1/ordens-servico"), """
                        {"documentoCliente":"12345678909","placa":"%s"}
                        """.formatted(PLACA_DONO)))
                .andExpect(status().isNotFound());
        mvc.perform(json(post("/api/v1/ordens-servico"), """
                        {"documentoCliente":"%s","placa":"QWE1234"}
                        """.formatted(CPF_DONO)))
                .andExpect(status().isNotFound());
        mvc.perform(json(post("/api/v1/ordens-servico"), """
                        {"clienteId":1,"veiculoId":1}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void desativaEReativaClientePeloDocumento() throws Exception {
        mvc.perform(autenticado(delete("/api/v1/clientes/" + CNPJ)))
                .andExpect(status().isNoContent());
        mvc.perform(autenticado(get("/api/v1/clientes/" + CNPJ)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
        mvc.perform(json(post("/api/v1/ordens-servico"), """
                        {"documentoCliente":"%s","placa":"%s"}
                        """.formatted(CNPJ, PLACA_OUTRO_DONO)))
                .andExpect(status().isBadRequest());
        mvc.perform(json(patch("/api/v1/clientes/" + CNPJ + "/ativacao"), """
                        {"ativo":true}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
    }

    private void criarSeNaoExistir(String recurso, String colecao, String corpo) throws Exception {
        int status = mvc.perform(autenticado(get(recurso))).andReturn().getResponse().getStatus();
        if (status == 404) {
            mvc.perform(json(post(colecao), corpo)).andExpect(status().isCreated());
        }
    }

    private MockHttpServletRequestBuilder autenticado(MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + token);
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String corpo) {
        return autenticado(request).contentType(MediaType.APPLICATION_JSON).content(corpo);
    }
}
