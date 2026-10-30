# Informe de Seguridad - OficioYa

## 1. ¿Dónde aparecen en su proyecto codificar, cifrar y hashear? ¿Por qué la contraseña se hashea y no se cifra?
- **Hasheo:** Se utiliza mediante el algoritmo BCrypt (PasswordEncoder) en el flujo de registro e inicio de sesión para almacenar de forma segura las contraseñas de los usuarios. Se hashea y no se cifra porque las contraseñas nunca deben ser desencriptadas ni recuperadas en texto plano por el servidor; el proceso es unidireccional y se valida únicamente comparando hashes.
- **Codificación:** Se aplica en la estructura del JWT (usando Base64URL para codificar la cabecera y el payload), permitiendo que cualquier cliente decodifique el formato pero sin alterar su autenticidad.
- **Cifrado:** No se implementa directamente en las contraseñas, ya que el cifrado es bidireccional y requeriría una llave de desencriptación peligrosa si se viera comprometida.

## 2. Decodificaron el payload de su token: ¿qué contiene, qué pasó cuando cambiaron el role a ADMIN y por qué el servidor lo detectó?
- El payload contiene los claims estándar: sub (correo del usuario), role (rol asignado, ej. TRABAJADOR o CONTRATANTE), iss (emisor del token, oficioya), iat (fecha de emisión) y exp (fecha de expiración).
- Al modificar manualmente el campo role a ADMIN (por ejemplo, en jwt.io), la firma criptográfica firmada con HS256 mediante la llave secreta del servidor deja de coincidir con el contenido alterado.
- El servidor lo detecta automáticamente en el JwtAuthenticationFilter al momento de validar la firma con JwtService, rechazando la autenticación e invalidando la petición con un error 401 Unauthorized.

## 3. Den un ejemplo de 401, 403 y 404 de su API. ¿Por qué una solicitud ajena responde 404 y no 403?
- **Ejemplo 401 Unauthorized:** Intentar acceder a un endpoint protegido (como listar solicitudes) sin enviar el encabezado Authorization: Bearer <token> o enviando un token vencido o alterado.
- **Ejemplo 403 Forbidden:** Un usuario con rol CONTRATANTE que intenta acceder a una ruta exclusiva de administración (/api/v1/admin/usuarios).
- **Ejemplo 404 Not Found:** Consultar una solicitud o un recurso con un ID inexistente o que pertenece a otro usuario.
- **¿Por qué responde 404 a una solicitud ajena?:** Para evitar la enumeración de recursos. Si se respondiera 403, se le estaría confirmando al atacante que el recurso sí existe en la base de datos aunque no tenga acceso a él; el 404 oculta por completo su existencia.

## 4. ¿Por qué el contratante no envía su propio id al crear una solicitud?
- `SolicitudRequest` solo tiene `trabajadorId` y `descripcion`. El contratante sale del token: `SolicitudController` pasa `authentication.getName()` (el `sub` del JWT, ya validado por firma) y `SolicitudServiceImpl.crear` busca al usuario por ese correo.
- Si el id viniera en el cuerpo, cualquier contratante podría crear o modificar solicitudes a nombre de otro con solo cambiar un número (control de acceso roto, OWASP A01). El cliente puede alterar el cuerpo, pero no puede alterar un token firmado sin la clave.
 
## 5. ¿Por qué desactivaron CSRF? ¿Qué limitación tiene el cierre de sesión sin revocación de tokens?
- **CSRF:** el ataque funciona porque el navegador adjunta solo las cookies de sesión. Esta API es stateless (`SessionCreationPolicy.STATELESS`), no usa cookies y exige el token en la cabecera `Authorization: Bearer`, que el navegador no envía por su cuenta. Una página maliciosa no puede forjar una petición autenticada. Si el token se moviera a una cookie, habría que volver a activar CSRF.
- **Cierre de sesión:** el servidor no guarda sesiones. "Cerrar sesión" solo significa que el cliente borra el token. Un token robado sigue siendo válido hasta su `exp` (hasta 30 minutos). Para revocarlo haría falta una lista de tokens revocados (por `jti`), una versión de token por usuario o tokens de acceso muy cortos con refresh tokens revocables.
 
## 6. ¿Qué pasaría si la clave del JWT se sube a GitHub, o si es corta? ¿Qué cambiarían para producción?
- **Clave en GitHub:** cualquiera podría firmar tokens válidos con cualquier `sub` y `"role":"ADMIN"`. La autenticación y la autorización dejarían de servir. Habría que rotar la clave de inmediato (lo que invalida todos los tokens); borrarla del historial no basta porque ya pudo copiarse. Por eso llega por `JWT_SECRET` y `.env` está en `.gitignore`.
- **Clave corta:** con un solo token capturado se puede probar claves por fuerza bruta sin conexión hasta encontrar la que reproduce la firma HS256. `JwtService` no arranca si la clave tiene menos de 32 bytes.
- **Para producción:** clave en un gestor de secretos (Vault, AWS Secrets Manager) y rotación periódica; solo HTTPS; tokens de acceso cortos con refresh tokens revocables; límite de intentos en `/auth/login`; `ddl-auto=validate` con migraciones (Flyway); CORS restringido; mensajes de error genéricos en todos los casos; y considerar RS256, para que los servicios que validan solo tengan la clave pública.