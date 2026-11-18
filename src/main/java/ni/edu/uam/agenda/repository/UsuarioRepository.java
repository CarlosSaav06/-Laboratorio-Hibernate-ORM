package ni.edu.uam.agenda.repository;

import ni.edu.uam.agenda.model.Usuario;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** DAO: recibe la Session del servicio; NO abre transacciones ni SessionFactory. */
public final class UsuarioRepository {
    public Optional<Usuario> buscarPorNombreUsuario(Session sesion, String nombreUsuario) {
        return sesion.createQuery("""
                from Usuario u where lower(u.nombreUsuario) = :identificador
                """, Usuario.class)
                .setParameter("identificador", nombreUsuario.toLowerCase())
                .uniqueResultOptional();
    }

    public Usuario buscarPorId(Session sesion, UUID id) {
        return sesion.get(Usuario.class, id);
    }

    public boolean existeNombreUsuario(Session sesion, String login, UUID exceptoId) {
        String hql = exceptoId == null
                ? "select count(u) from Usuario u where lower(u.nombreUsuario) = :valor"
                : "select count(u) from Usuario u where lower(u.nombreUsuario) = :valor and u.id <> :excluir";
        var consulta = sesion.createQuery(hql, Long.class).setParameter("valor", login.toLowerCase());
        if (exceptoId != null) consulta.setParameter("excluir", exceptoId);
        return consulta.getSingleResult() > 0;
    }

    public boolean existeCorreo(Session sesion, String correo, UUID exceptoId) {
        String hql = exceptoId == null
                ? "select count(u) from Usuario u where lower(u.correo) = :valor"
                : "select count(u) from Usuario u where lower(u.correo) = :valor and u.id <> :excluir";
        var consulta = sesion.createQuery(hql, Long.class).setParameter("valor", correo.toLowerCase());
        if (exceptoId != null) consulta.setParameter("excluir", exceptoId);
        return consulta.getSingleResult() > 0;
    }

    public List<Usuario> buscar(Session sesion, String filtro) {
        String expresion = "%" + filtro.toLowerCase() + "%";
        return sesion.createQuery("""
                from Usuario u
                where lower(u.nombre) like :filtro
                   or lower(u.apellido) like :filtro
                   or lower(u.nombreUsuario) like :filtro
                   or lower(u.correo) like :filtro
                order by u.apellido asc, u.nombre asc
                """, Usuario.class)
                .setParameter("filtro", expresion)
                .getResultList();
    }

    public void crear(Session sesion, Usuario usuario) { sesion.persist(usuario); }
    public void borrar(Session sesion, Usuario usuario) { sesion.remove(usuario); }
    // No se llama a 'update' al editar: Hibernate reconoce cambios (dirty checking).
}
