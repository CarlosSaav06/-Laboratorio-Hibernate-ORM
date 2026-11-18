package ni.edu.uam.agenda.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ni.edu.uam.agenda.model.Usuario;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

/** Una SessionFactory para toda la aplicación; una Session NUEVA para cada operación. */
public final class HibernateProvider implements AutoCloseable {
    private final HikariDataSource pool;
    private final SessionFactory sessionFactory;

    private HibernateProvider(HikariDataSource pool, SessionFactory sessionFactory) {
        this.pool = pool;
        this.sessionFactory = sessionFactory;
    }

    public static HibernateProvider iniciar() throws Exception {
        ConfiguracionBD datos = ConfiguracionBD.cargar();
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(datos.url());
        hikari.setUsername(datos.usuario());
        hikari.setPassword(datos.clave());
        hikari.setPoolName("AgendaORM-Pool");
        hikari.setMaximumPoolSize(5);
        hikari.setMinimumIdle(1);
        hikari.setConnectionTimeout(10_000);
        hikari.setValidationTimeout(5_000);
        HikariDataSource datasource = new HikariDataSource(hikari);

        StandardServiceRegistry registro = null;
        try {
            registro = new StandardServiceRegistryBuilder()
                    .applySetting(AvailableSettings.DATASOURCE, datasource)
                    .applySetting(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect")
                    .applySetting(AvailableSettings.HBM2DDL_AUTO, "validate")
                    .applySetting(AvailableSettings.SHOW_SQL, "false")
                    .applySetting(AvailableSettings.FORMAT_SQL, "true")
                    .build();
            SessionFactory factory = new MetadataSources(registro)
                    .addAnnotatedClass(Usuario.class)
                    .buildMetadata()
                    .buildSessionFactory();
            return new HibernateProvider(datasource, factory);
        } catch (Exception e) {
            if (registro != null) StandardServiceRegistryBuilder.destroy(registro);
            datasource.close();
            throw e;
        }
    }

    public SessionFactory getSessionFactory() { return sessionFactory; }

    @Override
    public void close() {
        if (sessionFactory != null && !sessionFactory.isClosed()) sessionFactory.close();
        pool.close();
    }
}
