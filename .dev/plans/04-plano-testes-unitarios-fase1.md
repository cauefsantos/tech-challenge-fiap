# Plan: Testes unitários da Fase 1

> Base: `02-plano-fase1-revisado.md` (Fase 5) e `docs/Fase 1.md`. Revisão: Yoda, resultado OKAY (revisão estática: caminhos, referências arquivo:linha e coerência dos cenários; os comandos de verificação não foram executados).
> Decisões em aberto assumidas com a opção recomendada, a confirmar pelo grupo: (1) 80% de instruções separado em `domain.*` e `application.*`; (2) `jacoco:check` quebra o `verify`; (3) TestNG removido se não houver uso; (4) `infrastructure` e `presentation` fora da meta unitária.

## Contexto

Este plano amplia a cobertura unitária da aplicação Spring Boot sem alterar regras de produção. A Fase 1 exige testes unitários e de integração dos fluxos principais, validação de CPF/CNPJ e placa, cobertura mínima de 80% nos domínios críticos, evidência de cobertura e instruções de execução (`docs/Fase 1.md:127-153`, `.dev/Checklist_Fase1_Tech_Challenge.md:127-145`).

### Escopo objetivo

- **Domínios críticos contratados para este plano:** `br.com.fiap.techchallengefiap.domain.*` e `br.com.fiap.techchallengefiap.application.*`, medidos **separadamente** por instruções, cada um com mínimo de 80%.
  - Justificativa: `domain` contém invariantes, entidades e transições de negócio; `application` orquestra os casos de uso, persistência e as decisões de estoque. Essa divisão respeita a arquitetura em camadas descrita em `README.md:22-29` e a meta já adotada em `.dev/plans/02-plano-fase1-revisado.md:136-145`.
- **Foco:** testes unitários JUnit 5 isolados. Domínio sem Spring; application com Mockito, `@ExtendWith(MockitoExtension.class)`, `@Mock` e `@InjectMocks`, conforme `src/test/java/br/com/fiap/techchallengefiap/application/GerenciarEntidadesUseCaseTest.java:13-34`.
- **Complementos necessários, mas fora da implementação deste plano:** o teste de integração JPA existente e novos fluxos MockMvc/transacionais. Eles continuam necessários para a Fase 1, porém não devem ser usados para alegar que o escopo unitário ou rollback real foi comprovado.
- **Exclusões da métrica obrigatória:** classe main, DTOs/records de request/response e entidades JPA. Como o gate será recortado em `domain.*` e `application.*`, controllers/adapters/JPA ficam naturalmente fora; nenhuma entidade de domínio, caso de uso ou regra de negócio poderá ser excluída para elevar a métrica.

### Estado inicial e premissas verificadas

- O baseline histórico de 15 testes e 39,8% de instruções (`.dev/plans/02-plano-fase1-revisado.md:10-11`) **não foi reexecutado nesta sessão**. Portanto, a cobertura atual é **não medida** até a Task 1.1.
- O projeto declara Java 21 (`pom.xml:30-34`), mas a máquina pode usar Java 8. A execução deve definir `JAVA_HOME=C:\Users\gabriel.costa\JAVAS\jdk-21.0.9+10` antes de chamar o Maven Wrapper.
- O JaCoCo atual só prepara o agente e produz relatório no ciclo `test`; não há `jacoco:check` nem limite (`pom.xml:132-150`).
- TestNG 7.12.0 está em `compile` (`pom.xml:118-123`), embora os testes inspecionados usem JUnit 5/Mockito. A decisão sobre removê-lo ou mudar escopo é obrigatória antes de alterar o POM.
- Há uma inconsistência de organização: `src/test/java/br/com/fiap/techchallengefiap/model/OrdemServicoTest.java` declara o pacote `br.com.fiap.techchallengefiap.domain.ordemservico.model` (`:1`), devendo residir no diretório compatível com o pacote.

### Convenções obrigatórias para os novos testes

- Métodos em português, camelCase e expressivos, como `criaClienteQuandoDocumentoAindaNaoEstaEmUso` e `deveCancelarOsQuandoOrcamentoERejeitado` (`GerenciarEntidadesUseCaseTest.java:37-55`, `OrdemServicoTest.java:30-40`).
- Assertions de `org.junit.jupiter.api.Assertions`; Mockito apenas na camada application. Nenhum `@SpringBootTest`, contexto Spring ou banco em teste unitário.
- Criar fixtures específicas por contexto, não uma classe genérica `utils`: uma fixture de domínio da OS em `.../domain/ordemservico/model/OrdemServicoFixture.java` e, se o volume justificar, uma fixture dos casos de uso de OS em `.../application/ordemservico/OrdemServicoUseCaseFixture.java`.
- Evitar `sleep`. Para campos produzidos por `OffsetDateTime.now()` (por exemplo, `OrdemServico.java:43,201,208,213`), verificar presença/ordem ou uma janela capturada antes/depois; para reconstituição, usar os construtores que recebem datas fixas.
- Um teste que revelar possível defeito de produção deve registrar cenário, regra esperada, comportamento observado e impacto. Não alterar produção nem ajustar a expectativa para ocultar o defeito sem aprovação explícita.

## Tasks

### Wave 1: Baseline, contrato de cobertura e organização (sequencial)

- [ ] **Task 1.1: Medir o baseline real com o JDK correto antes de mudanças.**
  - Categoria: validação
  - Arquivos: gerar apenas artefatos transitórios em `target/`; registrar resultado no PR/issue de execução, sem tratar números históricos como resultado novo.
  - Classes cobertas: todas, apenas para medição inicial.
  - Procedimento:
    1. No PowerShell, definir `JAVA_HOME` para `C:\Users\gabriel.costa\JAVAS\jdk-21.0.9+10` e prefixar `JAVA_HOME\bin` em `Path` na sessão.
    2. Confirmar `java -version` e `./mvnw.cmd -version` apontando para Java 21.
    3. Executar a suíte limpa e o ciclo de verificação para gerar um relatório novo.
    4. Registrar revisão testada, comando, quantidade de testes executados, falhas e índices JaCoCo por `domain.*` e `application.*` quando disponíveis.
  - Cenários: `executaSuiteComJdk21`; `falhaDeAmbienteNaoEInterpretadaComoFalhaDeCobertura`.
  - Critério de aceite: execução válida com mais de zero testes, versão efetiva do Java 21 e relatório associado à mesma execução. Se bloqueada, registrar **não validado**, causa e pendência; não informar 39,8% como baseline atual.
  - Comandos de verificação:
    ```powershell
    $env:JAVA_HOME = 'C:\Users\gabriel.costa\JAVAS\jdk-21.0.9+10'
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
    java -version
    .\mvnw.cmd -version
    .\mvnw.cmd clean verify
    ```

- [ ] **Task 1.2: Decidir e corrigir a configuração de dependência e gate JaCoCo.**
  - Categoria: build
  - Arquivos: alterar `C:\Users\gabriel.costa\pessoal\tech-challenge-fiap\pom.xml`.
  - Classes cobertas: configuração de build; não introduzir testes de negócio nesta tarefa.
  - Procedimento:
    1. Buscar usos de `org.testng` em `src/main` e `src/test`. Se não houver uso, remover a dependência TestNG de `compile`; se houver motivo comprovado para mantê-la, restringi-la a `test` e documentar o motivo.
    2. Manter `prepare-agent`; vincular relatório a `verify` ou garantir que o relatório usado como evidência seja produzido pela mesma execução que avalia o gate.
    3. Configurar duas execuções de `jacoco:check` vinculadas a `verify`: uma incluindo `br/com/fiap/techchallengefiap/domain/**` e outra `br/com/fiap/techchallengefiap/application/**`. Cada execução deve ter regra de elemento `BUNDLE`, contador `INSTRUCTION`, valor `COVEREDRATIO` e mínimo `0.80`.
    4. Aplicar exclusões restritas e justificadas para a classe main, DTOs/records e entidades JPA, sem excluir modelos de domínio ou casos de uso. Confirmar que os includes não tornaram o bundle vazio.
  - Cenários: `falhaBuildQuandoDomainFicaAbaixoDeOitentaPorCento`; `falhaBuildQuandoApplicationFicaAbaixoDeOitentaPorCento`; `naoAceitaApenasRelatorioSemCheck`.
  - Critério de aceite: `verify` avalia os dois gates e falha quando qualquer recorte ficar abaixo de 80%; o log identifica a regra executada. A configuração não depende de TestNG em produção.
  - Comando de verificação: o comando da Task 1.1, seguido de inspeção de `target/site/jacoco/index.html` e do log contendo as duas avaliações `jacoco:check`.

- [ ] **Task 1.3: Organizar a suíte existente e criar fixtures locais de teste.**
  - Categoria: testes-unitários
  - Arquivos:
    - mover `C:\Users\gabriel.costa\pessoal\tech-challenge-fiap\src\test\java\br\com\fiap\techchallengefiap\model\OrdemServicoTest.java` para `C:\Users\gabriel.costa\pessoal\tech-challenge-fiap\src\test\java\br\com\fiap\techchallengefiap\domain\ordemservico\model\OrdemServicoTest.java` sem mudar seu pacote;
    - substituir gradualmente o agrupador `...\domain\Fase1EntidadesTest.java` por testes focados nos pacotes das classes cobertas;
    - criar `...\domain\ordemservico\model\OrdemServicoFixture.java` e, somente se necessário pelos mocks compartilhados, `...\application\ordemservico\OrdemServicoUseCaseFixture.java`.
  - Classes cobertas: preservação dos cenários já existentes de `Cliente`, `Veiculo`, `Servico`, `Material`, `ItemServico`, `ItemMaterial`, `Orcamento` e `OrdemServico`.
  - Cenários: `preservaCenariosExistentesAoSepararArquivos`; `criaAgregadosPersistidosParaCenariosQueExigemId`; `reutilizaDatasFixasSemSleep`.
  - Critério de aceite: não restam diretório e declaração de pacote divergentes; não há duplicação de testes já existentes; fixtures permanecem no contexto de domínio/application correspondente e não introduzem dependência Spring.
  - Comando de verificação: `./mvnw.cmd -Dtest=OrdemServicoTest,Fase1EntidadesTest,GerenciarEntidadesUseCaseTest test` durante a migração, seguido de `./mvnw.cmd test` após remover/substituir o agrupador.

### Wave 2: Domínio crítico (pode executar em paralelo após a Wave 1)

- [ ] **Task 2.1: Cobrir Cliente e Veiculo, incluindo documentos e placas obrigatórios.**
  - Categoria: testes-unitários
  - Arquivos:
    - criar/alterar `C:\Users\gabriel.costa\pessoal\tech-challenge-fiap\src\test\java\br\com\fiap\techchallengefiap\domain\cliente\model\ClienteTest.java`;
    - criar/alterar `C:\Users\gabriel.costa\pessoal\tech-challenge-fiap\src\test\java\br\com\fiap\techchallengefiap\domain\veiculo\model\VeiculoTest.java`.
  - Classes de produção: `domain/cliente/model/Cliente.java:18-97`, `TipoPessoa`; `domain/veiculo/model/Veiculo.java:18-48`.
  - Cenários de `ClienteTest`:
    - `criaPessoaFisicaComCpfMascaradoValido`;
    - `criaPessoaJuridicaComCnpjMascaradoValido`;
    - `rejeitaCpfInvalidoParaPessoaFisica`;
    - `rejeitaCnpjInvalidoParaPessoaJuridica`;
    - `rejeitaDocumentoComCaracteresNaoPermitidosOuTipoIncompativel`;
    - `rejeitaTipoDePessoaOuNomeAusentes`;
    - `normalizaEmailETelefoneQuandoInformados`;
    - `permiteEmailETelefoneAusentes` e `rejeitaEmailInvalido`;
    - `alteraAtivacaoSemAlterarDadosHistoricos`.
  - Cenários de `VeiculoTest`:
    - `normalizaPlacaAntigaAbc1234`;
    - `normalizaPlacaMercosulAbc1d23`;
    - `rejeitaPlacaInvalidaOuAusente`;
    - `rejeitaClienteIdInvalido`;
    - `rejeitaMarcaOuModeloAusentes`;
    - `aceitaAnoNosLimitesPermitidos`;
    - `rejeitaAnoAnteriorA1886OuPosteriorAoLimite`;
    - `alteraAtivacaoDoVeiculo`.
  - Critério de aceite: CPF/CNPJ válidos, inválidos e mascarados, placas antiga e Mercosul, e todos os limites de criação acima estão cobertos sem Spring; datas de cadastro são verificadas sem suposição de milissegundo exato.
  - Comando de verificação: `./mvnw.cmd -Dtest=ClienteTest,VeiculoTest test`.

- [ ] **Task 2.2: Cobrir Servico, Material, itens e HistoricoStatus.**
  - Categoria: testes-unitários
  - Arquivos:
    - criar `...\src\test\java\br\com\fiap\techchallengefiap\domain\servico\model\ServicoTest.java`;
    - criar `...\domain\material\model\MaterialTest.java`;
    - criar `...\domain\ordemservico\model\ItemServicoTest.java`;
    - criar `...\domain\ordemservico\model\ItemMaterialTest.java`;
    - criar `...\domain\ordemservico\model\HistoricoStatusTest.java`.
  - Classes de produção: `Servico.java:21-50`, `Material.java:23-72`, `ItemServico.java:19-46`, `ItemMaterial.java:20-51`, `HistoricoStatus.java:16-29`.
  - Cenários de `ServicoTest`: `criaServicoComDadosValidosEArredondaValorBase`; `rejeitaCodigoOuNomeEmBranco`; `rejeitaValorNuloOuNegativo`; `rejeitaTempoNuloZeroOuNegativo`; `alteraAtivacaoDoServico`.
  - Cenários de `MaterialTest`: `criaMaterialComDadosValidosEArredondaValorUnitario`; `rejeitaCamposObrigatoriosOuValoresNegativos`; `validaDisponibilidadeComEstoqueSuficiente`; `rejeitaQuantidadeNaoPositivaEstoqueInsuficienteOuMaterialInativo`; `consomeEstoqueSomenteAposValidacao`; `alteraAtivacaoDoMaterial`.
  - Cenários de `ItemServicoTest`: `criaSnapshotDePrecoDescricaoETempoDoServico`; `rejeitaServicoInexistenteOuInativo`; `rejeitaQuantidadeNaoPositiva`; `rejeitaReconstrucaoComServicoPrecoTempoOuSubtotalInvalidos`.
  - Cenários de `ItemMaterialTest`: `criaSnapshotDeTipoPrecoEDescricaoDoMaterial`; `rejeitaMaterialInexistenteQuantidadeInvalidaOuIndisponivel`; `calculaSubtotalDoItemMaterial`; `rejeitaReconstrucaoComDadosOuSubtotalInvalidos`; `rejeitaConsumoComMaterialDiferenteDoItem`.
  - Cenários de `HistoricoStatusTest`: `criaHistoricoComStatusNovoEObservacaoNormalizada`; `rejeitaStatusNovoOuDataAusentes`; `permiteStatusAnteriorNuloNaPrimeiraTransicao`.
  - Critério de aceite: snapshots permanecem iguais após a criação do item, subtotais são calculados em duas casas, e cada operação inválida lança a exceção de domínio esperada. Registrar como achado separado o setter vazio de `HistoricoStatus.setId` (`HistoricoStatus.java:32`) em vez de modificar produção neste escopo.
  - Comando de verificação: `./mvnw.cmd -Dtest=ServicoTest,MaterialTest,ItemServicoTest,ItemMaterialTest,HistoricoStatusTest test`.

- [ ] **Task 2.3: Cobrir Orcamento e a matriz completa da OrdemServico.**
  - Categoria: testes-unitários
  - Arquivos:
    - criar `...\src\test\java\br\com\fiap\techchallengefiap\domain\orcamento\model\OrcamentoTest.java`;
    - alterar/mover `...\src\test\java\br\com\fiap\techchallengefiap\domain\ordemservico\model\OrdemServicoTest.java`;
    - usar `...\domain\ordemservico\model\OrdemServicoFixture.java` da Task 1.3.
  - Classes de produção: `Orcamento.java:26-104`, `StatusOrcamento`; `OrdemServico.java:34-274`, `StatusOrdemServico`.
  - Cenários de `OrcamentoTest`:
    - `calculaValoresDeServicosPecasInsumosETotal`;
    - `rejeitaOrdemServicoOuVersaoNaoPositiva`;
    - `geraOrcamentoPendenteComDataDeGeracao`;
    - `disponibilizaSomenteOrcamentoPendente`;
    - `aprovaOuRejeitaSomenteOrcamentoDisponibilizado`;
    - `normalizaObservacaoDaResposta`;
    - `cancelaSomenteOrcamentoAindaNaoAprovadoOuCancelado`;
    - `atualizaValoresSomenteEmEstadosEditaveis`;
    - `rejeitaListasNulasOuTotalReconstruidoDivergente`.
  - Cenários de `OrdemServicoTest`:
    - `iniciaRecebidaComNumeroEValorZero` e `rejeitaClienteOuVeiculoInvalidos`;
    - `transicionaRecebidaParaDiagnostico`; `transicionaDiagnosticoParaAguardandoAprovacao`; `transicionaAguardandoAprovacaoParaExecucao`; `transicionaExecucaoParaFinalizada`; `transicionaFinalizadaParaEntregue`; `transicionaAguardandoAprovacaoParaCanceladaQuandoRejeita`;
    - `rejeitaCadaTransicaoEmEstadoInvalido`;
    - `registraHistoricoEmCadaTransicaoComStatusAnteriorENovo`;
    - `registraEConcluiDiagnosticoSomenteEmDiagnostico`;
    - `adicionaItensSomenteEmDiagnostico`; `rejeitaItemNuloOuDeOutraOrdem`; `removeItensERecalculaValorTotal`; `rejeitaRemocaoDeItemInexistente`;
    - `geraOrcamentosComVersoesCrescentes`; `rejeitaGeracaoSemIdPersistidoOuForaDeDiagnostico`;
    - `rejeitaDisponibilizacaoDeOrcamentoDeOutraOs`; `rejeitaValorNuloNegativoOuDivergenteDaSomaDosItens`;
    - `aprovaOrcamentoUmaUnicaVezEIniciaExecucao`; `rejeitaAprovacaoDuplicadaOuOrcamentoNaoPertencente`;
    - `cancelaOsQuandoOrcamentoERejeitado`; `rejeitaRejeicaoForaDeAguardandoAprovacao`;
    - `impedeFinalizacaoSemAprovacaoOuSemItemServico`;
    - `impedeEdicaoAposAprovacaoEntregaOuCancelamento`;
    - `atualizaOrcamentosPendentesOuDisponibilizadosQuandoItensMudam`;
    - `carregaItensMateriaisOrcamentosEHistoricoPersistidosSubstituindoConteudoAnterior` e `carregaListasNulasComoVazias`.
  - Critério de aceite: todas as transições válidas e inválidas do fluxo `RECEBIDA → EM_DIAGNOSTICO → AGUARDANDO_APROVACAO → EM_EXECUCAO → FINALIZADA → ENTREGUE`, mais `CANCELADA` por rejeição, possuem asserções de estado, histórico e efeitos colaterais permitidos. Se os testes expuserem a não-atomicidade potencial de `disponibilizarOrcamento` antes da validação do valor (`OrdemServico.java:131-137`) ou a semântica de `concluirDiagnostico` (`:83-86`), registrar como achado, sem corrigir.
  - Comando de verificação: `./mvnw.cmd -Dtest=OrcamentoTest,OrdemServicoTest test`.

### Wave 3: Casos de uso críticos (pode executar em paralelo após a Wave 1; Task 3.3 depende das fixtures da Task 1.3)

- [ ] **Task 3.1: Cobrir CRUDs de cliente, veículo, serviço e material com Mockito.**
  - Categoria: testes-unitários
  - Arquivos:
    - dividir/alterar `...\src\test\java\br\com\fiap\techchallengefiap\application\GerenciarEntidadesUseCaseTest.java` em:
      - `...\application\cliente\GerenciarClienteUseCaseTest.java`;
      - `...\application\veiculo\GerenciarVeiculoUseCaseTest.java`;
      - `...\application\servico\GerenciarServicoUseCaseTest.java`;
      - `...\application\material\GerenciarMaterialUseCaseTest.java`.
  - Classes de produção: `GerenciarClienteUseCase.java:21-58`, `GerenciarVeiculoUseCase.java:24-75`, `GerenciarServicoUseCase.java:24-67`, `GerenciarMaterialUseCase.java:22-59`.
  - Cenários de cliente: `criaClienteQuandoDocumentoAindaNaoEstaEmUso`; `impedeCriacaoComDocumentoAtivoDuplicado`; `buscaOuInformaClienteNaoEncontrado`; `listaClientesDoRepositorio`; `atualizaPreservandoIdDataEAtivacao`; `impedeAtualizacaoComDocumentoDeOutroCliente`; `desativaCliente`; `impedeReativacaoComDocumentoEmUso`.
  - Cenários de veículo: `criaVeiculoParaClienteAtivo`; `impedeCriacaoParaClienteInexistenteOuInativo`; `impedePlacaAtivaDuplicada`; `buscaListaEAtualizaVeiculo`; `impedeAtualizacaoComPlacaDeOutroVeiculo`; `desativaVeiculo`; `impedeReativacaoComClienteInativoOuPlacaEmUso`.
  - Cenários de serviço: `criaServicoComTempoEmSegundos`; `rejeitaTempoEstimadoAusente`; `impedeCodigoDuplicadoNaCriacaoOuAtualizacao`; `buscaListaEInformaServicoNaoEncontrado`; `alteraAtivacaoDoServico`.
  - Cenários de material: `criaMaterialQuandoCodigoAindaNaoEstaEmUso`; `impedeCodigoDuplicadoNaCriacaoOuAtualizacao`; `buscaListaEInformaMaterialNaoEncontrado`; `atualizaPreservandoDadosDeCadastro`; `alteraAtivacaoDoMaterial`.
  - Critério de aceite: todo caminho de erro verifica `never().salvar(...)`; todo caminho válido verifica o objeto salvo e as consultas de unicidade aplicáveis. Cada caso de uso continua testado sem carregar contexto Spring.
  - Comando de verificação: `./mvnw.cmd -Dtest=GerenciarClienteUseCaseTest,GerenciarVeiculoUseCaseTest,GerenciarServicoUseCaseTest,GerenciarMaterialUseCaseTest test`.

- [ ] **Task 3.2: Cobrir criação, busca, consulta pública e métricas da OS.**
  - Categoria: testes-unitários
  - Arquivos:
    - criar `...\src\test\java\br\com\fiap\techchallengefiap\application\ordemservico\CriarOrdemServicoUseCaseTest.java`;
    - criar `...\application\ordemservico\BuscarOrdemServicoUseCaseTest.java`;
    - criar `...\application\ordemservico\ConsultarOrdemServicoPublicaUseCaseTest.java`;
    - criar `...\application\ordemservico\MetricasOrdemServicoUseCaseTest.java`.
  - Classes de produção: `CriarOrdemServicoUseCase.java:25-37`, `BuscarOrdemServicoUseCase.java:16-18`, `ConsultarOrdemServicoPublicaUseCase.java:20-34`, `MetricasOrdemServicoUseCase.java:20-40`.
  - Cenários de criação: `criaOsParaClienteEVeiculoAtivosEVinculados`; `impedeCriacaoQuandoClienteNaoExisteOuEstaInativo`; `impedeCriacaoQuandoVeiculoNaoExisteOuEstaInativo`; `impedeCriacaoQuandoVeiculoNaoPertenceAoCliente`; `naoSalvaQuandoPrecondicaoFalha`.
  - Cenários de busca: `retornaOsQuandoEncontrada`; `informaOsNaoEncontrada`.
  - Cenários públicos: `retornaSomenteDadosPublicosQuandoDocumentoConfereComOuSemMascara`; `retornaMesmoTipoEMensagemDeAusenciaParaDocumentoDivergenteEOsInexistente`; `retornaAusenciaQuandoClienteDaOsNaoExiste`; `respostaPublicaNaoPossuiDocumentoNomeEmailOuTelefone`.
  - Cenários de métricas: `retornaZeroAmostrasETempoNuloSemOrdensElegiveis`; `calculaTempoParaUmaOrdemFinalizada`; `calculaMediaInteiraParaVariasOrdensElegiveis`; `ignoraOrdensNaoFinalizadasOuSemDatasObrigatorias`.
  - Critério de aceite: a consulta pública não distingue OS inexistente de documento divergente pelo tipo/mensagem da falha e não expõe PII; a métrica sem amostras afirma `0` para quantidade e `null` para tempo, conforme `MetricasOrdemServicoUseCase.java:27-29` e o contrato de `.dev/plans/02-plano-fase1-revisado.md:123-125`.
  - Comando de verificação: `./mvnw.cmd -Dtest=CriarOrdemServicoUseCaseTest,BuscarOrdemServicoUseCaseTest,ConsultarOrdemServicoPublicaUseCaseTest,MetricasOrdemServicoUseCaseTest test`.

- [ ] **Task 3.3: Cobrir cada operação de OrdemServicoFluxoUseCase e a baixa de estoque.**
  - Categoria: testes-unitários
  - Arquivos:
    - criar `...\src\test\java\br\com\fiap\techchallengefiap\application\ordemservico\OrdemServicoFluxoUseCaseTest.java`;
    - usar/criar `...\application\ordemservico\OrdemServicoUseCaseFixture.java` somente para estados válidos de OS, orçamento e itens reconstruídos.
  - Classes de produção: `OrdemServicoFluxoUseCase.java:34-165`, com efeitos sobre `OrdemServico`, `Material` e repositórios de domínio.
  - Cenários de comandos simples:
    - `enviaParaDiagnosticoEConcluiPersistindoOs`;
    - `registraDiagnosticoEConcluiDiagnosticoPersistindoOs`;
    - `adicionaERemoveItemServicoQuandoServicoExiste`; `informaServicoNaoEncontrado`;
    - `adicionaERemoveItemMaterialQuandoMaterialExiste`; `informaMaterialNaoEncontrado`;
    - `listaOrdensDoRepositorio`;
    - `geraOrcamentoEPersisteOs`;
    - `disponibilizaOrcamentoEncontradoEPersisteOs`; `informaOrcamentoNaoEncontradoNaOs`;
    - `rejeitaOrcamentoECancelaOsPersistindoResultado`;
    - `finalizaEEntregaOsPersistindoResultado`;
    - `informaOsNaoEncontradaParaCadaOperacao`.
  - Cenários de aprovação e estoque:
    - `aprovaOrcamentoComEstoqueSuficienteConsomeESalvaCadaMaterialEAOs`;
    - `bloqueiaAprovacaoComEstoqueInsuficienteSemConsumirNemSalvarMateriaisOuOs`;
    - `listaTodosOsMateriaisInsuficientesNaMensagem`;
    - `interrompeQuandoMaterialDoItemNaoExisteSemSalvarMateriaisOuOs`;
    - `repassaRegraDeAprovacaoInvalidaSemPersistir`.
  - Critério de aceite: todo método público do caso de uso possui ao menos um cenário de sucesso ou de delegação aplicável e um cenário de ausência/regra inválida quando aplicável. No caso insuficiente, verificar `verify(material, never()).consumir(...)`, `verify(materialRepository, never()).salvar(...)` e `verify(repository, never()).salvar(...)`; teste Mockito não deve alegar rollback de banco real.
  - Comando de verificação: `./mvnw.cmd -Dtest=OrdemServicoFluxoUseCaseTest test`.

### Wave 4: Cobertura, documentação e evidência (depende das Waves 2 e 3)

- [ ] **Task 4.1: Ajustar lacunas reveladas pelo relatório e validar os dois gates.**
  - Categoria: validação
  - Arquivos: apenas os arquivos de teste criados nas Tasks 2.1-3.3 e `pom.xml` se a configuração do gate precisar de correção pontual; não alterar produção sem aprovação.
  - Classes cobertas: itens de `domain.*` e `application.*` abaixo de 80% no relatório da mesma execução.
  - Procedimento: analisar o relatório por pacote/classe, adicionar somente cenários de regra ainda descobertos e repetir o ciclo completo. Não elevar cobertura com testes vazios, exclusões amplas ou duplicação de caminho feliz.
  - Cenários: `atingeOitentaPorCentoEmDomain`; `atingeOitentaPorCentoEmApplication`; `mantemGateSeparadoParaCadaRecorte`.
  - Critério de aceite: execução `clean verify` válida, com mais de zero testes, passa os dois checks independentes e apresenta pelo menos 80% de instruções nos dois bundles. Se não atingir, registrar o pacote faltante e estado **não validado** em vez de declarar conformidade.
  - Comando de verificação: comando completo da Task 1.1 e abertura de `C:\Users\gabriel.costa\pessoal\tech-challenge-fiap\target\site\jacoco\index.html`.

- [ ] **Task 4.2: Documentar execução e preparar evidência auditável para a entrega.**
  - Categoria: documentação
  - Arquivos: alterar `C:\Users\gabriel.costa\pessoal\tech-challenge-fiap\README.md`; gerar relatório em `target/site/jacoco/` durante a verificação; preparar captura/arquivo de evidência para referência no PDF de entrega sem incluir segredos ou dados pessoais.
  - Classes cobertas: não aplicável; documenta a validação das Tasks 1-4.
  - Conteúdo obrigatório no README:
    - pré-requisito Java 21 e caminho/forma de selecionar o JDK em PowerShell;
    - comando de suíte completa com `./mvnw.cmd clean verify`;
    - explicação de que `verify` avalia dois gates JaCoCo de instruções, `domain.*` e `application.*`, ambos com mínimo de 80%;
    - localização do relatório HTML `target/site/jacoco/index.html`;
    - observação de que testes unitários são isolados e que a evidência de integração/transação é complementar.
  - Cenários: `readmePermiteExecutarComJdk21`; `evidenciaCorrespondeAMesmaRevisaoEExecucaoDoGate`; `evidenciaNaoExibeSegredos`.
  - Critério de aceite: README contém instruções reproduzíveis; a captura/relatório mostra revisão, data/comando, contagem de testes e os dois recortes JaCoCo da execução aprovada. Não publicar valores sensíveis presentes em configuração de teste.
  - Comando de verificação: `./mvnw.cmd clean verify`, depois abrir localmente `target/site/jacoco/index.html` e revisar a seção nova do README.

## Matriz de rastreabilidade

| Regra da Fase 1 / decisão | Classe de produção | Classe de teste planejada | Task |
|---|---|---|---|
| CPF/CNPJ válidos e inválidos | `domain/cliente/model/Cliente` | `domain/cliente/model/ClienteTest` | 2.1 |
| Placa antiga e Mercosul, ano válido | `domain/veiculo/model/Veiculo` | `domain/veiculo/model/VeiculoTest` | 2.1 |
| CRUD de clientes | `application/cliente/GerenciarClienteUseCase` | `application/cliente/GerenciarClienteUseCaseTest` | 3.1 |
| CRUD de veículos | `application/veiculo/GerenciarVeiculoUseCase` | `application/veiculo/GerenciarVeiculoUseCaseTest` | 3.1 |
| CRUD de serviços | `application/servico/GerenciarServicoUseCase` e `domain/servico/model/Servico` | `GerenciarServicoUseCaseTest`, `ServicoTest` | 2.2, 3.1 |
| CRUD/controle de estoque | `application/material/GerenciarMaterialUseCase`, `domain/material/model/Material` | `GerenciarMaterialUseCaseTest`, `MaterialTest` | 2.2, 3.1 |
| Snapshots e subtotais de serviços/materiais | `ItemServico`, `ItemMaterial` | `ItemServicoTest`, `ItemMaterialTest` | 2.2 |
| Orçamento automático, disponibilização, aprovação e rejeição | `Orcamento`, `OrdemServico` | `OrcamentoTest`, `OrdemServicoTest` | 2.3 |
| Fluxo RECEBIDA até ENTREGUE e CANCELADA | `OrdemServico` | `OrdemServicoTest` | 2.3 |
| Baixa na aprovação e bloqueio por estoque insuficiente | `OrdemServicoFluxoUseCase` | `OrdemServicoFluxoUseCaseTest` | 3.3 |
| Criação da OS com cliente/veículo válidos e vinculados | `CriarOrdemServicoUseCase` | `CriarOrdemServicoUseCaseTest` | 3.2 |
| Consulta pública sem enumeração ou PII | `ConsultarOrdemServicoPublicaUseCase` | `ConsultarOrdemServicoPublicaUseCaseTest` | 3.2 |
| Tempo médio de execução | `MetricasOrdemServicoUseCase` | `MetricasOrdemServicoUseCaseTest` | 3.2 |
| 80% nos domínios críticos, relatório e instruções | `pom.xml`, `README.md` | execução JaCoCo, sem classe de teste específica | 1.2, 4.1, 4.2 |
| Testes de integração dos fluxos principais | adapters/controllers e fluxo transacional | `OrdemServicoRepositoryAdapterIntegrationTest` existente; novos MockMvc/transacionais em plano separado | Fora do escopo |

## Dependências e paralelização

| Task | Depende de | Pode rodar em paralelo com | Estimativa |
|---|---|---|---|
| 1.1 | nenhuma | nenhuma | S |
| 1.2 | 1.1 | nenhuma | M |
| 1.3 | 1.1 | 1.2 após não haver conflito no POM | M |
| 2.1 | 1.3 | 2.2, 2.3, 3.1, 3.2 | M |
| 2.2 | 1.3 | 2.1, 2.3, 3.1, 3.2 | M |
| 2.3 | 1.3 | 2.1, 2.2, 3.1, 3.2 | L |
| 3.1 | 1.3 | 2.1, 2.2, 2.3, 3.2 | L |
| 3.2 | 1.3 | 2.1, 2.2, 2.3, 3.1 | M |
| 3.3 | 1.3 e fixture de OS disponível | 2.1, 2.2, 2.3, 3.1, 3.2 | L |
| 4.1 | 1.2, 2.1-3.3 | nenhuma | M |
| 4.2 | 4.1 | nenhuma | S |

## Riscos e tratamento

- **JDK divergente:** Java 8 pode produzir erros enganosos com recursos Java 21. Mitigação: Task 1.1 exige comprovação de `java -version` e Maven antes de medir.
- **Baseline histórico obsoleto:** 39,8% é referência, não resultado atual. Mitigação: declarar não medido até a execução válida.
- **Gate no escopo errado:** percentual global pode esconder aplicação abaixo de 80%. Mitigação: dois checks independentes de instruções por bundle, com inclusões e exclusões revisáveis.
- **Cobertura insuficiente mesmo com os cenários previstos:** mitigação: Task 4.1 decide a próxima lacuna pelo relatório real, sem excluir regras de negócio.
- **Bug preexistente revelado por teste:** exemplos potenciais incluem `concluirDiagnostico` não transicionar estado (`OrdemServico.java:83-86`), disponibilização antes de validar valor (`:131-137`) e `HistoricoStatus.setId` vazio (`HistoricoStatus.java:32`). Mitigação: registrar como achado reproduzível, interromper a alegação afetada e pedir aprovação antes de corrigir produção.
- **Falsa prova de rollback:** mocks verificam ausência de chamadas, não transação no banco. Mitigação: manter rollback/H2 e fluxo MockMvc como integração complementar, não como conclusão dos unitários.
- **Flakiness temporal:** `OffsetDateTime.now()` torna igualdade exata instável. Mitigação: fixtures com datas fixas e asserções de janela/presença, sem `sleep`.

## Fora de escopo / não comprovado

- Este plano se baseia na leitura dos arquivos locais e não executou a suíte nesta sessão. O baseline atual e a aprovação da meta de 80% estão **não medidos** até a Task 1.1.
- Não cria nem amplia testes de integração JPA, transacionalidade/rollback real, MockMvc, autenticação ou controllers. O teste `OrdemServicoRepositoryAdapterIntegrationTest` existente permanece integração, não unitário.
- Não comprova que todos os fluxos HTTP da Fase 1, a persistência completa do agregado, a segurança ou a entrega em PDF estão validados.
- Não altera regras de domínio, arquitetura, contratos, produção ou configurações de segurança além do POM/README estritamente necessários para o gate e instruções aprovados.

## Decisões em aberto para o usuário

1. Confirmar que o contrato é **80% de instruções separadamente** em `domain.*` e `application.*`, e não uma média global. Este plano assume essa opção por ser mais auditável.
2. Confirmar que o `jacoco:check` deve bloquear `mvn verify` ao ficar abaixo do mínimo. Este plano assume que sim; gerar somente relatório não atende o objetivo.
3. Autorizar remover TestNG se a busca não encontrar uso; alternativa é mantê-lo exclusivamente em escopo `test` com justificativa.
4. Confirmar que infrastructure/presentation continuam fora da meta unitária de 80%; elas devem ser tratadas por testes de integração/MockMvc em trabalho complementar.
5. Definir se os testes de adapter/controller serão classificados e planejados como integração em um plano separado, incluindo a prova de rollback real da baixa de estoque.

## Final Verification Wave

- [ ] **F1: Executar a suíte completa com JDK 21.**
  - Categoria: validação
  - Verificação: `./mvnw.cmd clean verify` com `JAVA_HOME` explicitamente configurado; confirmar mais de zero testes executados.
- [ ] **F2: Confirmar os dois gates JaCoCo.**
  - Categoria: validação
  - Verificação: log do `verify` contendo os checks de `domain.*` e `application.*`, ambos com `INSTRUCTION/COVEREDRATIO >= 0.80`; relatório em `target/site/jacoco/index.html` da mesma execução.
- [ ] **F3: Revisar isolamento e consistência.**
  - Categoria: revisão
  - Verificação: testes unitários sem Spring/H2, Mockito apenas nos casos de uso, métodos em português camelCase, diretório/pacote compatíveis e ausência de sleeps/segredos.
- [ ] **F4: Revisar rastreabilidade e requisitos originais.**
  - Categoria: revisão
  - Verificação: conferir a matriz contra as regras da Fase 1, README com JDK/comando/relatório, e listar explicitamente evidências ainda não validadas de integração.

## Agent Dispatch Summary

| Wave | Perfil recomendado | Entrega |
|---|---|---|
| Wave 1 | build + testes-unitários | baseline válido, gate JaCoCo e organização da suíte |
| Wave 2 | testes-unitários | testes determinísticos de entidades, itens, orçamento e OS |
| Wave 3 | testes-unitários | testes Mockito dos casos de uso e invariantes de orquestração |
| Wave 4 | validação + documentação | cobertura comprovada, README e evidência para o PDF |
| Final Verification | validação + revisão | prova executável, alcance e pendências declarados |
