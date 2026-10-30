package br.com.fiap.techchallengefiap.infrastructure.ordemservico;

import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import br.com.fiap.techchallengefiap.domain.orcamento.model.Orcamento;
import br.com.fiap.techchallengefiap.domain.orcamento.model.StatusOrcamento;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.HistoricoStatus;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemMaterial;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.StatusOrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.repository.OrdemServicoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Repository
public class OrdemServicoRepositoryAdapter implements OrdemServicoRepository {
    private final OrdemServicoJpaRepository jpa;
    private final JdbcTemplate jdbcTemplate;

    public OrdemServicoRepositoryAdapter(OrdemServicoJpaRepository jpa, JdbcTemplate jdbcTemplate) {
        this.jpa = jpa;
        this.jdbcTemplate = jdbcTemplate;
    }

    public OrdemServico salvar(OrdemServico os) {
        var entity = jpa.save(toEntity(os));
        os.setId(entity.id);
        // Sincroniza os filhos do agregado preservando os ids ja persistidos: as referencias usadas
        // pela API (orcamentoId, itemId) continuam validas entre uma operacao e outra.
        salvarItensServico(entity.id, os.getItensServico());
        salvarItensMaterial(entity.id, os.getItensMaterial());
        salvarOrcamentos(entity.id, os.getOrcamentos());
        salvarHistorico(entity.id, os.getHistoricoStatus());
        return buscarPorId(entity.id).orElseThrow(() -> new IllegalStateException("Ordem de servico nao persistida corretamente."));
    }

    public Optional<OrdemServico> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<OrdemServico> buscarPorIdParaAtualizacao(Long id) {
        return jpa.buscarPorIdParaAtualizacao(id).map(this::toDomain);
    }

    public Optional<OrdemServico> buscarPorNumero(String numero) {
        if (numero == null || numero.isBlank()) return Optional.empty();
        return jpa.findAll().stream()
                .filter(item -> item.numero != null && item.numero.equalsIgnoreCase(numero.trim()))
                .findFirst()
                .map(this::toDomain);
    }

    public List<OrdemServico> listar() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    /** Remove da tabela os registros da OS que nao estao mais no agregado. */
    private void removerAusentes(String tabela, Long ordemServicoId, List<Long> idsMantidos) {
        if (idsMantidos.isEmpty()) {
            jdbcTemplate.update("DELETE FROM " + tabela + " WHERE ordem_servico_id = ?", ordemServicoId);
            return;
        }
        String marcadores = String.join(", ", Collections.nCopies(idsMantidos.size(), "?"));
        List<Object> parametros = new ArrayList<>();
        parametros.add(ordemServicoId);
        parametros.addAll(idsMantidos);
        jdbcTemplate.update("DELETE FROM " + tabela + " WHERE ordem_servico_id = ? AND id NOT IN (" + marcadores + ")",
                parametros.toArray());
    }

    private static <T> List<Long> idsPersistidos(List<T> itens, Function<T, Long> id) {
        return itens == null ? List.of() : itens.stream().map(id).filter(Objects::nonNull).toList();
    }

    private void salvarItensServico(Long ordemServicoId, List<ItemServico> itens) {
        removerAusentes("item_servico", ordemServicoId, idsPersistidos(itens, ItemServico::getId));
        if (itens == null || itens.isEmpty()) return;
        String sql = "INSERT INTO item_servico (ordem_servico_id, servico_id, descricao, quantidade, valor_unitario, subtotal, tempo_estimado_segundos) VALUES (?, ?, ?, ?, ?, ?, ?)";
        for (ItemServico item : itens) {
            if (item.getId() != null) continue;
            jdbcTemplate.update(sql,
                    ordemServicoId,
                    item.getServicoId(),
                    item.getDescricao(),
                    item.getQuantidade(),
                    item.getValorUnitario(),
                    item.getSubtotal(),
                    item.getTempoEstimadoSegundos());
        }
    }

    private void salvarItensMaterial(Long ordemServicoId, List<ItemMaterial> itens) {
        removerAusentes("item_material", ordemServicoId, idsPersistidos(itens, ItemMaterial::getId));
        if (itens == null || itens.isEmpty()) return;
        String sql = "INSERT INTO item_material (ordem_servico_id, material_id, tipo, descricao, quantidade, valor_unitario, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?)";
        for (ItemMaterial item : itens) {
            if (item.getId() != null) continue;
            jdbcTemplate.update(sql,
                    ordemServicoId,
                    item.getMaterialId(),
                    item.getTipo().name(),
                    item.getDescricao(),
                    item.getQuantidade(),
                    item.getValorUnitario(),
                    item.getSubtotal());
        }
    }

    private void salvarOrcamentos(Long ordemServicoId, List<Orcamento> orcamentos) {
        removerAusentes("orcamento", ordemServicoId, idsPersistidos(orcamentos, Orcamento::getId));
        if (orcamentos == null || orcamentos.isEmpty()) return;
        String insert = "INSERT INTO orcamento (ordem_servico_id, versao, valor_servicos, valor_pecas, valor_insumos, valor_total, status, data_geracao, data_resposta, observacao_resposta) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String update = "UPDATE orcamento SET valor_servicos = ?, valor_pecas = ?, valor_insumos = ?, valor_total = ?, status = ?, data_resposta = ?, observacao_resposta = ? WHERE id = ? AND ordem_servico_id = ?";
        for (Orcamento orcamento : orcamentos) {
            if (orcamento.getId() != null) {
                jdbcTemplate.update(update,
                        orcamento.getValorServicos(),
                        orcamento.getValorPecas(),
                        orcamento.getValorInsumos(),
                        orcamento.getValorTotal(),
                        orcamento.getStatus().name(),
                        orcamento.getDataResposta(),
                        orcamento.getObservacaoResposta(),
                        orcamento.getId(),
                        ordemServicoId);
                continue;
            }
            jdbcTemplate.update(insert,
                    ordemServicoId,
                    orcamento.getVersao(),
                    orcamento.getValorServicos(),
                    orcamento.getValorPecas(),
                    orcamento.getValorInsumos(),
                    orcamento.getValorTotal(),
                    orcamento.getStatus().name(),
                    orcamento.getDataGeracao(),
                    orcamento.getDataResposta(),
                    orcamento.getObservacaoResposta());
        }
    }

    private void salvarHistorico(Long ordemServicoId, List<HistoricoStatus> historicos) {
        if (historicos == null || historicos.isEmpty()) return;
        String sql = "INSERT INTO historico_status (ordem_servico_id, status_anterior, status_novo, data_alteracao, observacao) VALUES (?, ?, ?, ?, ?)";
        for (HistoricoStatus item : historicos) {
            // O historico e somente de insercao: registros ja persistidos nunca sao regravados.
            if (item.getId() != null) continue;
            jdbcTemplate.update(sql,
                    ordemServicoId,
                    item.getStatusAnterior() == null ? null : item.getStatusAnterior().name(),
                    item.getStatusNovo().name(),
                    item.getDataAlteracao(),
                    item.getObservacao());
        }
    }

    private OrdemServicoJpaEntity toEntity(OrdemServico os) {
        var e = new OrdemServicoJpaEntity();
        e.id = os.getId();
        e.clienteId = os.getClienteId();
        e.veiculoId = os.getVeiculoId();
        e.numero = os.getNumero();
        e.status = os.getStatus();
        e.orcamentoAprovado = os.isOrcamentoAprovado();
        e.valorTotal = os.getValorTotal();
        e.criadaEm = os.getCriadaEm();
        e.descricaoProblema = os.getDescricaoProblema();
        e.diagnostico = os.getDiagnostico();
        e.dataInicioExecucao = os.getDataInicioExecucao();
        e.dataFinalizacao = os.getDataFinalizacao();
        e.dataEntrega = os.getDataEntrega();
        e.observacoes = os.getObservacoes();
        return e;
    }

    private OrdemServico toDomain(OrdemServicoJpaEntity e) {
        var os = new OrdemServico(e.id, e.numero, e.clienteId, e.veiculoId, e.status, e.orcamentoAprovado,
                e.valorTotal, e.criadaEm, e.descricaoProblema, e.diagnostico, e.dataInicioExecucao,
                e.dataFinalizacao, e.dataEntrega, e.observacoes);
        os.carregarItensPersistidos(buscarItensServico(e.id));
        os.carregarMateriaisPersistidos(buscarItensMaterial(e.id));
        os.carregarOrcamentosPersistidos(buscarOrcamentos(e.id));
        os.carregarHistoricoPersistido(buscarHistorico(e.id));
        return os;
    }

    private List<ItemServico> buscarItensServico(Long ordemServicoId) {
        return jdbcTemplate.query(
                "SELECT id, ordem_servico_id, servico_id, descricao, quantidade, valor_unitario, subtotal, tempo_estimado_segundos FROM item_servico WHERE ordem_servico_id = ? ORDER BY id",
                itemServicoRowMapper(),
                ordemServicoId);
    }

    private List<ItemMaterial> buscarItensMaterial(Long ordemServicoId) {
        return jdbcTemplate.query(
                "SELECT id, ordem_servico_id, material_id, tipo, descricao, quantidade, valor_unitario, subtotal FROM item_material WHERE ordem_servico_id = ? ORDER BY id",
                itemMaterialRowMapper(),
                ordemServicoId);
    }

    private List<Orcamento> buscarOrcamentos(Long ordemServicoId) {
        return jdbcTemplate.query(
                "SELECT id, ordem_servico_id, versao, valor_servicos, valor_pecas, valor_insumos, valor_total, status, data_geracao, data_resposta, observacao_resposta FROM orcamento WHERE ordem_servico_id = ? ORDER BY versao",
                orcamentoRowMapper(),
                ordemServicoId);
    }

    private List<HistoricoStatus> buscarHistorico(Long ordemServicoId) {
        return jdbcTemplate.query(
                "SELECT id, ordem_servico_id, status_anterior, status_novo, data_alteracao, observacao FROM historico_status WHERE ordem_servico_id = ? ORDER BY data_alteracao, id",
                historicoStatusRowMapper(),
                ordemServicoId);
    }

    private RowMapper<ItemServico> itemServicoRowMapper() {
        return (rs, rowNum) -> new ItemServico(
                rs.getLong("id"),
                rs.getLong("ordem_servico_id"),
                rs.getLong("servico_id"),
                rs.getString("descricao"),
                rs.getBigDecimal("quantidade"),
                rs.getBigDecimal("valor_unitario"),
                rs.getBigDecimal("subtotal"),
                rs.getLong("tempo_estimado_segundos"));
    }

    private RowMapper<ItemMaterial> itemMaterialRowMapper() {
        return (rs, rowNum) -> new ItemMaterial(
                rs.getLong("id"),
                rs.getLong("ordem_servico_id"),
                rs.getLong("material_id"),
                TipoMaterial.valueOf(rs.getString("tipo")),
                rs.getString("descricao"),
                rs.getBigDecimal("quantidade"),
                rs.getBigDecimal("valor_unitario"),
                rs.getBigDecimal("subtotal"));
    }

    private RowMapper<Orcamento> orcamentoRowMapper() {
        return (rs, rowNum) -> new Orcamento(
                rs.getLong("id"),
                rs.getLong("ordem_servico_id"),
                rs.getInt("versao"),
                rs.getBigDecimal("valor_servicos"),
                rs.getBigDecimal("valor_pecas"),
                rs.getBigDecimal("valor_insumos"),
                rs.getBigDecimal("valor_total"),
                StatusOrcamento.valueOf(rs.getString("status")),
                rs.getObject("data_geracao", OffsetDateTime.class),
                rs.getObject("data_resposta", OffsetDateTime.class),
                rs.getString("observacao_resposta"));
    }

    private RowMapper<HistoricoStatus> historicoStatusRowMapper() {
        return (rs, rowNum) -> new HistoricoStatus(
                rs.getLong("id"),
                rs.getLong("ordem_servico_id"),
                converterStatusAnterior(rs.getString("status_anterior")),
                StatusOrdemServico.valueOf(rs.getString("status_novo")),
                rs.getObject("data_alteracao", OffsetDateTime.class),
                rs.getString("observacao"));
    }

    private StatusOrdemServico converterStatusAnterior(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return StatusOrdemServico.valueOf(valor);
    }
}
