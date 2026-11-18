package ni.edu.uam.agenda.service;

import ni.edu.uam.agenda.dto.DatosUsuario;
import ni.edu.uam.agenda.dto.UsuarioDTO;
import ni.edu.uam.agenda.model.Usuario;
import ni.edu.uam.agenda.repository.UsuarioRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/** Delimita cada transacción y transforma entidades a DTO antes de cerrar la Session. */
public final class UsuarioService {
    private final SessionFactory factory;
    private final UsuarioRepository repositorio;

    public UsuarioService(SessionFactory factory, UsuarioRepository repositorio) {
        this.factory = factory;
        this.repositorio = repositorio;
    }

    public Optional<UsuarioDTO> autenticar(String login, String password) {
        if (login == null || password == null || login.isBlank()) return Optional.empty();
        return transaccion(sesion -> repositorio.buscarPorNombreUsuario(sesion, login.trim())
                .filter(u -> HashClaves.coincide(password, u.getClaveHash()))
                .map(UsuarioDTO::desde));
    }

    public List<UsuarioDTO> listar(String filtro) {
        return transaccion(sesion -> repositorio.buscar(sesion, filtro == null ? "" : filtro.trim())
                .stream().map(UsuarioDTO::desde).toList());
    }

    public UsuarioDTO crear(DatosUsuario entrada) {
        ValidacionUsuario.verificar(entrada, true);
        return transaccion(sesion -> {
            comprobarDuplicados(sesion, entrada, null);
            Usuario nuevo = new Usuario(entrada.nombre().trim(), entrada.apellido().trim(),
                    entrada.nombreUsuario().trim(), entrada.correo().trim().toLowerCase(),
                    HashClaves.crear(entrada.nuevaClave()), normalizarFoto(entrada.fotoRuta()));
            repositorio.crear(sesion, nuevo);
            // Persist + flush: verifica restricciones SQL dentro de la transacción.
            sesion.flush();
            return UsuarioDTO.desde(nuevo);
        });
    }

    public UsuarioDTO actualizar(UUID id, DatosUsuario entrada) {
        if (id == null) throw new IllegalArgumentException("Selecciona un usuario.");
        ValidacionUsuario.verificar(entrada, false);
        return transaccion(sesion -> {
            Usuario guardado = repositorio.buscarPorId(sesion, id);
            if (guardado == null) throw new IllegalArgumentException("El usuario ya no existe.");
            comprobarDuplicados(sesion, entrada, id);
            guardado.actualizar(entrada.nombre().trim(), entrada.apellido().trim(),
                    entrada.nombreUsuario().trim(), entrada.correo().trim().toLowerCase(),
                    normalizarFoto(entrada.fotoRuta()));
            if (entrada.nuevaClave() != null && !entrada.nuevaClave().isBlank()) {
                guardado.cambiarClaveHash(HashClaves.crear(entrada.nuevaClave()));
            }
            // Dirty checking sincroniza los cambios al hacer flush/commit.
            sesion.flush();
            return UsuarioDTO.desde(guardado);
        });
    }

    public void eliminar(UUID id) {
        if (id == null) throw new IllegalArgumentException("Selecciona un usuario.");
        transaccion(sesion -> {
            Usuario guardado = repositorio.buscarPorId(sesion, id);
            if (guardado == null) throw new IllegalArgumentException("El usuario ya no existe.");
            repositorio.borrar(sesion, guardado);
            return null;
        });
    }

    private void comprobarDuplicados(Session sesion, DatosUsuario entrada, UUID excluir) {
        if (repositorio.existeNombreUsuario(sesion, entrada.nombreUsuario().trim(), excluir)) {
            throw new IllegalArgumentException("Ese nombre de usuario ya está registrado.");
        }
        if (repositorio.existeCorreo(sesion, entrada.correo().trim(), excluir)) {
            throw new IllegalArgumentException("Ese correo ya está registrado.");
        }
    }

    private String normalizarFoto(String ruta) {
        return ruta == null || ruta.isBlank() ? null : ruta.trim();
    }

    private <T> T transaccion(Function<Session, T> accion) {
        try (Session sesion = factory.openSession()) {
            Transaction tx = sesion.beginTransaction();
            try {
                T resultado = accion.apply(sesion);
                tx.commit();
                return resultado;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }
}
