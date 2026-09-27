package de.kaboomstudios.kaboomenchants.config;

import de.kaboomstudios.kaboomenchants.enchantment.CustomEnchantment;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.slf4j.Logger;

/**
 * Einstellungen einer Verzauberung aus ihrem Block in der config.yml.
 *
 * @param sources   bereits bereinigt: ausgeschaltete Verzauberungen haben keine Wege, und der
 *                  Verzauberungstisch fehlt, wo er technisch nicht greifen kann
 * @param giveItem  Name des Grund-Items für {@code kbe give ... item}; wird erst zur Laufzeit
 *                  aufgelöst, weil Materialien in der Bootstrap-Phase noch nicht sicher greifbar sind
 * @param section   der ganze Block, für Werte, die nur eine einzelne Verzauberung kennt; fehlende
 *                  Werte liefert er aus der mitgelieferten config.yml
 */
public record EnchantmentSettings(
        boolean enabled,
        Sources sources,
        boolean grindstoneRemovable,
        boolean repairable,
        boolean mendingAllowed,
        boolean netheriteUpgrade,
        int weight,
        String giveItem,
        ConfigurationSection section) {

    /** Grenzen, die die Registry für das Gewicht vorschreibt. */
    private static final int MIN_WEIGHT = 1;
    private static final int MAX_WEIGHT = 1024;

    /**
     * Liest über Pfade ab der Wurzel: Nur so greifen die Standardwerte aus dem Jar, wenn ein
     * Schalter in der Datei des Betreibers fehlt. Getter mit eigenem Standardwert würden sie übergehen.
     */
    static EnchantmentSettings read(final CustomEnchantment enchantment, final Configuration root, final Logger logger) {
        final String path = "enchantments." + enchantment.id();
        if (!root.isConfigurationSection(path)) {
            logger.warn("No block for the enchantment '{}' in config.yml, using the bundled defaults.", enchantment.id());
        }

        final boolean enabled = root.getBoolean(path + ".enabled");
        Sources sources = new Sources(
                root.getBoolean(path + ".sources.enchanting-table"),
                root.getBoolean(path + ".sources.anvil"),
                root.getBoolean(path + ".sources.trading"),
                root.getBoolean(path + ".sources.loot"));
        if (!enabled) {
            sources = Sources.NONE;
        } else if (sources.enchantingTable() && !enchantment.enchantingTableApplicable()) {
            logger.warn("The enchanting table cannot apply '{}', sources.enchanting-table is ignored.", enchantment.id());
            sources = new Sources(false, sources.anvil(), sources.trading(), sources.loot());
        }

        int weight = root.getInt(path + ".weight", MIN_WEIGHT);
        if (weight < MIN_WEIGHT || weight > MAX_WEIGHT) {
            logger.warn("weight of '{}' must be between {} and {}, got {}.",
                    enchantment.id(), MIN_WEIGHT, MAX_WEIGHT, weight);
            weight = Math.clamp(weight, MIN_WEIGHT, MAX_WEIGHT);
        }

        ConfigurationSection section = root.getConfigurationSection(path);
        if (section == null) {
            section = new MemoryConfiguration();
        }

        return new EnchantmentSettings(
                enabled,
                sources,
                root.getBoolean(path + ".grindstone-removable"),
                root.getBoolean(path + ".repairable"),
                root.getBoolean(path + ".mending-allowed"),
                root.getBoolean(path + ".netherite-upgrade"),
                weight,
                root.getString(path + ".give-item", ""),
                section);
    }
}
