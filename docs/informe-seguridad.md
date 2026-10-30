# Informe de seguridad · OficioYa
 
## 1. Codificar, cifrar y hashear
- **Codificar:** el JWT usa Base64URL en la cabecera y el payload. Es reversible y no protege nada: cualquiera puede leerlo en jwt.io.
- **Hashear:** las contraseñas se guardan con BCrypt (`PasswordEncoder`); en `password_hash` solo hay valores `$2a$10$...`.
- **Cifrar:** no se usa en las contraseñas. El cifrado es reversible y su clave podría filtrarse. El servidor nunca necesita recuperar la contraseña, solo compararla con `matches`, así que basta un hash de una sola vía.
 
## 2. Payload del token y cambio de rol
El payload solo lleva `sub` (correo), `role`, `iss` (`oficioya`), `iat` y `exp`, sin contraseña ni teléfono. Al cambiar `role` a `ADMIN` y conservar la firma original, la API respondió `401`. `JwtService` recalcula la firma HS256 con `JWT_SECRET` y ya no coincide con el contenido; sin la clave no se puede firmar el payload alterado (ataque 2).
 
## 3. Ejemplos de 401, 403 y 404
- **401:** `GET /solicitudes` sin token, o con un token alterado o vencido.
- **403:** un `CONTRATANTE` en `GET /admin/usuarios`: el servidor sabe quién es, pero su rol no alcanza.
- **404:** Luis pide la solicitud 1, que es de Ana.
 
Una solicitud ajena responde `404` y no `403` porque un `403` confirmaría que existe. La consulta incluye al participante, así que una solicitud ajena y una inexistente reciben la misma respuesta.
 
## 4. ¿Por qué el contratante no envía su id?
`SolicitudRequest` solo tiene `trabajadorId` y `descripcion`. El contratante sale de `authentication.getName()`, es decir, del `sub` de un token con firma verificada. Si el id viniera en el cuerpo, cualquiera podría crear solicitudes a nombre de otro con solo cambiar un número (OWASP A01).
 
## 5. CSRF y cierre de sesión
- **CSRF:** el ataque aprovecha las cookies que el navegador envía solo. Esta API es stateless, no usa cookies y exige el token en `Authorization: Bearer`, que el navegador no adjunta por su cuenta. Si el token pasara a una cookie, habría que reactivar CSRF.
- **Cierre de sesión:** el servidor no guarda sesiones, así que cerrar sesión solo borra el token del cliente. Un token robado sigue sirviendo hasta su `exp` (máximo 30 minutos). Para revocarlo haría falta una lista de tokens revocados o tokens cortos con refresh tokens.
 
## 6. Clave del JWT y producción
- **Si se sube a GitHub:** cualquiera puede firmar tokens con `"role":"ADMIN"`. Habría que rotarla de inmediato, porque borrarla del historial no basta. Por eso llega por `JWT_SECRET` y `.env` está en `.gitignore`.
- **Si es corta:** con un token capturado se puede adivinar por fuerza bruta. `JwtService` no arranca con menos de 32 bytes.
- **Para producción:** clave en un gestor de secretos con rotación; solo HTTPS; tokens cortos con refresh tokens; límite de intentos en el login; mensajes de error genéricos en todos los casos; y considerar RS256, para que los servicios que validan solo tengan la clave pública.