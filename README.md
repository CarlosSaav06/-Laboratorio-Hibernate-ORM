# Agenda de Usuarios ORM

**Laboratorio: persistencia de datos con Hibernate ORM**

Aplicación de escritorio de ejemplo académico creada con **Java 21 + JavaFX 21 + Hibernate ORM 6 + PostgreSQL**. El diseño y las clases son originales, escritos a partir de la arquitectura que explica el PDF del profesor. No es una copia del repositorio usado como referencia.

## ¿Qué hace?

- Registro de usuarios con nombre, apellido, usuario, correo, contraseña y fotografía opcional.
- Acceso con usuario y contraseña.
- Tabla que consulta las cuentas almacenadas en PostgreSQL.
- Búsqueda por nombre, apellido, usuario o correo (consulta HQL).
- Edición de datos y actualización opcional de contraseña.
- Eliminación con confirmación (excepto la cuenta que está usando la aplicación).
- Persistencia real: al cerrar y volver a abrir, los registros siguen en PostgreSQL.

Las contraseñas NO se guardan en texto plano: se usa PBKDF2 con una sal aleatoria. La base guarda una ruta local a la fotografía; si se cambia de equipo, la imagen puede dejar de verse hasta escogerla de nuevo.

## Requisitos previos

1. **JDK 21** (en IntelliJ: File > Project Structure > Project SDK).
2. **IntelliJ IDEA** con soporte para proyectos Maven.
3. **PostgreSQL** funcionando, normalmente en el puerto 5432; puedes usar pgAdmin.
4. Internet solo para la **primera descarga de dependencias Maven**.

## Instalación exacta en Windows / IntelliJ

**1. Crear la base de datos.** En pgAdmin, conéctate a PostgreSQL, abre Query Tool sobre la base predeterminada `postgres` y ejecuta `sql/01_crear_base.sql`. Si PostgreSQL informa que la base ya existe, no la crees de nuevo. Otra opción es usar clic derecho en Databases > Create > Database y nombrarla `agenda_orm`.

**2. Crear la tabla.** Selecciona la nueva base **agenda_orm**, abre Query Tool y ejecuta `sql/02_esquema.sql`. Es importante ejecutar el segundo archivo en la nueva base, no en `postgres`.

**3. Configurar contraseña.** En la carpeta raíz de este proyecto (la misma donde está `pom.xml`), copia `db.properties.example` y cambia el nombre de la copia a `db.properties`. Abre ese archivo y sustituye `COLOCA_AQUI_TU_CLAVE` por la contraseña REAL de PostgreSQL. Por ejemplo:

```properties
db.url=jdbc:postgresql://localhost:5432/agenda_orm
db.user=postgres
db.password=TU_CONTRASENA_REAL
```

Si tu usuario, host o puerto son distintos, ajusta los campos. Este archivo está en `.gitignore`, así que **no subas tu contraseña a GitHub**. También se admiten `DB_URL`, `DB_USER`, `DB_PASSWORD` como variables de entorno y las propiedades JVM `-Ddb.url`, `-Ddb.user`, `-Ddb.password`.

**4. Abrir el proyecto.** Descomprime el ZIP. En IntelliJ: **File > Open** > elige la carpeta `AgendaUsuariosORM` (la que contiene `pom.xml`) > **Trust Project**. En el panel Maven, pulsa **Reload All Maven Projects** y espera a que se descarguen las dependencias.

**5. Ejecutar.** En la ventana **Maven** de IntelliJ, expande **Plugins > javafx** y haz doble clic sobre **javafx:run**. También puedes usar el terminal integrado, ubicado en la carpeta del proyecto, con:

```bash
mvn clean javafx:run
```

**6. Primera cuenta.** La base comienza vacía: haz clic en **Crear cuenta**, registra un usuario con contraseña de al menos 8 caracteres y luego ingresa. Esto evita incluir una contraseña administrador débil en el script.

## Prueba que puedes demostrar en clase

1. Crear una cuenta, iniciar sesión y verificar que el registro aparece en la tabla.
2. Crear otro usuario; buscarlo por apellido o correo.
3. Seleccionar la fila y cambiar su correo; comprobar que se actualiza.
4. Eliminar la segunda cuenta; comprobar que desaparece.
5. Cerrar la aplicación, ejecutarla otra vez e iniciar sesión con la primera cuenta: los datos permanecen.
6. En pgAdmin, conectarte a `agenda_orm` y ejecutar:

```sql
SELECT nombre, apellido, nombre_usuario, correo, fecha_registro
FROM public.cuenta_usuario
ORDER BY fecha_registro DESC;
```

## Arquitectura solicitada por el PDF

```text
FXML + CSS / Controller (hilo JavaFX)
           │
           ├── Task<T> → ExecutorService (hilo de trabajo)
           │                      │
           │                 UsuarioService
           │                (Session + Transaction)
           │                      │
           │                UsuarioRepository
           │                  (DAO / HQL)
           │                      │
           │                 Hibernate ORM
           │                      │
           │                HikariCP + pgJDBC
           │                      │
           │                  PostgreSQL
           │
           └── onSucceeded / onFailed ← DTO (hilo JavaFX)
```

- **Entidades y DTO separados:** `Usuario` solo vive en servicio/DAO. `UsuarioDTO` es inmutable y no incluye la contraseña.
- **Transacciones acotadas:** cada caso de uso abre una sesión; `UsuarioService.transaccion` inicia la transacción, ejecuta la consulta o modificación, hace commit o rollback y cierra la sesión.
- **DAO reutiliza la misma sesión:** `UsuarioRepository` recibe `Session` como argumento y no conoce `SessionFactory`.
- **Pool único y seguro para múltiples hilos:** `HibernateProvider` instancia una sola `SessionFactory` y un `HikariDataSource`.
- **JavaFX sin bloqueos:** `EjecutorTareas` ejecuta las consultas en segundo plano y entrega DTO al controlador con `onSucceeded`.
- **DDL separado:** Hibernate usa `validate`, no crea ni modifica el esquema al arrancar. Se mantiene `sql/02_esquema.sql` para la creación inicial.
- **Credenciales externas:** el archivo `db.properties` no se incluye en el repositorio.

## Estructura de carpetas

```text
AgendaUsuariosORM/
├─ pom.xml
├─ db.properties.example
├─ sql/
│  ├─ 01_crear_base.sql
│  └─ 02_esquema.sql
├─ src/main/java/ni/edu/uam/agenda/
│  ├─ Launcher.java / AgendaApplication.java
│  ├─ config/         (pool, SessionFactory, configuración)
│  ├─ model/          (entidad Usuario)
│  ├─ dto/            (DatosUsuario, UsuarioDTO)
│  ├─ repository/     (DAO con HQL)
│  ├─ service/        (transacciones, validación, contraseñas)
│  ├─ controller/     (controladores JavaFX)
│  └─ util/           (ExecutorService y Task)
├─ src/main/resources/
│  ├─ fxml/           (inicio, listado, formulario)
│  └─ css/            (interfaz propia)
└─ src/test/java/     (pruebas unitarias)
```

## Fallos comunes

- **`No se pudo iniciar la base de datos`:** revisar que el servicio PostgreSQL esté activo, el archivo `db.properties` exista y la tabla `cuenta_usuario` esté creada en `agenda_orm`.
- **`password authentication failed`:** la clave de `db.properties` no coincide con PostgreSQL.
- **`database agenda_orm does not exist`:** crear primero la base con el script 01.
- **`Schema-validation: missing table`:** el script 02 se ejecutó en otra base, o faltan permisos.
- **`release version 21 not supported`:** configurar el Project SDK y el Maven Runner JRE a Java 21.
- **`mvn` no se reconoce:** usar el botón **javafx:run** del panel Maven integrado de IntelliJ.

## Alcance y seguridad

Este es un **laboratorio didáctico**, no un sistema de producción: cualquier usuario autenticado puede gestionar el directorio. Para producción se necesitan roles/permisos, migraciones versionadas, manejo centralizado de logs, cifrado/protección de secretos y pruebas de integración. La conexión local sin TLS es suficiente para practicar; en PostgreSQL remoto utiliza `sslmode=verify-full` y un certificado confiable en la URL JDBC.
