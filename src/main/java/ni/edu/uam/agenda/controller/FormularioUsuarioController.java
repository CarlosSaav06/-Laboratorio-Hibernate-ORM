package ni.edu.uam.agenda.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import ni.edu.uam.agenda.dto.DatosUsuario;
import ni.edu.uam.agenda.dto.UsuarioDTO;
import ni.edu.uam.agenda.service.ValidacionUsuario;

import java.io.File;

/** Solo controla campos de formulario. La persistencia la coordina la pantalla padre. */
public final class FormularioUsuarioController {
    @FXML private Label etiquetaClave;
    @FXML private TextField campoNombre;
    @FXML private TextField campoApellido;
    @FXML private TextField campoUsuario;
    @FXML private TextField campoCorreo;
    @FXML private PasswordField campoClave;
    @FXML private TextField campoFoto;
    @FXML private ImageView vistaFoto;
    private boolean nuevo;
    private Window ventana;

    public void preparar(Window owner, UsuarioDTO usuario) {
        ventana = owner;
        nuevo = (usuario == null);
        etiquetaClave.setText(nuevo ? "Contraseña (mínimo 8 caracteres)" : "Nueva contraseña (opcional)");
        if (usuario != null) {
            campoNombre.setText(usuario.nombre());
            campoApellido.setText(usuario.apellido());
            campoUsuario.setText(usuario.nombreUsuario());
            campoCorreo.setText(usuario.correo());
            campoFoto.setText(usuario.fotoRuta());
            cargarVistaFoto();
        }
    }

    @FXML private void elegirImagen() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Seleccionar fotografía");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Imágenes JPG/PNG", "*.jpg", "*.jpeg", "*.png"));
        File imagen = selector.showOpenDialog(ventana);
        if (imagen != null) {
            campoFoto.setText(imagen.getAbsolutePath());
            cargarVistaFoto();
        }
    }

    @FXML private void quitarImagen() {
        campoFoto.clear();
        vistaFoto.setImage(null);
    }

    private void cargarVistaFoto() {
        try {
            if (campoFoto.getText() != null && !campoFoto.getText().isBlank()) {
                File archivo = new File(campoFoto.getText());
                vistaFoto.setImage(archivo.isFile() ? new Image(archivo.toURI().toString(), true) : null);
            }
        } catch (Exception ex) {
            vistaFoto.setImage(null);
        }
    }

    public DatosUsuario extraerDatosValidados() {
        DatosUsuario datos = new DatosUsuario(campoNombre.getText(), campoApellido.getText(),
                campoUsuario.getText(), campoCorreo.getText(), campoClave.getText(), campoFoto.getText());
        ValidacionUsuario.verificar(datos, nuevo);
        return datos;
    }
}
