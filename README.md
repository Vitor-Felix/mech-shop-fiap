# Vehicle Catalog API

API principal de cadastro e fonte de verdade de marca, modelo, ano, cor e preço dos veículos. O serviço de vendas recebe as alterações por HTTP e mantém uma projeção própria. Este projeto e `vehicle-sales-api` não compartilham banco, schema, usuário ou volume.

## Stack e responsabilidades

- Kotlin, Java 21, Spring Boot 4 e Gradle.
- H2 em memória no perfil `dev`; PostgreSQL no perfil `prod`.
- CRUD administrativo do catálogo e publicação versionada para o serviço de vendas.
- A compra, disponibilidade, pagamento e listagens de vendas pertencem exclusivamente ao `vehicle-sales-api`.

## Configuração segura

Credenciais administrativas, assinatura JWT, datasource e token interno são definidos no ambiente, nunca em arquivos versionados. O segredo JWT deve ter pelo menos 32 bytes aleatórios. Não reutilize credenciais ou banco do serviço de vendas.

Para executar localmente, informe um usuário administrativo, uma senha e gere uma chave JWT efêmera no terminal:

```bash
export ADMIN_USERNAME='<login administrativo local>'
read -r -s -p 'Senha administrativa: ' ADMIN_PASSWORD; printf '\n'; export ADMIN_PASSWORD
export JWT_SECRET="$(openssl rand -base64 48)"
```

Para configurar sincronização com `vehicle-sales-api`, defina também `VEHICLE_SALES_BASE_URL` e `MECH_SHOP_INTERNAL_TOKEN`. O token interno deve corresponder ao valor configurado no serviço de vendas. Sem a URL, o cadastro local continua disponível, mas a sincronização fica marcada como falha e pode ser repetida depois.

| Variável | Uso |
|---|---|
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | Credenciais do endpoint `/api/auth/login`. |
| `JWT_SECRET` | Chave aleatória de assinatura JWT, mínimo 32 bytes. |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Datasource do perfil `prod`; use um banco exclusivo do catálogo. |
| `VEHICLE_SALES_BASE_URL` | URL base do serviço de vendas. |
| `MECH_SHOP_INTERNAL_TOKEN` | Bearer token da integração HTTP. |
| `VEHICLE_SALES_CONNECT_TIMEOUT`, `VEHICLE_SALES_READ_TIMEOUT` | Timeouts finitos da integração (padrões: `3s` e `5s`). |

As variáveis do datasource são obrigatórias no perfil `prod`. Credenciais previamente expostas em histórico de código devem ser rotacionadas antes de qualquer implantação.

## Executar localmente

### Desenvolvimento com H2

Depois de definir `ADMIN_USERNAME`, `ADMIN_PASSWORD` e `JWT_SECRET` conforme acima:

```bash
./gradlew bootRun
```

O perfil `dev` usa H2 em memória e os dados são descartados ao reiniciar. A documentação Swagger fica em <http://localhost:8080/swagger-ui/index.html>.

### Compose com PostgreSQL

Defina as variáveis administrativas e JWT acima, mais credenciais exclusivas para o PostgreSQL do catálogo:

```bash
export POSTGRES_DB=mechshop
export POSTGRES_USER='<usuario exclusivo do catalogo>'
read -r -s -p 'Senha do PostgreSQL: ' POSTGRES_PASSWORD; printf '\n'; export POSTGRES_PASSWORD
docker compose up --build -d
docker compose logs -f vehicle-catalog-api
```

O PostgreSQL fica acessível somente pela rede interna do Compose; somente a API publica a porta `8080`. O volume persistente mantém o nome lógico `pgdata`. `docker compose down` para os containers sem apagar os dados. **Não use `docker compose down -v` para atualização ou deploy**; isso remove o volume.

Em volumes já inicializados, as variáveis `POSTGRES_USER`/`POSTGRES_PASSWORD` do Compose não alteram as credenciais existentes. Configure no ambiente as credenciais que correspondem ao volume, faça backup antes de atualizar e rotacione credenciais com um procedimento próprio do PostgreSQL.

## API principal e sincronização

As rotas administrativas exigem JWT obtido em `POST /api/auth/login` com `{"login":"...","pass":"..."}`:

| Método | Rota | Finalidade |
|---|---|---|
| `POST` | `/api/admin/vehicles` | Cadastrar veículo e solicitar sincronização. |
| `GET` | `/api/admin/vehicles` | Listar cadastros do catálogo. |
| `GET` | `/api/admin/vehicles/{id}` | Consultar cadastro. |
| `PUT` | `/api/admin/vehicles/{id}` | Editar dados e incrementar a versão sincronizada. |
| `POST` | `/api/admin/vehicles/{id}/sync` | Reenviar a versão atual sem incrementá-la. |

O payload cadastral contém `brand`, `model`, `year`, `color` e `price`. Respostas de criação/edição trazem `syncStatus` (`PENDING`, `SYNCED` ou `FAILED`) e `syncError` sanitizado. HTTP `201`/`200` confirma o cadastro local; apenas `syncStatus: "SYNCED"` confirma a publicação remota. Falhas não removem o cadastro e podem ser repetidas pela rota de sync.

## Testes, cobertura e build

```bash
./gradlew --no-daemon clean check bootJar
```

`check` executa os testes e falha se a cobertura de linhas JaCoCo ficar abaixo de 80%. O relatório HTML fica em `build/reports/jacoco/test/html/index.html`. A imagem Docker compila o artefato executável nomeado, sem wildcard ambíguo; a CI roda testes/cobertura antes de validar o build Java.

Nos workflows do GitHub Actions, `CI` executa testes, cobertura, `bootJar` e `docker build`; o check do job de build aparece como `verify`. O workflow `Test Coverage` executa a verificação JaCoCo de 80% em separado; seu check aparece como `coverage`. Ambos rodam em PRs e pushes para `develop` e `main`. Configure `verify` e `coverage` como status checks obrigatórios nas regras de proteção das branches.

```bash
docker build --no-cache -t vehicle-catalog-api:local .
docker compose config --quiet
```

## Kubernetes e entrega

`k8s/base/` contém Deployment com duas réplicas, Service interno `ClusterIP`, recursos, probes de saúde e configuração por ConfigMap/Secret. A imagem padrão `vehicle-catalog-api:latest` é um identificador local substituível via Kustomize. O banco PostgreSQL é externo aos manifests e deve ser provisionado exclusivamente para este serviço; não é compartilhado com `vehicle-sales-api`.

Antes de aplicar os manifests:

1. Publique a imagem em um registry escolhido pela equipe e substitua a imagem no overlay Kustomize de deploy.
2. Crie no namespace um Secret chamado `vehicle-catalog-api-secrets`, fora do Git/manifest, com as chaves `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `JWT_SECRET` e `MECH_SHOP_INTERNAL_TOKEN`.
3. Configure `VEHICLE_SALES_BASE_URL` no ConfigMap `vehicle-catalog-api-config` se a integração for habilitada.
4. Aponte o datasource para a base PostgreSQL isolada do catálogo e aplique `kubectl apply -k k8s/base/`.

Os workflows em `.github/workflows/` validam Pull Requests e pushes para `develop` e `main`. A CI bloqueia PRs direcionados a `main` quando a origem não é a branch `develop`; configure `enforce-main-source`, `verify` e `coverage` como checks obrigatórios na proteção de `main`. **A publicação de imagem e o CD para cluster ainda não estão habilitados:** este repositório não define registry, provedor/cluster ou credenciais. A escolha de destino e a configuração de secrets/variáveis de ambiente do GitHub e do cluster são necessárias antes de automatizar o deploy; não se presume um deploy operacional.

## Migrações e preservação de dados

No perfil `prod`, Flyway executa migrações versionadas e Hibernate valida o schema (`ddl-auto: validate`), sem atualizar/apagar tabelas automaticamente. `V1__catalog_baseline.sql` cria o schema para uma base nova. Em uma base não vazia sem histórico Flyway, `baseline-on-migrate` registra a versão 1 como baseline e não executa V1 sobre os dados existentes. Faça backup e confirme que o schema legado corresponde ao modelo antes de atualizar; schemas incompletos falham na validação em vez de serem alterados silenciosamente. Mudanças futuras devem ser novas migrações (`V2`, `V3`...) testadas contra uma cópia do banco.
