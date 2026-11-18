package ni.edu.uam.agenda.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ni.edu.uam.agenda.AgendaApplication;
import ni.edu.uam.agenda.dto.DatosUsuario;
import ni.edu.uam.agenda.dto.UsuarioDTO;
import ni.edu.uam.agenda.service.UsuarioService;
import ni.edu.uam.agenda.util.EjecutorTareas;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** TableView solo consume DTOs. Todas las consultas/ediciones son asíncronas. */
public final class UsuariosController extends ControladorBase {
    @FXML private Label sesionActual;
    @FXML private Label estado;
    @FXML private Label totalUsuarios;
    @FXML private Label detalleNombre;
    @FXML private Label detalleUsuario;
    @FXML private Label detalleCorreo;
    @FXML private Label detalleFecha;
    @FXML private TextField campoBusqueda;
    @FXML private TableView<UsuarioDTO> tabla;
    @FXML private TableColumn<UsuarioDTO, String> columnaNombre;
    @FXML private TableColumn<UsuarioDTO, String> columnaLogin;
    @FXML private TableColumn<UsuarioDTO, String> columnaCorreo;
    @FXML private TableColumn<UsuarioDTO, String> columnaFecha;
    @FXML private ImageView detalleFoto;
    @FXML private Button botonBuscar;
    @FXML private Button botonAgregar;
    @FXML private Button botonEditar;
    @FXML private Button botonEliminar;

    private final ObservableList<UsuarioDTO> registros = FXCollections.observableArrayList();
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private UsuarioService servicio;
    private EjecutorTareas tareas;
    private AgendaApplication aplicacion;
    private UsuarioDTO conectado;
    private boolean ocupado;

    @FXML private void initialize() {
        columnaNombre.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().nombreCompleto()));
        columnaLogin.setCellValueFactory(v -> new ReadOnlyStringWrapper("@" + v.getValue().nombreUsuario()));
        columnaCorreo.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().correo()));
        columnaFecha.setCellValueFactory(v -> new ReadOnlyStringWrapper(
                v.getValue().fechaRegistro() == null ? "—" : v.getValue().fechaRegistro().format(formatoFecha)));
        tabla.setItems(registros);
        tabla.setPlaceholder(new Label("No hay usuarios para mostrar"));
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, antes, ahora) -> mostrarDetalle(ahora));
        campoBusqueda.setOnAction(evento -> buscar());
        mostrarDetalle(null);
    }

    public void preparar(UsuarioService servicio, EjecutorTareas tareas, AgendaApplication app, UsuarioDTO actual) {
        this.servicio = servicio;
        this.tareas = tareas;
        this.aplicacion = app;
        conectado = actual;
        sesionActual.setText("Conectado: @" + actual.nombreUsuario());
        buscar();
    }

    @FXML private void buscar() {
        if (ocupado) return;
        establecerOcupado(true);
        estado.setText("Consultando datos…");
        String termino = campoBusqueda.getText();
        tareas.ejecutar(() -> servicio.listar(termino), resultado -> {
            establecerOcupado(false);
            registros.setAll(resultado);
            totalUsuarios.setText(resultado.size() + (resultado.size() == 1 ? " registro" : " registros"));
            estado.setText("Lista actualizada desde PostgreSQL.");
        }, error -> {
            establecerOcupado(false);
            estado.setText("No se pudo cargar la lista.");
            avisarError("Error al consultar", error);
        });
    }

    @FXML private void agregar() {
        Optional<DatosUsuario> datos = DialogoUsuario.mostrar(aplicacion.getVentana(), null);
        if (datos.isEmpty()) return;
        establecerOcupado(true);
        estado.setText("Creando usuario…");
        tareas.ejecutar(() -> servicio.crear(datos.get()), creado -> {
            establecerOcupado(false);
            estado.setText("Usuario creado: @" + creado.nombreUsuario());
            buscar();
        }, error -> {
            establecerOcupado(false);
            avisarError("No se pudo guardar", error);
        });
    }

    @FXML private void editar() {
        UsuarioDTO seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;
        Optional<DatosUsuario> datos = DialogoUsuario.mostrar(aplicacion.getVentana(), seleccionado);
        if (datos.isEmpty()) return;
        establecerOcupado(true);
        estado.setText("Actualizando usuario…");
        tareas.ejecutar(() -> servicio.actualizar(seleccionado.id(), datos.get()), editado -> {
            establecerOcupado(false);
            estado.setText("Registro actualizado correctamente.");
            if (editado.id().equals(conectado.id())) {
                conectado = editado;
                sesionActual.setText("Conectado: @" + editado.nombreUsuario());
            }
            buscar();
        }, error -> {
            establecerOcupado(false);
            avisarError("No se pudo actualizar", error);
        });
    }

    @FXML private void eliminar() {
        UsuarioDTO seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;
        if (seleccionado.id().equals(conectado.id())) {
            informar("Operación no permitida", "No puedes eliminar la cuenta con la sesión abierta.");
            return;
        }
        if (!confirmar("Eliminar registro", "¿Eliminar permanentemente a "
                + seleccionado.nombreCompleto() + "?")) return;
        UUID id = seleccionado.id();
        establecerOcupado(true);
        estado.setText("Eliminando usuario…");
        tareas.ejecutar(() -> { servicio.eliminar(id); return id; }, eliminado -> {
            establecerOcupado(false);
            estado.setText("Usuario eliminado de PostgreSQL.");
            buscar();
        }, error -> {
            establecerOcupado(false);
            avisarError("No se pudo eliminar", error);
        });
    }

    @FXML private void cerrarSesion() { aplicacion.mostrarInicio(); }

    private void establecerOcupado(boolean valor) {
        ocupado = valor;
        botonBuscar.setDisable(valor);
        botonAgregar.setDisable(valor);
        campoBusqueda.setDisable(valor);
        tabla.setDisable(valor);
        actualizarBotones();
    }

    private void mostrarDetalle(UsuarioDTO dato) {
        detalleFoto.setImage(null);
        if (dato == null) {
            detalleNombre.setText("Selecciona una fila");
            detalleUsuario.setText("—");
            detalleCorreo.setText("—");
            detalleFecha.setText("—");
        } else {
            detalleNombre.setText(dato.nombreCompleto());
            detalleUsuario.setText("@" + dato.nombreUsuario());
            detalleCorreo.setText(dato.correo());
            detalleFecha.setText(dato.fechaRegistro() == null ? "—" : dato.fechaRegistro().format(formatoFecha));
            if (dato.fotoRuta() != null) {
                try {
                    File archivo = new File(dato.fotoRuta());
                    if (archivo.isFile()) detalleFoto.setImage(new Image(archivo.toURI().toString(), true));
                } catch (Exception ignored) {
                    // Una foto movida no debe impedir consultar el resto de la cuenta.
                }
            }
        }
        actualizarBotones();
    }

    private void actualizarBotones() {
        if (botonEditar == null || botonEliminar == null) return;
        UsuarioDTO seleccionado = tabla.getSelectionModel().getSelectedItem();
        botonEditar.setDisable(ocupado || seleccionado == null);
        botonEliminar.setDisable(ocupado || seleccionado == null ||
                conectado != null && seleccionado.id().equals(conectado.id()));
    }
}
