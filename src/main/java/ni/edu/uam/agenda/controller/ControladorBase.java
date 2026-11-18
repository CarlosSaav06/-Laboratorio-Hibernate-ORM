package ni.edu.uam.agenda.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import java.util.Optional;

/** Mensajes comunes para las pantallas. No maneja operaciones Hibernate. */
abstract class ControladorBase {
    protected void informar(String titulo, String texto) {
        alerta(Alert.AlertType.INFORMATION, titulo, texto).showAndWait();
    }
    protected void avisarError(String titulo, Throwable e) {
        String mensaje = e == null ? "Error desconocido." : e.getMessage();
        Throwable causa = e;
        while ((mensaje == null || mensaje.isBlank()) && causa != null && causa.getCause() != null) {
            causa = causa.getCause();
            mensaje = causa.getMessage();
        }
        alerta(Alert.AlertType.ERROR, titulo,
                mensaje == null || mensaje.isBlank() ? "Ocurrió un error. Revisa la consola." : mensaje)
                .showAndWait();
    }
    protected boolean confirmar(String titulo, String texto) {
        Alert dialogo = alerta(Alert.AlertType.CONFIRMATION, titulo, texto);
            dialogo.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> respuesta = dialogo.showAndWait();
        return respuesta.filter(ButtonType.OK::equals).isPresent();
    }
    private Alert alerta(Alert.AlertType tipo, String titulo, String texto) {
        Alert a = new Alert(tipo, texto, ButtonType.OK);
        a.setHeaderText(titulo);
        a.setTitle("Agenda ORM");
        return a;
    }
}
