package br.com.fiap.techchallengefiap.presentation.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Fluxo de orcamento pela API usando apenas ids devolvidos pelas proprias respostas:
 * o id do orcamento gerado e o mesmo usado para disponibilizar e aprovar.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FluxoOrcamentoIntegrationTest {
    private static final String CPF = "13579246828";
    private static final String PLACA = "ORC1A23";

    @Autowired
    private MockMvc mvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String token;

    @Test
    void geraDisponibilizaEAprovaOrcamentoComOMesmoId() throws Exception {
        token = objectMapper.readTree(mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"test-bootstrap-admin","password":"test-only-password-1234"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("accessToken").asText();

        enviar(post("/api/v1/clientes"), """
                {"tipoPessoa":"FISICA","documento":"%s","nome":"Rita Alves"}
                """.formatted(CPF));
        enviar(post("/api/v1/veiculos"), """
                {"documentoCliente":"%s","placa":"%s","marca":"VW","modelo":"Gol","ano":2019}
                """.formatted(CPF, PLACA));
        long servicoId = enviar(post("/api/v1/servicos"), """
                {"codigo":"SRV-ORC-1","nome":"Troca de oleo","valorBase":150.00,"tempoEstimadoSegundos":3600}
                """).get("id").asLong();
        long materialId = enviar(post("/api/v1/materiais"), """
                {"codigo":"MAT-ORC-1","tipo":"PECA","nome":"Filtro","valorUnitario":45.90,"unidadeMedida":"UN","quantidadeEstoque":10,"estoqueMinimo":2}
                """).get("id").asLong();
        long osId = enviar(post("/api/v1/ordens-servico"), """
                {"documentoCliente":"%s","placa":"%s"}
                """.formatted(CPF, PLACA)).get("id").asLong();
        String os = "/api/v1/ordens-servico/" + osId;

        enviar(post(os + "/diagnostico/inicio"), null);
        enviar(post(os + "/itens-servico"), "{\"servicoId\":%d,\"quantidade\":1}".formatted(servicoId));
        JsonNode comItens = enviar(post(os + "/itens-servico"), "{\"servicoId\":%d,\"quantidade\":2}".formatted(servicoId));
        long itemParaRemover = comItens.get("itensServico").get(1).get("id").asLong();
        JsonNode aposRemocao = enviar(delete(os + "/itens-servico/" + itemParaRemover), null);
        assertEquals(1, aposRemocao.get("itensServico").size());
        enviar(post(os + "/itens-material"), "{\"materialId\":%d,\"quantidade\":1}".formatted(materialId));

        JsonNode orcamento = enviar(post(os + "/orcamentos"), null);
        assertFalse(orcamento.get("id").isNull(), "o orcamento gerado deve vir com id");
        long orcamentoId = orcamento.get("id").asLong();
        assertEquals(150.00, orcamento.get("valorServicos").asDouble());
        assertEquals(45.90, orcamento.get("valorPecas").asDouble());
        assertEquals(195.90, orcamento.get("valorTotal").asDouble());

        JsonNode disponibilizada = enviar(post(os + "/orcamentos/" + orcamentoId + "/disponibilizar"), null);
        assertEquals("AGUARDANDO_APROVACAO", disponibilizada.get("status").asText());

        JsonNode aprovada = enviar(post(os + "/orcamentos/" + orcamentoId + "/aprovar"), """
                {"observacao":"Aprovado por telefone"}
                """);
        assertEquals("EM_EXECUCAO", aprovada.get("status").asText());

        JsonNode consulta = enviar(get(os), null);
        assertEquals(1, consulta.get("orcamentos").size());
        assertEquals(orcamentoId, consulta.get("orcamentos").get(0).get("id").asLong());
        assertEquals("APROVADO", consulta.get("orcamentos").get(0).get("status").asText());
        assertEquals(1, consulta.get("itensMaterial").size());
    }

    private JsonNode enviar(MockHttpServletRequestBuilder request, String corpo) throws Exception {
        request.header("Authorization", "Bearer " + token);
        if (corpo != null) request.contentType(MediaType.APPLICATION_JSON).content(corpo);
        String resposta = mvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta);
    }
}
