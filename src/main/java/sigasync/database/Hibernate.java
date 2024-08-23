package sigasync.database;

import java.io.File;
import java.util.Properties;
import java.util.Set;

import javax.imageio.spi.ServiceRegistry;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import jakarta.persistence.Entity;
import sigasync.Constants;

public class Hibernate {
    private static SessionFactory sessionFactory;

    /** To be used in database operations. */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            // Configuration
            Configuration cfg = Hibernate.getH2Configuration();

            // Classes
            // addClassesToConfig(cfg, Activity.class);
            addClassesToConfig(cfg);

            // Registry
            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(cfg.getProperties())
                .build();

            sessionFactory = cfg.buildSessionFactory(serviceRegistry);
        }

        return sessionFactory;
    }

    private static Configuration getH2Configuration() {
        Configuration config = new Configuration();
        Properties properties = new Properties();

        // Define properties
        properties.put(Environment.SHOW_SQL, "true");
        properties.put(Environment.FORMAT_SQL, "true");
        properties.put(Environment.URL, String.format("jdbc:h2:%s", getDatabaseFile().toString()));
        properties.put(Environment.HBM2DDL_AUTO, "update");

        config.setProperties(properties);
        return config;
    }

    /**
     * Automatically detect all {@link Entity} classes and add them to the ORM.
     */
    private static void addClassesToConfig(Configuration cfg) {
        Reflections reflections = new Reflections(
            ConfigurationBuilder .build()
                .setUrls(ClasspathHelper.forPackage("sigasync.models"))
        );

        for (Class<? extends Object> c : reflections.getTypesAnnotatedWith(Entity.class)) {
            cfg.addAnnotatedClass(c);
        }
    }

    /*
     * Get platform dependent project directory.
     */
    private static File getDatabaseFile() {
        String platform = System.getProperty("os.name").toLowerCase().strip();

        // Define the path
        String dataPath;
        if (platform.startsWith("win")) {
            dataPath = System.getenv("LOCALAPPDATA") + "\\" + Constants.PROJECT_NAME + "\\";
        } else {
            dataPath = System.getProperty("user.home") + "/.local/share/" + Constants.PROJECT_NAME + "/";
        }

        // Make sure that it exists
        File dataDir = new File(dataPath);
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        // Return database file
        File databaseFile = new File(dataPath + "database");
        return databaseFile;
    }
}
