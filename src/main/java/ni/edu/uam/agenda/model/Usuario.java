package ni.edu.uam.agenda.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/** Entidad JPA. Nunca se pasa directamente a los controladores JavaFX. */
@Entity
@Table(name = "cuenta_usuario", schema = "public")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 80)
    private String apellido;

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 40)
    private String nombreUsuario;

    @Column(name = "correo", nullable = false, unique = true, length = 120)
    private String correo;

    @Column(name = "clave_hash", nullable = false, length = 255)
    private String claveHash;

    @Column(name = "foto_ruta", length = 500)
    private String fotoRuta;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    protected Usuario() { }

    public Usuario(String nombre, String apellido, String nombreUsuario,
                   String correo, String claveHash, String fotoRuta) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.nombreUsuario = nombreUsuario;
        this.correo = correo;
        this.claveHash = claveHash;
        this.fotoRuta = fotoRuta;
    }

    @PrePersist
    private void completarFecha() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getCorreo() { return correo; }
    public String getClaveHash() { return claveHash; }
    public String getFotoRuta() { return fotoRuta; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }

    public void actualizar(String nombre, String apellido, String nombreUsuario,
                           String correo, String fotoRuta) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.nombreUsuario = nombreUsuario;
        this.correo = correo;
        this.fotoRuta = fotoRuta;
    }

    public void cambiarClaveHash(String claveHash) { this.claveHash = claveHash; }
}
