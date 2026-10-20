package br.com.fiap.techchallengefiap.application.ordemservico;

import br.com.fiap.techchallengefiap.domain.cliente.repository.ClienteRepository;
import br.com.fiap.techchallengefiap.domain.ordemservico.repository.OrdemServicoRepository;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import br.com.fiap.techchallengefiap.presentation.rest.ordemservico.PublicOrdemServicoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultarOrdemServicoPublicaUseCase {
    private final OrdemServicoRepository repository;
    private final ClienteRepository clienteRepository;

    public ConsultarOrdemServicoPublicaUseCase(OrdemServicoRepository repository, ClienteRepository clienteRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public PublicOrdemServicoResponse executar(String numero, String documento) {
        var os = repository.buscarPorNumero(numero)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de servico nao encontrada."));

        var cliente = clienteRepository.buscarPorId(os.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente da ordem de servico nao encontrado."));

        String documentoNormalizado = documento == null ? "" : documento.replaceAll("\\D", "");
        if (!cliente.getDocumento().equals(documentoNormalizado)) {
            throw new ResourceNotFoundException("Ordem de servico nao encontrada.");
        }

        return PublicOrdemServicoResponse.from(os);
    }
}
