# Vehicle Catalog API

API principal de cadastro de veículos e fonte de verdade dos dados de catálogo para a plataforma de vendas.

## 🛠️ Tecnologias
- **Linguagem:** Kotlin (Java 21)
- **Framework:** Spring Boot 4
- **Banco de Dados:** H2 (Dev) / PostgreSQL (Prod)
- **Containers:** Docker / Docker Compose

---

## 🚀 Como Executar

O projeto está configurado com perfis automáticos para facilitar a sua rotina:

### Opção 1: Desenvolvimento Rápido (Via IntelliJ / H2)
*Ideal para codificar e testar rápido. Usa banco de dados H2 em memória (zera ao reiniciar).*

*   **Pela IDE:** Abra o projeto no IntelliJ e clique no botão **Run** na classe `MechShopApplication.kt`.
*   **Pelo Terminal:**
    ```bash
    ./gradlew bootRun

### Opção 2: Ambiente Completo (Via Docker / Postgres)
*Ideal para testar a versão final isolada. Persiste os dados na sua máquina física.*

- Subir tudo (API + Banco): `docker compose up -d`
- Acompanhar logs: `docker compose logs -f`
- Parar o ambiente (Mantendo os dados): `docker compose down`
- Parar e APAGAR o banco (Reset total): `docker compose down -v`

## 📖 Documentação da API (Swagger)

Com a aplicação rodando (seja por IntelliJ ou Docker), acesse o link abaixo para testar os endpoints:

🔗 http://localhost:8080/swagger-ui/index.html

> [!NOTE]
> **Autenticação:** Para acessar as rotas de administrador (`/api/admin/`), gere um token no endpoint `/api/auth/login`, clique no botão **Authorize** (ícone de cadeado no topo da página) e cole o token lá.

## Sincronização do catálogo de vendas

O cadastro permanece salvo neste serviço mesmo quando `vehicle-sales-api` está indisponível. As respostas de criação/edição incluem `syncStatus` (`PENDING`, `SYNCED` ou `FAILED`) e `syncError` sanitizado; `201`/`200` confirma somente a gravação no catálogo principal, não a publicação remota. Veículos não sincronizados podem ser reenviados pela rota administrativa `POST /api/admin/vehicles/{id}/sync`, que publica a versão cadastral mais recente sem incrementá-la.

Configure a URL do serviço e o token interno fora do Git:

| Variável | Descrição | Padrão |
|---|---|---|
| `VEHICLE_SALES_BASE_URL` | URL base do `vehicle-sales-api`, sem path final obrigatório | vazio (sync falha explicitamente até configurar) |
| `MECH_SHOP_INTERNAL_TOKEN` | Token usado no header `Authorization: Bearer` | vazio |
| `VEHICLE_SALES_CONNECT_TIMEOUT` | Timeout de conexão | `3s` |
| `VEHICLE_SALES_READ_TIMEOUT` | Timeout de leitura | `5s` |

O cliente envia `PUT /internal/vehicles/{vehicleId}` com marca, modelo, ano, cor, preço decimal, versão e autenticação Bearer. Falhas não revertem o cadastro; a mesma versão pode ser reenviada com segurança.
