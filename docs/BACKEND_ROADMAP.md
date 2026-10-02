# Análisis y plan de finalización del backend

Revisión del código y configuración disponibles el 25 de septiembre de 2026, incluidos los cambios locales existentes. Este documento propone trabajo futuro; no implica que las correcciones estén implementadas.

## Evaluación

Planora tiene una base de monolito por funcionalidades adecuada para un portafolio Java: API REST, interfaces de casos de uso y repositorios, reglas de dominio, SQL tipado con jOOQ, migraciones, concurrencia optimista y pruebas por capas. Se encontraron 48 archivos `*Test.java`; ese número no mide cobertura ni garantiza que pasen.

El principal trabajo restante es cerrar comportamientos de negocio y seguridad, y hacer reproducible la ejecución. Añadir tecnologías debe responder a necesidades demostrables y producir evidencia que puedas explicar en una entrevista.

**Verificación realizada:** 199 pruebas en 41 suites, sin fallos, errores ni omisiones, ejecutando `./gradlew :backend:test -x jooqCodegen -x flywayMigrate --tests '*.domain.*' --tests '*.application.*' --tests '*.api.*' --tests '*.common.*'`. Se reutilizaron las clases jOOQ existentes. No se ejecutaron tests de repositorio contra PostgreSQL, migraciones ni una compilación limpia; este resultado no verifica esos caminos.

## P0 — Corregir antes de publicar una demo funcional

### 1. Completar el flujo de autenticación

**Evidencia:** `AuthController.login` autentica credenciales y responde sin cuerpo; `SecurityConfig` usa `STATELESS` y no configura autenticación Bearer. `SecurityCurrentUser` espera un `PlanoraUserPrincipal`.

- Implementar emisión y validación de access tokens, o integrar un proveedor de identidad y eliminar el login local redundante.
- Para JWT, verificar firma, expiración, emisor y audiencia; resolver el UUID del usuario desde un sujeto confiable. Adaptar `CurrentUser` al principal que produzca la autenticación elegida.
- Si se implementan refresh tokens, persistir su hash, rotarlos, detectar reutilización y revocarlos al cerrar sesión o cambiar contraseña.
- Definir respuestas JSON `401` y `403`; probar credenciales incorrectas, token ausente, inválido y expirado.
- Configurar CORS para los clientes previstos y documentar la decisión sobre CSRF según cómo se transporten las credenciales.

**Terminado cuando:** registro → login → creación y consulta de una cuenta funciona por HTTP real; tokens inválidos no acceden al negocio.

Spring Security proporciona [validación JWT como Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html); esa capacidad no implementa por sí sola la emisión de tokens ni el refresh.

### 2. Autorizar también las referencias dentro de compras

**Evidencia:** `AddPurchasePaymentService` comprueba el propietario de la compra, pero acepta `accountId` sin consultar su propietario. `AddPurchaseItemService` hace lo mismo con `categoryId`. Las claves foráneas verifican existencia, no que ambas entidades pertenezcan al mismo usuario. `ExpenseReferenceValidator` ya proporciona un patrón útil para estos controles.

- Validar cuenta y categoría mediante ID + usuario autenticado, y rechazar categorías eliminadas.
- Añadir pruebas con usuarios A/B: A no puede usar la cuenta o categoría de B en su compra.
- Revalidar las referencias que sean relevantes al completar la compra.

**Terminado cuando:** los intentos cruzados fallan sin persistir cambios y sin revelar datos del otro usuario. Corresponde al riesgo de [autorización a nivel de objeto descrito por OWASP](https://api-security.owasp.org/editions/2023/en/0xa1-broken-object-level-authorization/).

### 3. Resolver saldos y moneda

**Evidencia:** las cuentas nacen con saldo cero. `CreateExpenseService` guarda el gasto y `CompletePurchaseService` cambia el estado de la compra; ninguno registra movimientos ni actualiza saldos. La compra no tiene moneda, aunque las cuentas sí.

- Definir cuándo un pago es una asignación prevista y cuándo constituye un movimiento efectivo. Propuesta: afectar el saldo al completar la compra.
- Implementar un historial de movimientos con saldo inicial/ingresos, egresos y ajustes; añadir transferencias si forman parte del alcance elegido.
- Garantizar atomicidad entre movimiento, saldo y operación de negocio mediante una transacción del caso de uso. El repositorio de compras ya tiene transacciones; no es necesario añadir anotaciones indiscriminadamente.
- Decidir cómo revertir un gasto, cambiar su cuenta/importe y corregir compras completadas sin perder historial.
- Evitar descontar dos veces una compra que también se represente en reportes de gastos.
- Para v1, definir moneda de compra y exigir cuentas compatibles. La conversión de divisas necesita un diseño explícito de tasa, fecha y redondeo.
- Restringir el cambio de moneda de cuentas con historial.
- Definir política de saldo insuficiente y comprobar operaciones concurrentes.

**Terminado cuando:** una compra de 100 con pagos de 60 y 40 descuenta exactamente esos importes una sola vez; cualquier fallo revierte toda la operación.

### 4. Completar el contrato de errores y validaciones

**Evidencia:** `Purchase` lanza `IllegalStateException` por transiciones inválidas, pero el advice no la traduce. No hay traducción específica de conflictos de integridad de base de datos. El registro hace comprobaciones previas de duplicados que no resuelven carreras concurrentes.

- Introducir excepciones de negocio para conflictos de estado y mapearlas consistentemente, por ejemplo a `409`; no convertir todos los errores internos en errores del cliente.
- Traducir restricciones únicas y referencias inválidas sin exponer SQL ni detalles internos.
- Validar moneda de registro antes de guardar y cubrir registro concurrente duplicado.
- Alinear límites de texto, precisión y escala con SQL `NUMERIC(19,4)` y con `currency.decimal_places`.
- Definir redondeo explícito para cantidad × precio y totales; probar valores límite y evitar redondeo silencioso solo en persistencia.
- Revisar setters públicos de entidades: actualmente permiten saltarse transiciones y reglas del dominio. Preferir métodos de negocio y restauración controlada.

**Terminado cuando:** errores de negocio previsibles no terminan en `500`, y los importes conservan su significado antes y después de guardarse.

### 5. Preparar el repositorio para publicación

**Evidencia:** existen archivos PEM sin seguimiento bajo `infra/docker/secrets/`; el `.gitignore` del proyecto no cubre esa carpeta ni `.env`. No se inspeccionó el contenido de las claves ni su historial. `LICENSE` está vacío.

- Excluir `.env` y claves privadas; publicar únicamente una plantilla con valores ficticios y las instrucciones de generación local.
- Verificar que no se hayan publicado secretos; rotarlos si existió exposición.
- Completar licencia según la elección del autor y revisar artefactos del IDE antes de publicar.

## P1 — Completar una v1 de portafolio

| Trabajo | Entregable y criterio de aceptación |
| --- | --- |
| Metas de ahorro | La migración V5 ya existe, pero faltan dominio, casos de uso, API y tests. Implementar CRUD, aportes/retiros, progreso y finalización; decidir si los aportes afectan cuentas o solo seguimiento. |
| Catálogos | Endpoints de monedas y tipos de cuenta para que el cliente no dependa de IDs codificados a mano. |
| Filtros y resumen | Gastos por fechas, categoría y cuenta; compras por estado; resumen mensual con reglas que eviten contar dos veces compras y gastos. |
| Idempotencia | Para registrar movimientos/completar compras, clave por usuario y operación con restricción única; reintentos concurrentes no duplican efectos. |
| Integración aislada | PostgreSQL efímero con Testcontainers o servicio dedicado de CI, migraciones desde cero y pruebas HTTP con la seguridad real. |
| Concurrencia | Pruebas de dos transacciones simultáneas sobre la misma cuenta/compra y rollback tras fallo al guardar hijos; complementar las pruebas de versión obsoleta existentes. |
| Build reproducible | Evitar que la lectura obligatoria de `.env` bloquee CI; separar generación jOOQ y migraciones de entornos operativos, y generar desde una base efímera en la compilación limpia. |
| OpenAPI | Esquemas, autenticación, ejemplos, paginación y errores; una colección o archivo HTTP que complete el flujo autenticado. |
| CI | Checkout limpio → JDK → PostgreSQL aislado → migraciones/codegen → tests → artefacto. Añadir informe de cobertura y análisis estático centrados en reglas críticas. |
| Contenedor y despliegue | Dockerfile del backend, Compose completo, configuración por entorno y demo reproducible con datos ficticios. |
| Observabilidad | Health/readiness, logs estructurados con correlación, métricas HTTP/JVM/BD y un panel básico; proteger endpoints de gestión. Actuator ya está declarado. |
| Operación | Procedimiento de backup/restauración, migración de una versión previa y recuperación ante despliegue fallido. |

[Testcontainers ofrece PostgreSQL real y efímero](https://java.testcontainers.org/modules/databases/postgres/), apropiado para validar jOOQ, restricciones y migraciones sin depender de una instalación personal. No sustituye la preparación de jOOQ necesaria antes de compilar.

## P2 — Profundidad técnica y mejoras opcionales

- **Rendimiento SQL:** `JooqPurchaseRepository.findByUserId` ejecuta dos consultas de hijos por compra, además del listado y conteo (`2N + 2`). Recuperar hijos por lotes o usar una proyección adecuada; medir número de consultas y latencia antes/después. Analizar índices compuestos con `EXPLAIN ANALYZE` y datos representativos.
- **Arquitectura verificable:** pruebas de dependencias entre paquetes, dominio sin dependencias de infraestructura y decisiones breves documentadas sobre jOOQ, transacciones y concurrencia.
- **Eventos fiables:** si agregas notificaciones o exportaciones asíncronas, implementar outbox transaccional, consumidor idempotente y reintentos. Un broker se justifica cuando existe ese caso de uso.
- **Caché:** considerar Redis para datos leídos repetidamente o coordinación de límites de peticiones cuando haya una necesidad medida; documentar invalidación y comportamiento ante fallos.
- **Resiliencia:** para servicios externos reales, probar timeout, reintentos acotados y circuit breaker. No añadirlos a llamadas locales a la base de datos por apariencia.
- **Seguridad de cuenta:** recuperación de contraseña, verificación de email y limitación de intentos si la demo permite registro público.
- **Prueba de carga:** publicar escenario, datos, concurrencia, p95 y limitaciones del entorno; no inventar porcentajes de mejora.

No es necesario introducir microservicios, Kubernetes, Kafka, WebFlux ni sustituir jOOQ por JPA para considerar terminado este backend. El monolito actual puede demostrar modelado, SQL, seguridad y operación con suficiente profundidad.

## Orden recomendado

1. Publicación segura del repositorio y autenticación funcional.
2. Autorización de referencias y contrato de errores.
3. Movimientos, moneda, transacciones e idempotencia.
4. Metas, catálogos y consultas de negocio.
5. Pruebas reproducibles, documentación de API y CI.
6. Despliegue, observabilidad y medición de rendimiento.

## Definición de terminado

- Un clon limpio se inicia siguiendo el README sin conocimientos privados del entorno del autor.
- Un cliente puede completar registro, login, cuentas, gastos, compras y metas de ahorro.
- Dos usuarios no acceden ni vinculan recursos ajenos.
- Los saldos y monedas son consistentes bajo reintentos, concurrencia y fallos.
- CI valida migraciones, reglas de negocio, persistencia y seguridad; se conocen sus límites de cobertura.
- Existe una demo o procedimiento reproducible, contrato OpenAPI y evidencia de observabilidad.
- CV y README describen únicamente funcionalidades implementadas y verificaciones realmente realizadas.
