# Plano de Implementação de Usuários e Controle de Acesso

## Objetivo

Substituir a autenticação em memória por usuários persistidos, habilitar autorização por perfil (ADMIN, FUNCIONARIO e CLIENTE) e disponibilizar operações administrativas de gestão de usuários sem expor credenciais.

## Análise do estado atual

- A aplicação usa Spring Security, BCrypt e JWT, mas o `UserDetailsService` é baseado em `InMemoryUserDetailsManager` com um administrador configurado por propriedades.
- O login está em `POST /api/v1/auth/login`. O token contém as authorities do usuário na claim `scope`, mas a configuração atual protege os endpoints apenas com `authenticated()`, sem aplicar papéis aos recursos.
- `Cliente` já existe como entidade de domínio e tabela; ainda não há conta de usuário nem vínculo entre cliente e usuário.
- Flyway mantém migrations independentes em `db/migration/postgresql` e `db/migration/h2`, com Hibernate em `ddl-auto: validate`.
- A migration `V1__create_ordem_servico.sql` já existe. Portanto, a tabela de usuário deve entrar numa nova migration `V2` em ambos os diretórios, e não ser adicionada à V1 já versionada.
- O perfil `dev` atualmente contém valores padrão para o segredo JWT e a senha do administrador. A transição para autenticação persistida deve remover esses fallbacks inseguros; produção já exige configuração explícita.

## Escopo proposto

### Incluído

- Persistência de usuários com username único, hash BCrypt, papel, estado habilitado, timestamps e vínculo opcional e único com cliente.
- Autenticação de usuários persistidos e emissão de JWT com papel utilizável pela autorização Spring.
- Autorização explícita dos endpoints administrativos e operacionais por papel.
- CRUD administrativo de usuários, sem retornar ou permitir leitura do hash da senha.
- Criação inicial segura do administrador por configuração explícita, sem reinicializar ou sobrescrever sua senha em reinícios.
- Testes de domínio, persistência, autorização, API e migrations para H2 e PostgreSQL.

### Fora do escopo inicial

- Cadastro público/autônomo de contas.
- Refresh token, recuperação de senha, troca de senha autenticada e MFA.
- Consulta de OS pelo cliente. Essa funcionalidade depende de consultas com escopo obrigatório pelo `cliente_id` vinculado e deve ser entregue separadamente, com testes contra acesso cruzado entre clientes.

## Decisões e regras de negócio

1. `CLIENTE` deve estar vinculado a exatamente um cliente; `ADMIN` e `FUNCIONARIO` não podem ter vínculo com cliente.
2. A criação e edição de usuário nunca aceitam hash de senha. A API recebe senha em texto apenas na criação e persiste somente o resultado BCrypt; a resposta omite a senha.
3. `DELETE /api/v1/usuarios/{id}` desabilita a conta (`enabled=false`) em vez de apagar o registro, preservando histórico e referências. A reativação é feita por atualização administrativa.
4. Username é único sem distinção de maiúsculas/minúsculas, com normalização definida no domínio e restrição correspondente no banco.
5. O vínculo com cliente usa FK com exclusão restrita; cliente associado a usuário não pode ser removido fisicamente. O cliente existente é desativado conforme o fluxo de domínio atual.
6. O administrador inicial só é provisionado se todas as credenciais necessárias forem explicitamente configuradas. O provisionamento é idempotente e não altera um usuário já existente.
7. A política de acesso a clientes, veículos, OS e demais recursos deve ser declarada por endpoint/método. Não manter autorização genérica baseada apenas em usuário autenticado. Consultas de CLIENTE para OS ficam fora desta entrega.

## Etapas de implementação

### 1. Fechar contratos e autorização

- Definir enum `Role`/`Perfil` com valores persistidos `ADMIN`, `FUNCIONARIO` e `CLIENTE`.
- Catalogar os endpoints existentes por método HTTP e definir quais papéis podem invocá-los; manter rotas públicas existentes apenas se intencionais.
- Definir DTOs de criação, atualização e resposta; não compartilhar entidade JPA ou credenciais com a camada REST.
- Documentar o contrato de vínculo: criação de conta CLIENTE referencia um cliente existente e não cria/edita dados cadastrais do cliente implicitamente.

**Aceite:** matriz de acesso aprovada e contratos não expõem `passwordHash`.

### 2. Criar schema e persistência

- Adicionar `V2__create_usuario.sql` nas localizações PostgreSQL e H2.
- Criar tabela com `id`, `username`, `password_hash`, `role`, `enabled`, `cliente_id`, `created_at` e `updated_at`.
- Adicionar unicidade do username normalizado, unicidade de `cliente_id`, FK para `cliente` e checks para valores de papel e coerência entre papel e vínculo.
- Implementar modelo de domínio, entidade JPA, repositório Spring Data, porta de domínio e adapter seguindo o padrão já usado em `Cliente`.
- Cobrir conversões entre enum, timestamps e persistência e verificar migrations com Hibernate em modo `validate`.

**Aceite:** migrations aplicam em H2 e PostgreSQL; restrições impedem usernames/vínculos duplicados e papéis ou associações inválidas.

### 3. Implementar casos de uso

- Implementar criação, consulta paginada/listagem, busca por id, atualização, ativação e desativação.
- Validar papel e vínculo com cliente; rejeitar CLIENTE sem cliente e demais papéis com cliente.
- Codificar senha com o `PasswordEncoder` existente antes de persistir; não permitir atualizar senha pelo endpoint geral de edição.
- Traduzir conflitos de unicidade e recursos inexistentes para as exceções HTTP já tratadas pela API.
- Usar transações nos casos que alteram mais de uma estrutura.

**Aceite:** regras são verificadas nos casos de uso e senhas armazenadas nunca são iguais às senhas recebidas.

### 4. Integrar autenticação persistida e bootstrap

- Substituir o `InMemoryUserDetailsManager` por `UserDetailsService` que consulta o repositório de usuários habilitados.
- Garantir que contas desabilitadas ou inexistentes não autenticam; preservar BCrypt e a resposta/fluxo do endpoint atual de login.
- Provisionar administrador inicial de forma idempotente a partir de credenciais explicitamente configuradas, sem fallback para `admin/admin` ou segredo JWT conhecido.
- Emitir claim de papel no JWT e configurar o conversor do resource server para mapear essa claim às authorities esperadas por Spring Security. Manter compatibilidade do contrato `scope` apenas se necessária e testada.
- Verificar que falhas de configuração obrigatória impedem inicialização nos perfis não-testes.

**Aceite:** login de usuário persistido habilitado emite token válido; senha inválida, usuário desabilitado e credenciais ausentes falham sem autenticação.

### 5. Aplicar autorização e expor API de usuários

- Aplicar regras explícitas por método/rota para ADMIN, FUNCIONARIO e CLIENTE conforme a matriz da etapa 1; não deixar operações administrativas acessíveis a qualquer autenticado.
- Proteger `POST/GET/PUT/DELETE /api/v1/usuarios` como operações exclusivas de ADMIN (com DELETE realizando desativação).
- Adicionar validação de entrada, respostas consistentes, códigos HTTP e documentação OpenAPI.
- Assegurar que o usuário não escolha livremente privilégios por endpoints públicos; criação e alteração de papéis são exclusivamente administrativas.
- Revisar operações de gestão de usuários para evitar desabilitar a própria última conta ADMIN ativa.

**Aceite:** ADMIN gerencia usuários; papéis sem permissão recebem 403; chamadas sem token recebem 401; nenhuma resposta inclui senha ou hash.

### 6. Testar e atualizar documentação

- Testes unitários para normalização, unicidade, papéis, vínculo cliente e ativação/desativação.
- Testes de integração para migrations e repositório nos dois bancos suportados.
- Testes de API/Spring Security para login, token/authorities, CRUD, 401/403 e acesso por endpoint/papel.
- Testes de regressão para rotas públicas existentes e para os fluxos atuais de clientes e OS.
- Atualizar documentação de configuração (`JWT_SECRET`, credenciais de bootstrap, perfis e permissões) e exemplos de chamadas; nunca documentar valores secretos reutilizáveis.

**Aceite final:** `mvn clean verify` passa; aplicações novas iniciam com migrations válidas; usuários são autenticados pelo banco; endpoints respeitam a matriz de permissões.

## Principais riscos e mitigação

| Risco | Mitigação |
|---|---|
| Divergência de schema entre H2 e PostgreSQL | Manter migrations V2 equivalentes e executar testes nos dois mecanismos. |
| JWT válido sem authorities corretas | Testar conversão da claim até `hasRole`/`hasAuthority` em requisições reais. |
| Escalada de privilégio por edição de perfil | Restringir gestão de papéis a ADMIN e validar no servidor. |
| Exposição de senha ou hash em JSON/logs | DTOs dedicados, testes de serialização e não registrar credenciais. |
| Acesso de cliente a dados de outro cliente | Não incluir consulta de OS de CLIENTE nesta fase; quando implementada, derivar o escopo do usuário autenticado e testar isolamento. |
| Perda de acesso após remover usuário em memória | Criar o administrador inicial no banco por bootstrap explícito e idempotente antes de remover o mecanismo em memória. |
| Credenciais padrão continuarem ativas em desenvolvimento | Remover defaults inseguros e exigir configuração explícita fora do perfil de teste. |

## Ordem sugerida de entrega

1. Contratos, matriz de acesso e migration V2.
2. Modelo, repositório e casos de uso.
3. Login persistido e provisionamento do administrador.
4. Autorização por endpoint e API administrativa.
5. Testes de ponta a ponta, validação de ambos os bancos e documentação.
