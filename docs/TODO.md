agregar clock o mejor formato de fecha para las Instant.now()

Utilizar Excepciones personalizadas

Utilizar ENUM para proveedores, ejemplo google

Implementar refresh tokens


Docker deberia ejecutarse solo la base de datos
con un solo comando compilar el jar, ejecutar la aplicacion y bd

Arquitectura para refresh token
Login local o Google
|
v
Access token JWT (15 minutos)
+
Refresh token aleatorio
|
v
Hash del refresh token en PostgreSQL
|
v
POST /api/auth/refresh
|
v
Invalidar token anterior
Emitir nuevos tokens

El refresh token debería transmitirse mediante cookie HttpOnly, Secure y una configuración SameSite
adecuada; nunca almacenaría su valor en texto plano en PostgreSQL. El endpoint de renovación también
necesitará protección CSRF y detección de reutilización.
Añade además cierre de sesión que revoque los refresh tokens, limitación de intentos de login,
recuperación de contraseñas y verificación de autorizaciones sobre recursos de cada usuario.




Test:

Prueba	Resultado esperado
/api/auth/google/session sin sesión	401
/api/auth/google/session desde otro Origin	403
Google con identidad ya vinculada	200 + JWT
Identidad nueva	REGISTRATION_REQUIRED
Correo local existente	LINK_REQUIRED
Completar registro nuevo	Usuario e identidad registrados
Reutilizar sesión consumida	401
Completar con sesión expirada	401
Vincular Google sin JWT local	401
Vincular identidad con otro correo	409
Vincular Google a usuario correcto	200 + JWT
Registrar username duplicado	409
Error en segunda inserción	Rollback de ambas escrituras

Para las pruebas HTTP, utiliza MockMvc con una sesión simulada y encabezado Origin: http://localhost:4200.
En las pruebas de vinculación, envía además un JWT firmado con las claves de prueba que ya configuraste.

Importante: al modificar SecurityConfig para incluir OAuth2 Client, tus tests @WebMvcTest existentes podrían requerir
configuración adicional. Si importan toda la clase SecurityConfig, tendrán que proporcionar también los beans necesarios
para GoogleLoginSuccessHandler y el registro del cliente OAuth2. Una alternativa más mantenible es separar las cadenas de
seguridad en clases ApiSecurityConfig y OAuth2SecurityConfig, conservando exactamente las mismas reglas y beans, para que
cada test web pueda importar solo la configuración que necesita.

Para Planora, la siguiente mejora es incorporar Testcontainers con PostgreSQL 17 y ejecutar las migraciones Flyway en el
contenedor de prueba.


Pruebas necesarias para cerrar este componente
   Antes de pasar a JooqEmailVerificationChallengeRepository, verifica estos casos.
   Pruebas de la entidad User
1. verifyEmail() establece emailVerifiedAt y actualiza updatedAt.
2. Intentar verificar un usuario ya verificado lanza IllegalStateException.
3. markVersionPersisted() incrementa la versión exactamente en uno.
4. markVersionPersisted() rechaza una versión inesperada.
5. 
Pruebas de integración de JooqUserRepository
   Usando PostgreSQL de pruebas —no una base en memoria que pueda diferir en el comportamiento de CITEXT, restricciones o tipos—, comprueba:
1. Actualizar un usuario con la versión actual funciona y persiste sus cambios.
2. La versión aumenta exactamente en uno en PostgreSQL y en el objeto.
3. Actualizar usando una versión obsoleta lanza OptimisticLockingFailureException.
4. Si la actualización falla, la versión del objeto en memoria no cambia.
5. Los valores nullable, como emailVerifiedAt, se guardan y recuperan correctamente.
   Puedes seguir el patrón de pruebas de persistencia que ya utilizas para JooqUserRepository. No necesitamos crear una nueva infraestructura de testing si ya tienes una configurada.
