package br.com.fiap.techchallengefiap.domain.cliente.model;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {
    private static final OffsetDateTime DATA = OffsetDateTime.parse("2025-01-02T10:15:30-03:00");

    @Test
    void criaPessoaFisicaComCpfMascaradoValido() {
        Cliente cliente = new Cliente(1L, TipoPessoa.FISICA, "529.982.247-25", "  Ana Silva  ",
                null, null, DATA, true);

        assertEquals("52998224725", cliente.getDocumento());
        assertEquals("Ana Silva", cliente.getNome());
        assertEquals(DATA, cliente.getDataCadastro());
    }

    @Test
    void criaPessoaJuridicaComCnpjMascaradoValido() {
        Cliente cliente = new Cliente(TipoPessoa.JURIDICA, "11.222.333/0001-81", "Oficina", null, null);

        assertEquals("11222333000181", cliente.getDocumento());
    }

    @Test
    void rejeitaCpfInvalidoParaPessoaFisica() {
        assertThrows(DomainException.class,
                () -> new Cliente(TipoPessoa.FISICA, "111.111.111-11", "Ana", null, null));
        assertThrows(DomainException.class,
                () -> new Cliente(TipoPessoa.FISICA, "52998224724", "Ana", null, null));
    }

    @Test
    void rejeitaCnpjInvalidoParaPessoaJuridica() {
        assertThrows(DomainException.class,
                () -> new Cliente(TipoPessoa.JURIDICA, "00.000.000/0000-00", "Oficina", null, null));
        assertThrows(DomainException.class,
                () -> new Cliente(TipoPessoa.JURIDICA, "11.222.333/0001-80", "Oficina", null, null));
    }

    @Test
    void rejeitaDocumentoComCaracteresNaoPermitidosOuTipoIncompativel() {
        assertThrows(DomainException.class,
                () -> new Cliente(TipoPessoa.FISICA, "52998224725x", "Ana", null, null));
        assertThrows(DomainException.class,
                () -> new Cliente(TipoPessoa.JURIDICA, "52998224725", "Ana", null, null));
    }

    @Test
    void rejeitaTipoDePessoaOuNomeAusentes() {
        assertThrows(DomainException.class, () -> new Cliente(null, "52998224725", "Ana", null, null));
        assertThrows(DomainException.class, () -> new Cliente(TipoPessoa.FISICA, "52998224725", " ", null, null));
    }

    @Test
    void normalizaEmailETelefoneQuandoInformados() {
        Cliente cliente = new Cliente(TipoPessoa.FISICA, "52998224725", "Ana", "  ana@example.com  ", "  11999990000 ");

        assertEquals("ana@example.com", cliente.getEmail());
        assertEquals("11999990000", cliente.getTelefone());
    }

    @Test
    void permiteEmailETelefoneAusentesERejeitaEmailInvalido() {
        Cliente cliente = new Cliente(TipoPessoa.FISICA, "52998224725", "Ana", " ", null);

        assertNull(cliente.getEmail());
        assertNull(cliente.getTelefone());
        assertThrows(DomainException.class,
                () -> new Cliente(TipoPessoa.FISICA, "52998224725", "Ana", "email-invalido", null));
    }

    @Test
    void alteraAtivacaoSemAlterarDadosHistoricos() {
        Cliente cliente = new Cliente(5L, TipoPessoa.FISICA, "52998224725", "Ana", null, null, DATA, true);
        cliente.desativar();
        cliente.ativar();

        assertTrue(cliente.isAtivo());
        assertEquals(5L, cliente.getId());
        assertEquals("52998224725", cliente.getDocumento());
        assertEquals(DATA, cliente.getDataCadastro());
    }
}
