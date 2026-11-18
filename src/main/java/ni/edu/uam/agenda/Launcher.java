package ni.edu.uam.agenda;

import javafx.application.Application;

/** Punto de entrada para ejecutar JavaFX a través de Maven en IntelliJ. */
public final class Launcher {
    private Launcher() { }
    public static void main(String[] args) {
        Application.launch(AgendaApplication.class, args);
    }
}
