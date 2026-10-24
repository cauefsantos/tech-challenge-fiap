package br.com.fiap.techchallengefiap.presentation.rest.usuario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.fiap.techchallengefiap.application.cliente.GerenciarClienteUseCase;
import br.com.fiap.techchallengefiap.domain.cliente.model.TipoPessoa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioSecurityIntegrationTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private GerenciarClienteUseCase gerenciarCliente;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void aplicaLoginPersistidoPapeisEProtegeCredenciais() throws Exception {
        mvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized());

        String adminToken = login("TEST-BOOTSTRAP-ADMIN", "test-only-password-1234");
        String username = "funcionario-" + UUID.randomUUID().toString().substring(0, 8);
        MvcResult created = mvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"employee-test-password-1234","papel":"FUNCIONARIO"}
                                """.formatted(username)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.papel").value("FUNCIONARIO"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();
        JsonNode employee = objectMapper.readTree(created.getResponse().getContentAsString());
        long employeeId = employee.get("id").asLong();
        mvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"another-test-password-1234","papel":"FUNCIONARIO"}
                                """.formatted(username.toUpperCase())))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"sem-vinculo","password":"employee-test-password-1234","papel":"CLIENTE"}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"funcionario-vinculado","password":"employee-test-password-1234","papel":"FUNCIONARIO","clienteId":999999}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"cliente-inexistente","password":"customer-test-password-1234","papel":"CLIENTE","clienteId":999999}
                                """))
                .andExpect(status().isNotFound());

        String employeeToken = login(username, "employee-test-password-1234");
        mvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/clientes")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/servicos")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/clientes")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/servicos")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/usuarios/" + employeeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/clientes")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"employee-test-password-1234"}
                                """.formatted(username)))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/v1/usuarios/" + employeeId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"papel":"FUNCIONARIO","enabled":true}
                                """))
                .andExpect(status().isOk());
        login(username, "employee-test-password-1234");
        mvc.perform(delete("/api/v1/usuarios/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/v1/public/ordens-servico/non-existent?documento=52998224725"))
                .andExpect(status().isNotFound());

        var cliente = gerenciarCliente.criar(TipoPessoa.FISICA, "11144477735", "Cliente de teste", null, null);
        String clienteUsername = "cliente-" + UUID.randomUUID().toString().substring(0, 8);
        mvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"customer-test-password-1234","papel":"CLIENTE","clienteId":%d}
                                """.formatted(clienteUsername, cliente.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(cliente.getId()));
        mvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"cliente-duplicado","password":"customer-test-password-1234","papel":"CLIENTE","clienteId":%d}
                                """.formatted(cliente.getId())))
                .andExpect(status().isConflict());
        String clienteToken = login(clienteUsername, "customer-test-password-1234");
        mvc.perform(get("/api/v1/clientes")
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isForbidden());
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }
}
