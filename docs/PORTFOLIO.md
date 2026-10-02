# Planora en el CV y portafolio

Estos textos describen el estado actual. No atribuyen JWT funcional, pasarela de pagos, despliegue productivo ni metas de ahorro implementadas.

## CV

**Planora — Backend de finanzas personales | Proyecto personal en desarrollo**

Desarrollo de una API REST con Java 21 y Spring Boot para gestionar cuentas, categorías, gastos y compras planificadas. Organización por funcionalidades y casos de uso, persistencia con PostgreSQL y jOOQ, migraciones con Flyway y control de concurrencia optimista. Implementación de reglas de negocio para artículos, distribución de pagos y estados de compra, con pruebas de dominio, servicios, controladores y repositorios mediante JUnit, Mockito y MockMvc.

**Tecnologías:** Java 21, Spring Boot, Spring Security, PostgreSQL, jOOQ, Flyway, Gradle, Docker Compose, JUnit y Mockito.

## Portafolio

Planora es un backend de finanzas personales que permite organizar cuentas y categorías, registrar gastos y gestionar compras desde su planificación hasta su finalización o cancelación. Las compras agrupan artículos y permiten distribuir el importe entre varias cuentas, verificando que los pagos coincidan con el total antes de completarlas.

El proyecto está construido como un monolito organizado por funcionalidades, con separación entre API REST, casos de uso, dominio y persistencia. Utiliza Java 21 y Spring Boot, PostgreSQL para almacenamiento y jOOQ para consultas SQL tipadas. El esquema evoluciona mediante Flyway e incorpora restricciones de integridad, borrado lógico en categorías y gastos, y control de concurrencia optimista. La suite incluye pruebas de reglas de dominio, servicios, contratos HTTP y repositorios sobre PostgreSQL.

Actualmente está en desarrollo: los próximos hitos son completar la autenticación por token, reforzar la autorización de referencias entre entidades, implementar movimientos de saldo y metas de ahorro, y automatizar la ejecución y el despliegue.

## Evidencia que conviene añadir al terminar

- Enlace al repositorio, contrato OpenAPI y demo con datos ficticios.
- Un caso explicado de concurrencia y rollback con su prueba automatizada.
- Un ejemplo de aislamiento entre usuarios probado por HTTP.
- Diagrama de arquitectura y decisiones sobre moneda, movimientos y persistencia.
- Resultado real de CI y mediciones de rendimiento reproducibles.

Actualiza la descripción cuando los hitos estén terminados. Añade métricas solo si puedes mostrar cómo se obtuvieron.
