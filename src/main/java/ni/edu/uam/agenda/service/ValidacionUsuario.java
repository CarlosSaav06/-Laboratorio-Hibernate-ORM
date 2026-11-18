package ni.edu.uam.agenda.service;

import ni.edu.uam.agenda.dto.DatosUsuario;
import java.util.regex.Pattern;

/** Validaciones de negocio, independientes de los controles JavaFX. */
public final class ValidacionUsuario {
    private static final Pattern CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern LOGIN = Pattern.compile("^[a-zA-Z0-9._-]{3,40}$");
    private ValidacionUsuario() { }

    public static void verificar(DatosUsuario datos, boolean esNuevo) {
        if (datos == null) throw new IllegalArgumentException("No se recibieron datos.");
        verificarLongitud(datos.nombre(), "nombre", 2, 80);
        verificarLongitud(datos.apellido(), "apellido", 2, 80);
        if (datos.nombreUsuario() == null || !LOGIN.matcher(datos.nombreUsuario().trim()).matches()) {
            throw new IllegalArgumentException("El usuario debe tener 3 a 40 caracteres: letras, números, puntos, guiones o _.");
        }
        if (datos.correo() == null || datos.correo().length() > 120 ||
                !CORREO.matcher(datos.correo().trim()).matches()) {
            throw new IllegalArgumentException("Escribe un correo válido de hasta 120 caracteres.");
        }
        if (esNuevo || (datos.nuevaClave() != null && !datos.nuevaClave().isBlank())) {
            if (datos.nuevaClave() == null || datos.nuevaClave().length() < 8) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
            }
        }
        if (datos.fotoRuta() != null && datos.fotoRuta().length() > 500) {
            throw new IllegalArgumentException("La ruta de fotografía es demasiado larga.");
        }
    }

    private static void verificarLongitud(String valor, String campo, int minimo, int maximo) {
        if (valor == null || valor.trim().length() < minimo || valor.trim().length() > maximo) {
            throw new IllegalArgumentException("El " + campo + " debe tener entre " + minimo + " y " + maximo + " caracteres.");
        }
    }
}
