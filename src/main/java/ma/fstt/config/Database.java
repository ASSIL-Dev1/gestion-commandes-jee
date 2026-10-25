package ma.fstt.config;

import java.io.InputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
public final class Database {
    private static final Properties CONFIG = new Properties();
    static {
        try (InputStream in = Database.class.getResourceAsStream("/db.properties")) {
            if (in != null) CONFIG.load(in);
        } catch (IOException e) { throw new ExceptionInInitializerError(e); }
    }
    private Database() {}
    private static String value(String key, String env, String fallback) {
        String system = System.getProperty(key);
        if (system != null) return system;
        String environment = System.getenv(env);
        return environment != null ? environment : CONFIG.getProperty(key, fallback);
    }
    public static Connection open() throws SQLException {
        // Chargement explicite pour la découverte JDBC dans les classloaders Tomcat.
        String url = value("db.url", "DB_URL", "jdbc:mysql://localhost:3306/gestion_commandes?connectionTimeZone=UTC&characterEncoding=UTF-8");
        try { Class.forName(url.startsWith("jdbc:h2:") ? "org.h2.Driver" : "com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new SQLException("Pilote JDBC introuvable", e); }
        return DriverManager.getConnection(url, value("db.user", "DB_USER", "commandes_app"), value("db.password", "DB_PASSWORD", ""));
    }
}
