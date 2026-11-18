package ni.edu.uam.agenda.util;

import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** El Task corre fuera del hilo de JavaFX. Sus callbacks corren en el JavaFX Application Thread. */
public final class EjecutorTareas {
    private final ExecutorService workers = Executors.newFixedThreadPool(3, tarea -> {
        Thread hilo = new Thread(tarea, "hibernate-io");
        hilo.setDaemon(true);
        return hilo;
    });

    public <T> void ejecutar(Callable<T> operacion, Consumer<T> exito, Consumer<Throwable> fallo) {
        Task<T> tarea = new Task<>() {
            @Override protected T call() throws Exception { return operacion.call(); }
        };
        tarea.setOnSucceeded(evento -> exito.accept(tarea.getValue()));
        tarea.setOnFailed(evento -> fallo.accept(tarea.getException()));
        enviar(tarea);
    }

    public void enviar(Task<?> tarea) { workers.execute(tarea); }
    public void cerrar() { workers.shutdownNow(); }
}
