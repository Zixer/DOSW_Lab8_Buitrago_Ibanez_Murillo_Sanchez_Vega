# OficioYa · Laboratorio 8: Seguridad con Spring Security y JWT
 
## Tecnologías
 
- Java 21 · Maven · Spring Boot 4.1
- Spring Web MVC, Spring Data JPA, Bean Validation
- Spring Security con filtro JWT propio (jjwt 0.12.6, HS256)
- PostgreSQL 17 (ejecución) y H2 en memoria (pruebas)
- JUnit 5, MockMvc y @WithMockUser
 
## Cómo ejecutar el proyecto
 
### Requisitos
 
JDK 21, Maven (o el wrapper mvnw), Docker y Git.
 
### 1. Clonar el repositorio
 
```
git clone https://github.com/Zixer/DOSW_Lab8_Buitrago_Ibanez_Murillo_Sanchez_Vega.git
cd DOSW_Lab8_Buitrago_Ibanez_Murillo_Sanchez_Vega
git checkout develop
```
 
### 2. Levantar PostgreSQL
 
```
docker run --name postgres-oficioya -e POSTGRES_DB=oficioya_security \
  -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=<su_clave> -p 5432:5432 -d postgres:17
```
 
### 3. Variables de entorno
 
La aplicación no arranca si faltan las obligatorias. **Los valores no se suben al repositorio**; el archivo .env.example solo lista los nombres. Copie .env.example a .env (ignorado por Git) y complete los valores.
 
| Variable | Obligatoria | Descripción |
|---|---|---|
| DB_PASSWORD | Sí | Contraseña de PostgreSQL (la misma del paso 2) |
| JWT_SECRET | Sí | Clave de firma del JWT, de **al menos 32 bytes** |
| DB_URL | No | URL de la base. Por defecto jdbc:postgresql://localhost:5432/oficioya_security |
| DB_USER | No | Usuario de la base. Por defecto postgres |
| ADMIN_CORREO | Solo perfil dev | Correo del usuario ADMIN que se crea al arrancar |
| ADMIN_PASSWORD | Solo perfil dev | Contraseña del usuario ADMIN |
 
Generar una clave local para JWT_SECRET (no se sube a Git):
 
```
# Linux / macOS / Git Bash
openssl rand -base64 48
 
# PowerShell
$b = New-Object byte[] 48
(New-Object Security.Cryptography.RNGCryptoServiceProvider).GetBytes($b)
[Convert]::ToBase64String($b)
```
 
### 4. Arrancar la aplicación
 
Git Bash, Linux o macOS (carga el .env en la terminal):
 
```
set -a; source .env; set +a
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```
 
PowerShell (las variables valen solo para esa ventana):
 
```
$env:DB_PASSWORD = "<su_clave>"
$env:JWT_SECRET = "<clave_generada>"
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:ADMIN_CORREO = "<correo_admin>"
$env:ADMIN_PASSWORD = "<clave_admin>"
mvn spring-boot:run
```
 
La API queda en `http://localhost:8080/api/v1`.
 
Con el perfil dev, AdminSeeder crea al arrancar el usuario ADMIN con ADMIN_CORREO y ADMIN_PASSWORD (contraseña con BCrypt). Si ya existe no lo duplica, y con cualquier otro perfil no se crea. El ADMIN nunca se obtiene por el registro público.
 
> Si el .env se editó en Windows y la base rechaza la contraseña, quite los finales de línea de Windows con `sed -i 's/\r$//' .env` y vuelva a cargarlo.
 
## Pruebas
 
```
mvn clean verify
```
 
Usan el perfil test (src/test/resources/application-test.properties) con H2 en memoria y una clave de prueba, así que **no requieren PostgreSQL ni variables de entorno**.
 
**Resultado de mvn clean verify (2026-10-29):** `Tests run: 40, Failures: 0, Errors: 0, Skipped: 0` · `BUILD SUCCESS` ([evidencia](docs/evidence/mvn-verify.png)).
 
| Pruebas | Ubicación | Responsable |
|---|---|---|
| PT-01 a PT-06 · registro y login | AuthControllerTest | A |
| PT-07 a PT-09 · token (sin token, alterado, vencido) | SeguridadTokenTest, JwtServiceTest | B |
| PT-10 a PT-12 · perfiles y administración | PerfilesAdminTest | C |
| PT-13 a PT-17 · solicitudes, propiedad y estados | SolicitudesTest | D |
 
## Contrato de la API
 
Prefijo /api/v1. El rol equivocado responde 403; una solicitud ajena o inexistente responde 404; una transición de estado inválida responde 409.
 
| Método | Endpoint | Acceso | Éxito | Errores | Estado |
|---|---|---|---|---|---|
| POST | /auth/registro | Público | 201 | 400, 409 | Implementado |
| POST | /auth/login | Público | 200 con JWT | 400, 401 | Implementado |
| GET | /usuarios/me | Autenticado | 200 con datos propios (incluye contacto) | 401 | Implementado |
| GET | /trabajadores | Autenticado | 200 con perfiles públicos | 401 | Implementado |
| GET | /trabajadores/{id} | Autenticado | 200 con perfil público | 401, 404 | Implementado |
| GET | /admin/usuarios | ADMIN | 200 con datos de contacto | 401, 403 | Implementado |
| POST | /solicitudes | CONTRATANTE | 201 | 400, 401, 403, 404 | Implementado |
| GET | /solicitudes | CONTRATANTE, TRABAJADOR | 200 solo las mías | 401, 403 | Implementado |
| GET | /solicitudes/{id} | Parte de la solicitud | 200 | 401, 403, 404 | Implementado |
| PATCH | /solicitudes/{id}/aceptar | TRABAJADOR asignado | 200 | 401, 403, 404, 409 | Implementado |
| PATCH | /solicitudes/{id}/rechazar | TRABAJADOR asignado | 200 | 401, 403, 404, 409 | Implementado |
| PATCH | /solicitudes/{id}/cancelar | CONTRATANTE autor | 200 | 401, 403, 404, 409 | Implementado |
 
Estados de una solicitud: PENDIENTE → ACEPTADA o RECHAZADA (trabajador asignado); PENDIENTE o ACEPTADA → CANCELADA (contratante autor). Cualquier otro cambio responde 409.
 
### Ejemplos
 
Registro (POST /api/v1/auth/registro). oficioPrincipal y zona son opcionales y solo aplican a trabajadores:
 
```json
{
  "nombre": "Pedro Gómez",
  "correo": "pedro@example.com",
  "telefono": "3001234567",
  "password": "<contraseña_de_al_menos_8_caracteres>",
  "rol": "TRABAJADOR",
  "oficioPrincipal": "Plomero",
  "zona": "Chapinero"
}
```
 
Respuesta 201 (sin secretos): { "id": 3, "nombre": "Pedro Gómez", "correo": "pedro@example.com", "rol": "TRABAJADOR" }
 
Login (POST /api/v1/auth/login): cuerpo { "correo": "pedro@example.com", "password": "<contraseña>" }. Respuesta 200:
 
```json
{ "tipo": "Bearer", "accessToken": "<JWT_FIRMADO>", "expiraEn": 1800 }
```
 
Crear solicitud (POST /api/v1/solicitudes, con Authorization: Bearer <JWT_FIRMADO>). El contratante **no** se envía: sale del token.
 
```json
{ "trabajadorId": 3, "descripcion": "Tengo una gotera en el baño" }
```
 
Las rutas protegidas se llaman con el header Authorization: Bearer <JWT_FIRMADO>. <JWT_FIRMADO> es un marcador: no se suben tokens reales al repositorio.
 
Formato único de error (también para 401 y 403):
 
```json
{
  "timestamp": "2026-10-29T22:20:35.6970036",
  "status": 401,
  "error": "Unauthorized",
  "mensaje": "Token ausente, inválido o vencido",
  "ruta": "/api/v1/solicitudes"
}
```
 
## Seguridad implementada
 
- Contraseñas con **BCrypt** (PasswordEncoder); nunca en texto plano.
- **JWT HS256** con los claims sub (correo), role, iss (oficioya), iat y exp, con vigencia de **30 minutos**. La clave llega por JWT_SECRET (mínimo 32 bytes).
- JwtAuthenticationFilter valida firma, emisor y expiración en cada petición. Sin token, alterado o vencido: 401.
- Sesiones **stateless**; solo /auth/** es público; /admin/** es solo para ADMIN.
- Registro solo con los roles TRABAJADOR y CONTRATANTE (lista de permitidos); ADMIN nunca se crea por registro.
- Login con el mismo 401 y el mismo mensaje si el correo no existe o la contraseña es incorrecta.
- La identidad sale siempre del contexto de seguridad, nunca del cuerpo ni de la URL.
- Perfil público sin correo ni teléfono; solo el ADMIN y el propio usuario en /usuarios/me ven los datos de contacto.
- Solicitudes aisladas por propiedad: la consulta incluye al participante y una solicitud ajena responde 404.
- Sin secretos en el repositorio; los logs no contienen tokens ni contraseñas.
 
## Documentación y evidencias
 
| Documento | Enlace |
|---|---|
| Matriz OWASP | [docs/matriz-owasp.md](docs/matriz-owasp.md) |
| Matriz de acceso del proyecto | [docs/matriz-acceso.md](docs/matriz-acceso.md) |
| Informe de seguridad | [docs/informe-seguridad.md](docs/informe-seguridad.md) |
| Ataques manuales (bloque 8) | [docs/ataques.md](docs/ataques.md) |
| Evidencias | [docs/evidence/](docs/evidence/) |
 
Evidencias en docs/evidence/:
 
- Columna password_hash en PostgreSQL con hashes BCrypt: [password-hash.png](docs/evidence/password-hash.png)
- Payload decodificado de un token en jwt.io: [jwt-payload.png](docs/evidence/jwt-payload.png)
- Respuestas 401, 403 y 404: [respuesta-401.png](docs/evidence/respuesta-401.png), [respuesta-403.png](docs/evidence/respuesta-403.png), [ataque-4.png](docs/evidence/ataque-4.png)
- Ataques 2 y 4 del bloque 8: [ataque-2.png](docs/evidence/ataque-2.png), [ataque-4.png](docs/evidence/ataque-4.png)
- Resultado de mvn clean verify: [mvn-verify.png](docs/evidence/mvn-verify.png)