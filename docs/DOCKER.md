# Docker — Planora

## 1. Objetivo

Esta documentación describe cómo ejecutar y administrar el entorno de desarrollo de Planora mediante Docker Compose.

El entorno está compuesto por:

- **Backend:** Java 21 y Spring Boot.
- **Base de datos:** PostgreSQL 17.
- **Migraciones:** Flyway.
- **Generación de código:** jOOQ.
- **Contenedores:** Docker Compose.

La configuración de desarrollo se encuentra en `infra/docker/`.

## 2. Requisitos previos

Instalar y verificar:

- Docker Desktop con Docker Compose.
- JDK 21.
- Git.
- Acceso al repositorio de Planora.

Desde la raíz del proyecto, verificar Docker:

```powershell
docker --version
docker compose version
docker info
```

## 3. Configuración del entorno

El archivo `infra/docker/.env` contiene las variables utilizadas para configurar PostgreSQL y los procesos de compilación.

Variables principales:

```dotenv
POSTGRES_DB=planora
POSTGRES_USER=planora
POSTGRES_PASSWORD=<contraseña-local>
POSTGRES_HOST=localhost
POSTGRES_PORT=5433
```

Utilizar una contraseña local apropiada y mantener el archivo `.env` fuera del control de versiones.

No utilizar las credenciales de desarrollo en producción. Los secretos de producción deben proporcionarse mediante un mecanismo seguro de configuración.

La variable `POSTGRES_HOST` y el puerto `POSTGRES_PORT` tienen valores distintos según el contexto:

- Gradle ejecutado en el equipo: `localhost:5433`.
- Backend ejecutado dentro de Docker: `postgresql:5432`.

El nombre `postgresql` corresponde al servicio de PostgreSQL declarado en Docker Compose.

## 4. Iniciar el entorno

Ejecutar desde la raíz del repositorio, en PowerShell:

```powershell
$Compose = "infra/docker/compose.yaml"
$EnvFile = "infra/docker/.env"

docker compose --env-file $EnvFile -f $Compose up -d --wait postgresql
.\gradlew.bat :backend:bootJar
docker compose --env-file $EnvFile -f $Compose up -d --build
```

La primera instrucción inicia PostgreSQL y espera a que esté saludable. La segunda genera el JAR ejecutable del backend. La tercera construye la imagen del backend y levanta los servicios.

La compilación puede ejecutar tareas de Flyway y jOOQ que necesitan una base de datos accesible. Por eso PostgreSQL debe estar disponible antes de compilar.

Si la versión instalada de Docker Compose no admite `--wait`, comprobar el estado del servicio con `docker compose ps` y continuar cuando PostgreSQL esté saludable.

## 5. Comandos de uso frecuente

### Consultar el estado

```powershell
docker compose --env-file $EnvFile -f $Compose ps
```

### Consultar los logs del backend

```powershell
docker compose --env-file $EnvFile -f $Compose logs -f backend
```

### Consultar los logs de PostgreSQL

```powershell
docker compose --env-file $EnvFile -f $Compose logs -f postgresql
```

Para dejar de seguir los logs, pulsar `Ctrl+C`. Esto no detiene los contenedores.

### Reiniciar el backend

```powershell
docker compose --env-file $EnvFile -f $Compose restart backend
```

Este comando reinicia el contenedor existente. No incorpora cambios del código fuente ni actualiza automáticamente la imagen.

### Detener los servicios

```powershell
docker compose --env-file $EnvFile -f $Compose down
```

Elimina los contenedores y la red del proyecto, pero conserva el volumen de PostgreSQL.

### Iniciar los servicios existentes

```powershell
docker compose --env-file $EnvFile -f $Compose up -d
```

## 6. Actualizar el backend después de modificar código

La imagen actual contiene una copia del JAR generado durante su construcción. Por tanto, modificar código Java no actualiza automáticamente la imagen.

Después de modificar el backend, ejecutar:

```powershell
.\gradlew.bat :backend:bootJar
docker compose --env-file $EnvFile -f $Compose up -d --build backend
```

La primera instrucción compila el backend y genera el JAR. La segunda construye la imagen actualizada y aplica el cambio al contenedor.

No es necesario eliminar el volumen ni recrear la base de datos para actualizar código Java.

### Cambios en migraciones SQL

Cuando se agrega una migración nueva, comprobar que PostgreSQL esté disponible y generar el JAR para ejecutar las tareas configuradas de Flyway y jOOQ.

Después, construir y desplegar la imagen actualizada:

```powershell
.\gradlew.bat :backend:bootJar
docker compose --env-file $EnvFile -f $Compose up -d --build backend
```

Las migraciones ya aplicadas deben conservarse sin modificaciones retroactivas en entornos con datos que deban preservarse. Los cambios de esquema deben introducirse mediante nuevas migraciones.

## 7. Conexión a PostgreSQL

Para conectarse desde IntelliJ IDEA, DBeaver o pgAdmin:

| Parámetro | Valor |
|---|---|
| Host | `localhost` |
| Puerto | `5433` |
| Base de datos | `planora` |
| Usuario | `planora` |
| Contraseña | Valor de `POSTGRES_PASSWORD` |
| Esquema de la aplicación | `planora` |

Dentro de la red de Docker, el backend utiliza `postgresql:5432`.

### Consultar las tablas

```sql
SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_schema = 'planora'
ORDER BY table_name;
```

### Consultar el historial de Flyway

```sql
SELECT installed_rank, version, description, success
FROM planora.flyway_schema_history
ORDER BY installed_rank;
```

## 8. Reiniciar la base de datos desde cero

**Advertencia: este procedimiento elimina los datos persistidos en los volúmenes declarados en el archivo Compose. No ejecutarlo si se necesita conservar la información.**

Primero, detener y eliminar los contenedores y volúmenes:

```powershell
docker compose --env-file $EnvFile -f $Compose down -v
```

Después, iniciar PostgreSQL y esperar a que esté saludable:

```powershell
docker compose --env-file $EnvFile -f $Compose up -d --wait postgresql
```

Generar el JAR para ejecutar las migraciones y la generación de código configuradas:

```powershell
.\gradlew.bat :backend:bootJar
```

Finalmente, construir la imagen y levantar el entorno:

```powershell
docker compose --env-file $EnvFile -f $Compose up -d --build
```

Este procedimiento crea un volumen nuevo y una base vacía, y reconstruye el esquema mediante las migraciones disponibles. No recupera los datos anteriores.

## 9. Seguridad y mantenimiento

- No versionar `infra/docker/.env`.
- No incluir contraseñas ni otros secretos en imágenes o en el repositorio.
- No eliminar volúmenes como parte del reinicio habitual.
- Mantener las migraciones de Flyway versionadas en Git.
- Verificar el estado y los logs si un servicio no arranca.
- No exponer el puerto de PostgreSQL a redes innecesarias en producción.
- Revisar la configuración de secretos, accesos y persistencia antes de desplegar en producción.

## 10. Referencia rápida

| Necesidad | Acción |
|---|---|
| Iniciar servicios | `docker compose ... up -d` |
| Consultar estado | `docker compose ... ps` |
| Ver logs | `docker compose ... logs -f backend` |
| Actualizar código | `bootJar` y `up -d --build backend` |
| Detener sin perder datos | `docker compose ... down` |
| Eliminar datos persistidos | `docker compose ... down -v` |

Los comandos abreviados de la tabla deben ejecutarse con las opciones `--env-file infra/docker/.env -f infra/docker/compose.yaml` mostradas en los ejemplos anteriores.
