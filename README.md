# Planora

Backend de finanzas personales para organizar cuentas, categorías, gastos y compras planificadas o realizadas. Construido con Java y Spring Boot, con persistencia SQL mediante jOOQ y PostgreSQL.

**Estado: en desarrollo.** Existen los casos de uso y endpoints principales, pero la autenticación de peticiones posteriores al login, los movimientos de saldo y la funcionalidad de metas de ahorro todavía están pendientes. No es una versión lista para producción.

## Funcionalidades implementadas

- Registro de usuarios con hash de contraseñas y validación de credenciales en el login.
- CRUD de cuentas, categorías, gastos y compras; listados paginados.
- Identificación del usuario mediante `CurrentUser` y consultas por propietario en los casos de uso principales.
- Compras con artículos, pagos distribuidos entre cuentas y estados `PENDING`, `COMPLETED` y `CANCELLED`.
- Validación de que los pagos igualen el total antes de completar una compra.
- Borrado lógico de categorías y gastos.
- Persistencia transaccional del agregado de compra y control de concurrencia optimista mediante `version`.
- Validación de solicitudes y respuestas estructuradas para las excepciones contempladas.
- Migraciones con restricciones, índices y catálogos iniciales de monedas y tipos de cuenta.

Los pagos representan registros internos; no existe integración con una pasarela de pagos. Las cuentas tienen moneda, pero no existe conversión de divisas.

## Tecnologías

- Java 21
- Spring Boot 4.1.0 y Spring Security
- PostgreSQL 17 en Docker Compose
- Flyway y jOOQ, con generación de código desde el esquema
- Bean Validation y Lombok
- Gradle Wrapper y catálogo de versiones
- JUnit Jupiter, Mockito y MockMvc

Las versiones declaradas están en [`gradle/libs.versions.toml`](gradle/libs.versions.toml). JWT y MapStruct aparecen en ese catálogo, pero no están integrados en el backend. Actuator está incluido como dependencia; la observabilidad operativa queda pendiente.

## Estructura

```text
backend/       API REST, casos de uso, dominio, repositorios y pruebas
database/      Migraciones SQL empaquetadas como recurso del backend
infra/docker/  PostgreSQL para desarrollo local
gradle/        Wrapper y catálogo de versiones
docs/          Análisis, pendientes y descripción para CV/portafolio
```

El backend es un monolito organizado por funcionalidad (`account`, `category`, `expense`, `purchase`, `auth`). Dentro de cada funcionalidad se separan API, aplicación, dominio y persistencia. Los servicios implementan interfaces de casos de uso y dependen de interfaces de repositorio; jOOQ implementa el acceso a PostgreSQL.

Los módulos Gradle incluidos actualmente son `backend` y `database`. Los clientes móviles y de escritorio no forman parte de esta implementación.

## Requisitos

- JDK 21 disponible para el toolchain de compilación
- Docker
- Docker Compose
- Acceso a los repositorios de dependencias en la primera compilación

## Levantar el entorno

1. Crea `infra/docker/.env` con valores locales. No publiques ese archivo ni claves privadas. Actualmente el script Gradle exige que el archivo exista, incluso si proporcionas todas las variables mediante el entorno.

   ```dotenv
   POSTGRES_HOST=localhost
   POSTGRES_PORT=5433
   POSTGRES_DB=planora_dev
   POSTGRES_USER=planora_dev
   POSTGRES_PASSWORD=replace_with_a_local_password
   ```

2. Inicia PostgreSQL desde la raíz del repositorio:

   ```bash
   docker compose --env-file infra/docker/.env -f infra/docker/compose.yml up -d
   docker compose --env-file infra/docker/.env -f infra/docker/compose.yml ps
   ```

3. Cuando PostgreSQL esté saludable, inicia el backend:

   ```bash
   ./gradlew :backend:bootRun
   ```

   La compilación ejecuta Flyway y genera las clases jOOQ contra la base configurada. Usa una base local dedicada: este proceso modifica su esquema. El servidor usa el puerto predeterminado `8080`.

Docker Compose inicia únicamente PostgreSQL; aún no existe una imagen Docker del backend. El volumen conserva los datos entre reinicios.

## API disponible

| Ruta | Operaciones |
| --- | --- |
| `/api/auth/register` | `POST`: registro |
| `/api/auth/login` | `POST`: validación de credenciales; aún no devuelve token |
| `/api/accounts` | `GET`, `POST`; `GET`, `PUT`, `DELETE` en `/{accountId}` |
| `/api/categories` | `GET`, `POST`; `GET`, `PUT`, `DELETE` en `/{categoryId}` |
| `/api/expenses` | `GET`, `POST`; `GET`, `PUT`, `DELETE` en `/{expenseId}` |
| `/api/purchases` | `GET`, `POST`; `GET`, `PUT`, `DELETE` en `/{purchaseId}` |
| `/api/purchases/{purchaseId}/items` | `POST`; `DELETE` en `/{itemId}` |
| `/api/purchases/{purchaseId}/payments` | `POST`; `DELETE` en `/{paymentId}` |
| `/api/purchases/{purchaseId}/complete` | `POST` |
| `/api/purchases/{purchaseId}/cancel` | `POST` |

Los listados aceptan `page` y `size`, con valores predeterminados `0` y `10`.

Ejemplo de registro local:

```bash
curl -i http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo","email":"demo@example.com","password":"LocalDemo123!","firstName":"Demo","lastName":"User","primaryCurrencyId":1}'
```

El catálogo inicial contiene PEN (`1`), USD (`2`) y EUR (`3`). Todavía no hay endpoints públicos de consulta de catálogos ni documentación OpenAPI.

La configuración de seguridad es stateless y protege las rutas de negocio. El login no emite token ni conserva sesión, por lo que todavía no es posible completar el flujo autenticado desde un cliente normal.

## Tests

La suite Java está en `backend/src/test/java`: dominio, servicios y validadores
con JUnit/Mockito, controladores con MockMvc y repositorios con PostgreSQL real.
Los tests de repositorio que escriben datos usan transacciones con rollback.

Para ejecutar toda la suite, configura `POSTGRES_HOST`, `POSTGRES_PORT`,
`POSTGRES_DB`, `POSTGRES_USER` y `POSTGRES_PASSWORD` apuntando a una **base
exclusiva de pruebas** y ejecuta:

```bash
./gradlew :backend:test
```

Gradle toma esas variables del entorno o de `infra/docker/.env` (el archivo
debe existir). La compilación genera clases jOOQ y ejecuta Flyway; los tests
de integración también inicializan el esquema mediante Flyway.

Si ya existen las clases generadas de jOOQ y el esquema no ha cambiado:

```bash
./gradlew :backend:test -x jooqCodegen -x flywayMigrate
```

Para ejecutar únicamente tests sin PostgreSQL, reutilizando esas clases:

```bash
./gradlew :backend:test -x jooqCodegen -x flywayMigrate \
  --tests '*.domain.*' --tests '*.application.*' --tests '*.api.*' \
  --tests '*.common.*'
```

El informe HTML queda en `backend/build/reports/tests/test/index.html`.
Los tests MVC comprueban el contrato HTTP con casos de uso simulados;
`SecurityConfigTest` comprueba por separado el rechazo de peticiones anónimas
con la configuración real de seguridad.

El smoke manual está en `backend/src/test/resources/http/planora-smoke.http`.
Ejecuta sus peticiones en orden desde IntelliJ contra una base de pruebas:
crea un usuario único y comprueba registro, duplicados, validación, login y
rechazo de acceso anónimo. Actualmente el login no emite token ni conserva
sesión, por lo que el CRUD autenticado de extremo a extremo queda pendiente
de completar ese mecanismo. Un parámetro `userId` no autentica al cliente.

Los tests de repositorio dependen de un PostgreSQL configurado externamente; Testcontainers aún no está integrado. Tener archivos de prueba no implica que toda la suite esté verificada en cada entorno.

## Próximos pasos

1. Completar autenticación y autorización de las referencias a cuentas y categorías en compras.
2. Definir movimientos financieros, saldos, moneda y límites transaccionales.
3. Implementar metas de ahorro y consultas de catálogos.
4. Consolidar manejo de errores, precisión decimal e idempotencia.
5. Automatizar pruebas aisladas, CI, documentación OpenAPI y despliegue.

El [análisis y backlog priorizado](docs/BACKEND_ROADMAP.md) contiene los hallazgos y criterios de finalización. La [descripción para CV y portafolio](docs/PORTFOLIO.md) refleja las capacidades actuales.

## Licencia

El archivo `LICENSE` está vacío. La licencia de distribución queda pendiente de elección por el autor.
