package de.kaboomstudios.kaboomenchants.enchantment.treefeller;

import de.kaboomstudios.kaboomenchants.config.EnchantmentSettings;
import org.bukkit.configuration.ConfigurationSection;
import org.slf4j.Logger;

/**
 * Werte, die nur Tree Feller kennt. Alle per Reload änderbar.
 *
 * @param durabilityPerExtraBlock Anteil des normalen Werkzeugabzugs je zusätzlichem Stamm
 * @param sneakDisables           beim Schleichen nur den einen Stamm abbauen
 * @param maxBlocks               höchstens so viele zusätzliche Stämme je Baum
 * @param minNaturalLeaves        so viele natürliche Blätter müssen am Stamm hängen
 */
record TreeFellerSettings(double durabilityPerExtraBlock, boolean sneakDisables, int maxBlocks,
                          int minNaturalLeaves) {

    static TreeFellerSettings read(final EnchantmentSettings settings, final Logger logger) {
        final ConfigurationSection section = settings.section();

        double durability = section.getDouble("durability-per-extra-block");
        if (durability < 0 || durability > 1) {
            logger.warn("tree_feller.durability-per-extra-block must be between 0 and 1, got {}.", durability);
            durability = Math.clamp(durability, 0.0, 1.0);
        }
        int maxBlocks = section.getInt("max-blocks");
        if (maxBlocks < 1) {
            logger.warn("tree_feller.max-blocks must be at least 1, got {}.", maxBlocks);
            maxBlocks = 1;
        }
        int minLeaves = section.getInt("min-natural-leaves");
        if (minLeaves < 0) {
            logger.warn("tree_feller.min-natural-leaves must not be negative, got {}.", minLeaves);
            minLeaves = 0;
        }
        return new TreeFellerSettings(durability, section.getBoolean("sneak-disables"), maxBlocks, minLeaves);
    }
}
