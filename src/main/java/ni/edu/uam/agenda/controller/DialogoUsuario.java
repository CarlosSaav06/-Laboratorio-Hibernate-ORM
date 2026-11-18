package ni.edu.uam.agenda.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar;
import javafx.stage.Window;
import ni.edu.uam.agenda.dto.DatosUsuario;
import ni.edu.uam.agenda.dto.UsuarioDTO;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/** Diálogo reutilizable para registro y edición, con validación sin cerrar la ventana. */
public final class DialogoUsuario {
    private DialogoUsuario() { }

    public static Optional<DatosUsuario> mostrar(Window owner, UsuarioDTO existente) {
        try {
            FXMLLoader loader = new FXMLLoader(DialogoUsuario.class.getResource("/fxml/formulario-usuario.fxml"));
            Parent contenido = loader.load();
            FormularioUsuarioController formulario = loader.getController();
            formulario.preparar(owner, existente);

            Dialog<ButtonType> dialogo = new Dialog<>();
            dialogo.initOwner(owner);
            dialogo.setTitle(existente == null ? "Registrar usuario" : "Editar usuario");
            dialogo.setHeaderText(existente == null ? "Nueva cuenta" : "Actualizar datos de cuenta");
            dialogo.getDialogPane().setContent(contenido);
            dialogo.getDialogPane().getStylesheets()
                    .add(DialogoUsuario.class.getResource("/css/estilos.css").toExternalForm());
            ButtonType aceptar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialogo.getDialogPane().getButtonTypes().addAll(aceptar, ButtonType.CANCEL);
            AtomicReference<DatosUsuario> resultado = new AtomicReference<>();

            Button guardar = (Button) dialogo.getDialogPane().lookupButton(aceptar);
            guardar.addEventFilter(ActionEvent.ACTION, evento -> {
                try {
                    resultado.set(formulario.extraerDatosValidados());
                } catch (IllegalArgumentException ex) {
                    evento.consume();
                    Alert alerta = new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK);
                    alerta.initOwner(owner);
                    alerta.setTitle("Revisar datos");
                    alerta.setHeaderText("Hay un dato por corregir");
                    alerta.showAndWait();
                }
            });
            Optional<ButtonType> seleccion = dialogo.showAndWait();
            return seleccion.filter(aceptar::equals).map(value -> resultado.get());
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar el formulario de usuarios.", ex);
        }
    }
}
