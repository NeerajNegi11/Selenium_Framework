package utils;

import utils.crypto.CryptoUtil;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties props = new Properties();

    // Runs once: loads src/main/resources/config.properties
    static {
        try (InputStream is = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (is == null) {
                throw new RuntimeException("config.properties not found in src/main/resources");
            }
            props.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    // Normal (non-secret) values.
    // Order: -Dkey=value (command line) > environment variable > config.properties
    public static String get(String key) {
        String sys = System.getProperty(key);
        if (sys != null) return sys;

        String env = System.getenv(key.toUpperCase().replace(".", "_"));   // browser -> BROWSER
        if (env != null) return env;

        return props.getProperty(key);
    }

    // Secret values (username, password).
    // Order: environment variable > ENC(...) in config.properties. Plain text is rejected.
    public static String getSecret(String envKey) {
        // 1. Environment variable / Jenkins credential (e.g. APP_PASSWORD)
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) return env;

        // 2. Encrypted value in config.properties (APP_PASSWORD -> app.password)
        String propKey = envKey.toLowerCase().replace("_", ".");
        String value = props.getProperty(propKey);

        if (value != null && !value.isBlank()) {
            value = value.trim();
            if (value.startsWith("ENC(") && value.endsWith(")")) {
                String encrypted = value.substring(4, value.length() - 1);   // strip ENC( and )
                return CryptoUtil.decrypt(encrypted);
            }
            throw new IllegalStateException("Plain-text secret found for '" + propKey
                    + "' in config.properties. Encrypt it with EncryptTool or set env variable "
                    + envKey + ".");
        }

        // 3. Not found anywhere
        throw new IllegalStateException("Missing required secret: " + envKey
                + ". Set env variable " + envKey + " or add " + propKey + "=ENC(...) to config.properties.");
    }
}
