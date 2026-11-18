package ni.edu.uam.agenda.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Credenciales externas: db.properties, variables de entorno o propiedades -D. */
public record ConfiguracionBD(String url, String usuario, String clave) {
    public static ConfiguracionBD cargar() throws IOException {
        Properties archivo = new Properties();
        Path ruta = Path.of("db.properties");
        if (Files.exists(ruta)) {
            try (InputStream entrada = Files.newInputStream(ruta)) {
                archivo.load(entrada);
            }
        }
        String url = leer("DB_URL", "db.url", archivo);
        String usuario = leer("DB_USER", "db.user", archivo);
        String clave = leer("DB_PASSWORD", "db.password", archivo);
        if (url == null || usuario == null || clave == null || clave.isBlank()
                || clave.equals("COLOCA_AQUI_TU_CLAVE")) {
            throw new IllegalStateException("Falta configurar db.properties. Copia db.properties.example "
                    + "como db.properties y completa la URL, el usuario y la contraseña de PostgreSQL.");
        }
        if (!url.startsWith("jdbc:postgresql://")) {
            throw new IllegalArgumentException("La URL debe iniciar con jdbc:postgresql://");
        }
        return new ConfiguracionBD(url, usuario, clave);
    }

    private static String leer(String variable, String propiedad, Properties archivo) {
        String valorSistema = System.getProperty(propiedad);
        if (valorSistema != null && !valorSistema.isBlank()) return valorSistema.trim();
        String valorEntorno = System.getenv(variable);
        if (valorEntorno != null && !valorEntorno.isBlank()) return valorEntorno.trim();
        String valorArchivo = archivo.getProperty(propiedad);
        return valorArchivo == null ? null : valorArchivo.trim();
    }
}
