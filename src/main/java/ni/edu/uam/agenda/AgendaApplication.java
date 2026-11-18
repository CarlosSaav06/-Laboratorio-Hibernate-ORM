package ni.edu.uam.agenda;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ni.edu.uam.agenda.config.HibernateProvider;
import ni.edu.uam.agenda.controller.InicioController;
import ni.edu.uam.agenda.controller.UsuariosController;
import ni.edu.uam.agenda.dto.UsuarioDTO;
import ni.edu.uam.agenda.repository.UsuarioRepository;
import ni.edu.uam.agenda.service.UsuarioService;
import ni.edu.uam.agenda.util.EjecutorTareas;

import java.io.IOException;
import java.net.URL;

/** Navegación JavaFX. Aquí no se abre ni se guarda ninguna Session de Hibernate. */
public final class AgendaApplication extends Application {
    private final EjecutorTareas ejecutor = new EjecutorTareas();
    private Stage ventana;
    private HibernateProvider hibernate;
    private UsuarioService servicio;

    @Override
    public void start(Stage primaryStage) {
        ventana = primaryStage;
        ventana.setTitle("Agenda de Usuarios | Laboratorio Hibernate ORM");
        ventana.setMinWidth(840);
        ventana.setMinHeight(570);
        pantallaConexion();
        iniciarPersistencia();
        ventana.show();
    }

    private void pantallaConexion() {
        ProgressIndicator progreso = new ProgressIndicator();
        Label mensaje = new Label("Conectando con PostgreSQL…");
        mensaje.getStyleClass().add("splash-title");
        VBox layout = new VBox(20, progreso, mensaje);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        colocarEscena(layout, 900, 590);
    }

    private void iniciarPersistencia() {
        Task<HibernateProvider> inicio = new Task<>() {
            @Override
            protected HibernateProvider call() throws Exception {
                return HibernateProvider.iniciar();
            }
        };
        inicio.setOnSucceeded(evento -> {
            hibernate = inicio.getValue();
            servicio = new UsuarioService(hibernate.getSessionFactory(), new UsuarioRepository());
            mostrarInicio();
        });
        inicio.setOnFailed(evento -> pantallaErrorInicio(inicio.getException()));
        ejecutor.enviar(inicio);
    }

    private void pantallaErrorInicio(Throwable error) {
        Label titulo = new Label("No se pudo iniciar la base de datos");
        titulo.getStyleClass().add("splash-title");
        Label detalle = new Label("Revisa PostgreSQL, el script 02_esquema.sql y el archivo db.properties.\n\n"
                + limpiarError(error));
        detalle.setWrapText(true);
        Button salir = new Button("Cerrar");
        salir.setOnAction(evento -> ventana.close());
        VBox layout = new VBox(16, titulo, detalle, salir);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(55));
        colocarEscena(layout, 900, 590);
    }

    public void mostrarInicio() {
        try {
            FXMLLoader loader = cargar("inicio.fxml");
            Parent vista = loader.load();
            InicioController controlador = loader.getController();
            controlador.preparar(servicio, ejecutor, this);
            colocarEscena(vista, 900, 590);
        } catch (IOException ex) {
            pantallaErrorInicio(ex);
        }
    }

    public void mostrarUsuarios(UsuarioDTO usuarioActual) {
        try {
            FXMLLoader loader = cargar("usuarios.fxml");
            Parent vista = loader.load();
            UsuariosController controlador = loader.getController();
            controlador.preparar(servicio, ejecutor, this, usuarioActual);
            colocarEscena(vista, 1110, 730);
        } catch (IOException ex) {
            pantallaErrorInicio(ex);
        }
    }

    private FXMLLoader cargar(String nombre) {
        URL recurso = getClass().getResource("/fxml/" + nombre);
        if (recurso == null) throw new IllegalStateException("No existe fxml/" + nombre);
        return new FXMLLoader(recurso);
    }

    private void colocarEscena(Parent raiz, int ancho, int alto) {
        Scene escena = new Scene(raiz, ancho, alto);
        escena.getStylesheets().add(getClass().getResource("/css/estilos.css").toExternalForm());
        ventana.setScene(escena);
        ventana.sizeToScene();
        ventana.centerOnScreen();
    }

    private String limpiarError(Throwable causa) {
        Throwable actual = causa;
        while (actual != null && actual.getCause() != null) actual = actual.getCause();
        return actual == null ? "Error desconocido" : String.valueOf(actual.getMessage());
    }

    public Stage getVentana() { return ventana; }

    @Override
    public void stop() {
        ejecutor.cerrar();
        if (hibernate != null) hibernate.close();
    }
}
