package br.com.fiap.techchallengefiap.domain.veiculo.model;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VeiculoTest {
    private static final OffsetDateTime DATA = OffsetDateTime.parse("2025-01-02T10:15:30-03:00");

    @Test
    void normalizaPlacaAntigaAbc1234() {
        Veiculo veiculo = new Veiculo(1L, "abc-1234", " Fiat ", " Uno ", 2020);

        assertEquals("ABC1234", veiculo.getPlaca());
        assertEquals("Fiat", veiculo.getMarca());
        assertEquals("Uno", veiculo.getModelo());
    }

    @Test
    void normalizaPlacaMercosulAbc1d23() {
        assertEquals("ABC1D23", new Veiculo(1L, "abc 1d23", "Ford", "Ka", 2021).getPlaca());
    }

    @Test
    void rejeitaPlacaInvalidaOuAusente() {
        assertThrows(DomainException.class, () -> new Veiculo(1L, null, "Ford", "Ka", 2021));
        assertThrows(DomainException.class, () -> new Veiculo(1L, "ABC12D3", "Ford", "Ka", 2021));
        assertThrows(DomainException.class, () -> new Veiculo(1L, "ABC-12345", "Ford", "Ka", 2021));
    }

    @Test
    void rejeitaClienteIdInvalido() {
        assertThrows(DomainException.class, () -> new Veiculo(null, "ABC1234", "Ford", "Ka", 2021));
        assertThrows(DomainException.class, () -> new Veiculo(0L, "ABC1234", "Ford", "Ka", 2021));
    }

    @Test
    void rejeitaMarcaOuModeloAusentes() {
        assertThrows(DomainException.class, () -> new Veiculo(1L, "ABC1234", " ", "Ka", 2021));
        assertThrows(DomainException.class, () -> new Veiculo(1L, "ABC1234", "Ford", null, 2021));
    }

    @Test
    void aceitaAnoNosLimitesPermitidos() {
        int anoMaximo = OffsetDateTime.now().getYear() + 1;
        assertEquals(1886, new Veiculo(1L, "ABC1234", "Ford", "Modelo", 1886).getAno());
        assertEquals(anoMaximo, new Veiculo(1L, "ABC1234", "Ford", "Modelo", anoMaximo).getAno());
    }

    @Test
    void rejeitaAnoAnteriorA1886OuPosteriorAoLimite() {
        int anoMaximo = OffsetDateTime.now().getYear() + 1;
        assertThrows(DomainException.class, () -> new Veiculo(1L, "ABC1234", "Ford", "Modelo", 1885));
        assertThrows(DomainException.class,
                () -> new Veiculo(1L, "ABC1234", "Ford", "Modelo", anoMaximo + 1));
    }

    @Test
    void alteraAtivacaoDoVeiculo() {
        Veiculo veiculo = new Veiculo(1L, 2L, "ABC1234", "Ford", "Ka", 2021, DATA, true);
        veiculo.desativar();
        assertFalse(veiculo.isAtivo());
        veiculo.ativar();
        assertTrue(veiculo.isAtivo());
        assertEquals(DATA, veiculo.getDataCadastro());
    }
}
