package com.automation.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Single source of configuration. Order of precedence: -Dkey=value > config-<env>.properties > config.properties. */
public final class ConfigReader {

    private static final Properties PROPS = new Properties();

    static {
        load("config/config.properties");
        String env = System.getProperty("env", PROPS.getProperty("env", "qa"));
        load("config/config-" + env + ".properties");
        PROPS.setProperty("env", env);
    }

    private ConfigReader() { }

    private static void load(String resource) {
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream(resource)) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + resource, e);
        }
    }

    public static String get(String key) {
        String value = System.getProperty(key, PROPS.getProperty(key));
        if (value == null) {
            throw new IllegalArgumentException("Missing config key: " + key);
        }
        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}
