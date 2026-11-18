# Explicación breve para la defensa del laboratorio

**Objetivo:** demostrar cómo una aplicación JavaFX de escritorio persiste datos en PostgreSQL mediante Hibernate ORM, sin escribir SQL JDBC manual para cada operación.

**¿Qué es Hibernate?** Es la implementación ORM que mapea la clase `Usuario` con la tabla `cuenta_usuario` usando `@Entity`, `@Table`, `@Id` y `@Column`.

**¿Qué hace el DAO?** `UsuarioRepository` contiene las consultas HQL (por ejemplo, buscar por nombre, usuario y correo) y usa la sesión que recibe del servicio.

**¿Qué hace el servicio?** `UsuarioService` comprueba los datos, inicia cada transacción, hace `commit` si todo sale bien, `rollback` si falla y convierte entidades a DTO antes del cierre.

**¿Por qué Task y ExecutorService?** Una consulta podría tardar. Ejecutarla directamente en el hilo de JavaFX congelaría botones y ventanas. Los resultados vuelven con `onSucceeded` o `onFailed`.

**¿Qué hace HikariCP?** Reutiliza conexiones físicas a PostgreSQL y evita abrir una conexión nueva para cada consulta. `SessionFactory` se construye solo una vez.

**¿Cómo pruebas la persistencia?** Creas y editas usuarios, cierras la aplicación y la abres otra vez: los datos permanecen porque están en PostgreSQL, no solo en memoria.

**¿En qué se diferencia del ejemplo?** Este proyecto utiliza un diseño visual propio, paquetes separados, un DAO reutilizable, validaciones de negocio, contraseñas con hash, DTOs sin campos sensibles, operaciones asíncronas, archivo externo de credenciales y scripts DDL independientes.

## Evidencias recomendadas (capturas propias)

1. Ventana de inicio de sesión y registro.
2. Tabla de usuarios después de crear dos cuentas.
3. Antes y después de editar el correo de un registro.
4. Consulta SQL en pgAdmin mostrando las filas.
5. Estructura Maven en IntelliJ (paquetes `config`, `repository`, `service`, `controller`).

**Nota:** el PDF suministrado describe una arquitectura de referencia, no un listado de casos de uso obligatorios. Los casos de uso aquí definidos se inspiran en el dominio de usuarios del repositorio enlazado, sin reutilizar su código.
