package de.kaboomstudios.kaboomenchants.config;

import de.kaboomstudios.kaboomenchants.enchantment.CustomEnchantment;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Der gelesene Inhalt der config.yml. Unveränderlich; ein Reload erzeugt ein neues Objekt. */
public record PluginSettings(String language, Map<String, EnchantmentSettings> enchantments) {

    /** Schützt vor Pfaden wie "../x" im Dateinamen der Sprachdatei. */
    private static final Pattern VALID_LANGUAGE = Pattern.compile("[a-zA-Z]{2,3}(_[a-zA-Z]{2,3})?");

    public static PluginSettings load(final Path dataDirectory, final List<CustomEnchantment> enchantments,
                                      final Logger logger) {
        final YamlConfiguration config = PluginFiles.load(dataDirectory, PluginFiles.CONFIG, PluginFiles.CONFIG, logger);

        String language = config.getString("language", PluginFiles.FALLBACK_LANGUAGE);
        if (!VALID_LANGUAGE.matcher(language).matches()) {
            logger.warn("Invalid language '{}' in config.yml, using English.", language);
            language = PluginFiles.FALLBACK_LANGUAGE;
        }

        final Map<String, EnchantmentSettings> settings = new LinkedHashMap<>();
        for (final CustomEnchantment enchantment : enchantments) {
            settings.put(enchantment.id(), EnchantmentSettings.read(enchantment, config, logger));
        }
        return new PluginSettings(language, Map.copyOf(settings));
    }

    public EnchantmentSettings of(final CustomEnchantment enchantment) {
        return enchantments.get(enchantment.id());
    }
}
