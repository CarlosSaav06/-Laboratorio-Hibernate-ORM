-- Ejecutar CONECTADO a la base agenda_orm. Se usa DDL separado del código Java.
CREATE TABLE IF NOT EXISTS public.cuenta_usuario (
    id UUID PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    apellido VARCHAR(80) NOT NULL,
    nombre_usuario VARCHAR(40) NOT NULL UNIQUE,
    correo VARCHAR(120) NOT NULL UNIQUE,
    clave_hash VARCHAR(255) NOT NULL,
    foto_ruta VARCHAR(500),
    fecha_registro TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_usuario_apellido
    ON public.cuenta_usuario (apellido, nombre);

-- Para verificar el CRUD en pgAdmin:
-- SELECT id, nombre, apellido, nombre_usuario, correo, fecha_registro
-- FROM public.cuenta_usuario ORDER BY apellido, nombre;
