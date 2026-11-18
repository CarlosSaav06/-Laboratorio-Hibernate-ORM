package ni.edu.uam.agenda.dto;

/** Valores del formulario que viajan desde JavaFX hasta el servicio. */
public record DatosUsuario(String nombre, String apellido, String nombreUsuario,
                           String correo, String nuevaClave, String fotoRuta) { }
