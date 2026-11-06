# Planora

Backend de finanzas personales para organizar cuentas, categorías, gastos y compras planificadas o realizadas. Construido con Java y Spring Boot, con persistencia SQL mediante jOOQ y PostgreSQL.

** 🚀 Estado: en desarrollo.** Existen los casos de uso y endpoints principales, autenticación de peticiones posteriores al login, pero los movimientos de saldo y la funcionalidad de metas de ahorro todavía están pendientes. No es una versión lista para producción.

## Funcionalidades implementadas

- Registro de usuarios con hash de contraseñas y validación de credenciales en el login.
- CRUD de cuentas, categorías, gastos y compras; listados paginados.
- Identificación del usuario mediante `CurrentUser` y consultas por propietario en los casos de uso principales.
- Compras con artículos, pagos distribuidos entre cuentas y estados `PENDING`, `COMPLETED` y `CANCELLED`.
- Borrado lógico de categorías y gastos.
- Persistencia transaccional del agregado de compra y control de concurrencia optimista mediante `version`.
- Validación de solicitudes y respuestas estructuradas para las excepciones contempladas.
- Migraciones con restricciones, índices y catálogos iniciales de monedas y tipos de cuenta.

## Tecnologías

- Java 21
- Spring Boot 4.1.0 y Spring Security
- PostgreSQL 17 en Docker Compose
- Flyway y jOOQ, con generación de código desde el esquema
- Bean Validation y Lombok
- Gradle Wrapper y catálogo de versiones
- JUnit Jupiter, Mockito y MockMvc


## Estructura

```text
backend/       API REST, casos de uso, dominio, repositorios y pruebas
database/      Migraciones SQL empaquetadas como recurso del backend
infra/docker/  PostgreSQL para desarrollo local
gradle/        Wrapper y catálogo de versiones
docs/          Análisis y pendientes
```

El backend es un monolito organizado por funcionalidad (`account`, `category`, `expense`, `purchase`, `auth`). Dentro de cada funcionalidad se separan API, aplicación, dominio y persistencia. Los servicios implementan interfaces de casos de uso y dependen de interfaces de repositorio; jOOQ implementa el acceso a PostgreSQL.

Los módulos Gradle incluidos actualmente son `backend` y `database`.

## Requisitos

- JDK 21 disponible para el toolchain de compilación
- Docker
- Docker Compose
- Acceso a los repositorios de dependencias en la primera compilación

## Levantar el entorno

1. Crea `infra/docker/.env` con valores locales. 

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

3. Cuando PostgreSQL esté en estado healthy, inicia el backend:

   ```bash
   ./gradlew :backend:bootRun
   ```

   La compilación ejecuta Flyway y genera las clases jOOQ contra la base configurada. El servidor usa el puerto predeterminado `8080`.

## Próximos pasos

1. Completar autenticación y autorización de las referencias a cuentas y categorías en compras.
2. Definir movimientos financieros, saldos, moneda y límites transaccionales.
3. Implementar metas de ahorro y consultas de catálogos.
4. Consolidar manejo de errores, precisión decimal e idempotencia.
5. Automatizar pruebas aisladas, CI, documentación OpenAPI y despliegue.
