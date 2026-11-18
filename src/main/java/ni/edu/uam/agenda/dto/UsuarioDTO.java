package ni.edu.uam.agenda.dto;

import ni.edu.uam.agenda.model.Usuario;
import java.time.LocalDateTime;
import java.util.UUID;

/** Datos seguros/desacoplados que se presentan en la interfaz; jamás incluyen la clave. */
public record UsuarioDTO(UUID id, String nombre, String apellido, String nombreUsuario,
                         String correo, String fotoRuta, LocalDateTime fechaRegistro) {
    public static UsuarioDTO desde(Usuario usuario) {
        return new UsuarioDTO(usuario.getId(), usuario.getNombre(), usuario.getApellido(),
                usuario.getNombreUsuario(), usuario.getCorreo(), usuario.getFotoRuta(),
                usuario.getFechaRegistro());
    }
    public String nombreCompleto() { return nombre + " " + apellido; }
}
