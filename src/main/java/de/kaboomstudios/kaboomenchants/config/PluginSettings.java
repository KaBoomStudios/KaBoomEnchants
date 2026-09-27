package de.kaboomstudios.kaboomenchants.config;

import de.kaboomstudios.kaboomenchants.enchantment.CustomEnchantment;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Der gelesene Inhalt der config.yml. Unveränderlich; ein Reload erzeugt ein neues Objekt. */
public record PluginSettings(String language, Map<String, EnchantmentSettings> enchantments) {

    /** Schützt vor Pfaden wie "../x" im Dateinamen der Sprachdatei. */
    private static final Pattern VALID_LANGUAGE = Pattern.compile("[a-zA-Z]{2,3}(_[a-zA-Z]{2,3})?");

    /** @throws IllegalStateException bei {@link LoadMode#RELOAD}, wenn die config.yml unlesbar ist */
    public static PluginSettings load(final Path dataDirectory, final List<CustomEnchantment> enchantments,
                                      final LoadMode mode, final Logger logger) {
        final YamlConfiguration config = PluginFiles.load(dataDirectory, PluginFiles.CONFIG, PluginFiles.CONFIG, mode, logger);

        String language = config.getString("language", PluginFiles.FALLBACK_LANGUAGE);
        if (!VALID_LANGUAGE.matcher(language).matches()) {
            logger.warn("Invalid language '{}' in config.yml, using English.", language);
            language = PluginFiles.FALLBACK_LANGUAGE;
        }

        final Map<String, EnchantmentSettings> settings = new LinkedHashMap<>();
        for (final CustomEnchantment enchantment : enchantments) {
            settings.put(enchantment.id(), EnchantmentSettings.read(enchantment, config, logger));
        }
        return new PluginSettings(language, Collections.unmodifiableMap(settings));
    }

    public EnchantmentSettings of(final CustomEnchantment enchantment) {
        return enchantments.get(enchantment.id());
    }

    /**
     * Die Werte, die in {@code newer} anders sind, aber erst nach einem Neustart wirken, weil sie
     * in der Registry oder in Vanilla-Tags stehen. Als Pfade der config.yml, für die Meldung an den
     * Betreiber.
     */
    public List<String> changesNeedingRestart(final PluginSettings newer) {
        final List<String> changes = new ArrayList<>();
        if (!language.equals(newer.language)) {
            changes.add("language");
        }
        for (final Map.Entry<String, EnchantmentSettings> entry : enchantments.entrySet()) {
            final EnchantmentSettings before = entry.getValue();
            final EnchantmentSettings after = newer.enchantments.get(entry.getKey());
            if (after == null) {
                continue;
            }
            final String path = entry.getKey() + ".";
            addIfChanged(changes, path + "enabled", before.enabled(), after.enabled());
            addIfChanged(changes, path + "sources.enchanting-table",
                    before.sources().enchantingTable(), after.sources().enchantingTable());
            addIfChanged(changes, path + "sources.trading", before.sources().trading(), after.sources().trading());
            addIfChanged(changes, path + "sources.loot", before.sources().loot(), after.sources().loot());
            addIfChanged(changes, path + "mending-allowed", before.mendingAllowed(), after.mendingAllowed());
            addIfChanged(changes, path + "weight", before.weight(), after.weight());
        }
        return changes;
    }

    private static void addIfChanged(final List<String> changes, final String path, final Object before,
                                     final Object after) {
        if (!before.equals(after)) {
            changes.add(path);
        }
    }
}
