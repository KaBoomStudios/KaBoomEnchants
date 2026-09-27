package de.kaboomstudios.kaboomenchants.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Liest und schreibt die Dateien im Plugin-Ordner.
 *
 * <p>Wird schon in der Bootstrap-Phase benutzt, in der es noch keinen Server und keine
 * Plugin-Instanz gibt. Deshalb nur Dateizugriffe und {@link YamlConfiguration}, nie
 * {@code Bukkit.*}: {@code YamlConfiguration.loadConfiguration} etwa meldet Fehler über den
 * Bukkit-Logger und würde dort mit einer Ausnahme den ganzen Serverstart abbrechen.</p>
 */
public final class PluginFiles {

    public static final String CONFIG = "config.yml";
    public static final String FALLBACK_LANGUAGE = "en";

    /** Dateien, die beim ersten Start in den Plugin-Ordner geschrieben werden. */
    private static final List<String> DEFAULT_FILES = List.of(CONFIG, "messages_en.yml", "messages_de.yml");

    private PluginFiles() {
    }

    public static String messagesFile(final String language) {
        return "messages_" + language + ".yml";
    }

    /** Legt fehlende Standarddateien an. Vorhandene Dateien bleiben unangetastet. */
    public static void writeMissingDefaults(final Path dataDirectory, final Logger logger) {
        try {
            Files.createDirectories(dataDirectory);
        } catch (final IOException exception) {
            logger.error("Could not create the plugin folder {}", dataDirectory, exception);
            return;
        }
        for (final String fileName : DEFAULT_FILES) {
            final Path target = dataDirectory.resolve(fileName);
            if (Files.exists(target)) {
                continue;
            }
            try (InputStream in = PluginFiles.class.getClassLoader().getResourceAsStream(fileName)) {
                if (in != null) {
                    Files.copy(in, target);
                }
            } catch (final IOException exception) {
                logger.error("Could not write the default file {}", target, exception);
            }
        }
    }

    /**
     * Lädt eine Datei aus dem Plugin-Ordner; fehlende Werte kommen aus der gleichnamigen Datei
     * im Plugin-Jar. Ist die Datei unlesbar, wird das gemeldet und nur der Jar-Stand benutzt,
     * damit ein Tippfehler den Server nicht am Start hindert.
     */
    public static YamlConfiguration load(final Path dataDirectory, final String fileName,
                                         final String jarFallbackName, final Logger logger) {
        final YamlConfiguration configuration = new YamlConfiguration();
        final Path file = dataDirectory.resolve(fileName);
        if (Files.isRegularFile(file)) {
            try {
                configuration.load(file.toFile());
            } catch (final IOException | InvalidConfigurationException exception) {
                logger.error("Could not read {}, using the bundled defaults instead: {}", file, exception.getMessage());
            }
        }
        configuration.setDefaults(readFromJar(jarFallbackName, logger));
        return configuration;
    }

    public static boolean existsInJar(final String fileName) {
        return PluginFiles.class.getClassLoader().getResource(fileName) != null;
    }

    public static YamlConfiguration readFromJar(final String fileName, final Logger logger) {
        final YamlConfiguration configuration = new YamlConfiguration();
        try (InputStream in = PluginFiles.class.getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) {
                return configuration;
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                configuration.load(reader);
            }
        } catch (final IOException | InvalidConfigurationException exception) {
            logger.error("Could not read the bundled file {}", fileName, exception);
        }
        return configuration;
    }
}
