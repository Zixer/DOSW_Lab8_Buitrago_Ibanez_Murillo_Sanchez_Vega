# OficioYa · Laboratorio 8: Seguridad con Spring Security y JWT
 
## Cómo ejecutar
 
Requisitos: JDK 21, Maven y Docker.
 
1. Levantar PostgreSQL:
 
```
docker run --name postgres-oficioya -e POSTGRES_DB=oficioya_security -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=<su_clave> -p 5432:5432 -d postgres:17
```
 
2. Copiar `.env.example` a `.env` y completar los valores (el `.env` no se sube a Git).
 
3. Arrancar con el perfil `dev` (Git Bash, Linux o macOS):
 
```
set -a; source .env; set +a
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```
 
La API queda en `http://localhost:8080/api/v1`. Con el perfil `dev` se crea el usuario `ADMIN` a partir de `ADMIN_CORREO` y `ADMIN_PASSWORD`.
 
## Variables de entorno
 
| Variable | Obligatoria | Descripción |
|---|---|---|
| `DB_PASSWORD` | Sí | Contraseña de PostgreSQL |
| `JWT_SECRET` | Sí | Clave de firma del JWT, mínimo 32 bytes (`openssl rand -base64 48`) |
| `DB_URL` | No | Por defecto `jdbc:postgresql://localhost:5432/oficioya_security` |
| `DB_USER` | No | Por defecto `postgres` |
| `ADMIN_CORREO` | Solo perfil `dev` | Correo del `ADMIN` |
| `ADMIN_PASSWORD` | Solo perfil `dev` | Contraseña del `ADMIN` |
 
## Endpoints
 
Prefijo `/api/v1`.
 
| Método | Endpoint | Acceso | Éxito | Errores |
|---|---|---|---|---|
| POST | `/auth/registro` | Público | 201 | 400, 409 |
| POST | `/auth/login` | Público | 200 con JWT | 400, 401 |
| GET | `/usuarios/me` | Autenticado | 200 | 401 |
| GET | `/trabajadores` | Autenticado | 200 | 401 |
| GET | `/trabajadores/{id}` | Autenticado | 200 | 401, 404 |
| GET | `/admin/usuarios` | `ADMIN` | 200 | 401, 403 |
| POST | `/solicitudes` | `CONTRATANTE` | 201 | 400, 401, 403, 404 |
| GET | `/solicitudes` | `CONTRATANTE`, `TRABAJADOR` | 200 | 401, 403 |
| GET | `/solicitudes/{id}` | Parte de la solicitud | 200 | 401, 403, 404 |
| PATCH | `/solicitudes/{id}/aceptar` | `TRABAJADOR` asignado | 200 | 401, 403, 404, 409 |
| PATCH | `/solicitudes/{id}/rechazar` | `TRABAJADOR` asignado | 200 | 401, 403, 404, 409 |
| PATCH | `/solicitudes/{id}/cancelar` | `CONTRATANTE` autor | 200 | 401, 403, 404, 409 |
 
## Pruebas
 
```
mvn clean verify
```
 
Resultado: `Tests run: 40, Failures: 0, Errors: 0, Skipped: 0` · `BUILD SUCCESS` ([evidencia](docs/evidence/mvn-verify.png)).
 
## Documentación y evidencias
 
- [Matriz OWASP](docs/matriz-owasp.md)
- [Matriz de acceso](docs/matriz-acceso.md)
- [Informe de seguridad](docs/informe-seguridad.md)
- [Ataques manuales](docs/ataques.md)
- Evidencias:
  - [password_hash en PostgreSQL](docs/evidence/password-hash.png)
  - [Payload del JWT](docs/evidence/jwt-payload.png)
  - Respuestas [401](docs/evidence/respuesta-401.png), [403](docs/evidence/respuesta-403.png) y [404](docs/evidence/ataque-4.png)
  - [Ataque 2](docs/evidence/ataque-2.png) y [ataque 4](docs/evidence/ataque-4.png)
  - [mvn clean verify](docs/evidence/mvn-verify.png)
