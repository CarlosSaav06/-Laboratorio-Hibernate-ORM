package ni.edu.uam.agenda.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ni.edu.uam.agenda.AgendaApplication;
import ni.edu.uam.agenda.dto.DatosUsuario;
import ni.edu.uam.agenda.service.UsuarioService;
import ni.edu.uam.agenda.util.EjecutorTareas;

import java.util.Optional;

/** Pantalla de entrada: la consulta se ejecuta en un Task de fondo. */
public final class InicioController extends ControladorBase {
    @FXML private TextField entradaUsuario;
    @FXML private PasswordField entradaClave;
    @FXML private Button botonIngresar;
    @FXML private Button botonRegistro;
    @FXML private Label aviso;

    private UsuarioService servicio;
    private EjecutorTareas tareas;
    private AgendaApplication aplicacion;

    public void preparar(UsuarioService servicio, EjecutorTareas tareas, AgendaApplication app) {
        this.servicio = servicio;
        this.tareas = tareas;
        this.aplicacion = app;
    }

    @FXML private void ingresar() {
        String login = entradaUsuario.getText().trim();
        String clave = entradaClave.getText();
        if (login.isBlank() || clave.isBlank()) {
            aviso.setText("Completa tu usuario y tu contraseña.");
            return;
        }
        ocupado(true);
        aviso.setText("Comprobando credenciales…");
        tareas.ejecutar(() -> servicio.autenticar(login, clave), resultado -> {
            ocupado(false);
            if (resultado.isPresent()) aplicacion.mostrarUsuarios(resultado.get());
            else {
                entradaClave.clear();
                aviso.setText("Usuario o contraseña incorrectos.");
            }
        }, error -> {
            ocupado(false);
            aviso.setText("No se pudo consultar PostgreSQL.");
            avisarError("Error al ingresar", error);
        });
    }

    @FXML private void registrar() {
        Optional<DatosUsuario> nuevo = DialogoUsuario.mostrar(aplicacion.getVentana(), null);
        if (nuevo.isEmpty()) return;
        ocupado(true);
        aviso.setText("Guardando nueva cuenta…");
        tareas.ejecutar(() -> servicio.crear(nuevo.get()), creado -> {
            ocupado(false);
            entradaUsuario.setText(creado.nombreUsuario());
            entradaClave.clear();
            aviso.setText("Cuenta creada. Ya puedes iniciar sesión.");
            informar("Registro finalizado", "Tu cuenta se guardó en PostgreSQL.");
        }, error -> {
            ocupado(false);
            aviso.setText("No se pudo guardar el usuario.");
            avisarError("Error de registro", error);
        });
    }

    private void ocupado(boolean trabajando) {
        botonIngresar.setDisable(trabajando);
        botonRegistro.setDisable(trabajando);
    }
}
