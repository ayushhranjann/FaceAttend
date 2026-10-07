package com.faceattend.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {

    private static final String CONFIG_FILE = "/db.properties";
    private static Properties props;

    private DBConnection() {
    }

    private static synchronized Properties loadProperties() {
        if (props == null) {
            props = new Properties();
            try (InputStream in = DBConnection.class.getResourceAsStream(CONFIG_FILE)) {
                if (in == null) {
                    throw new RuntimeException("Could not find " + CONFIG_FILE + " on classpath");
                }
                props.load(in);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load database configuration", e);
            }
        }
        return props;
    }

    public static Connection getConnection() throws SQLException {
        Properties p = loadProperties();
        String url = p.getProperty("db.url");
        String user = p.getProperty("db.user");
        String password = p.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}
