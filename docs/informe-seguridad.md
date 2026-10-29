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

