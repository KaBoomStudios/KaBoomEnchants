package de.kaboomstudios.kaboomenchants.enchantment.tunnelbore;

import de.kaboomstudios.kaboomenchants.config.EnchantmentSettings;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.slf4j.Logger;

import java.util.EnumSet;
import java.util.Set;

/**
 * Werte, die nur Tunnel Bore kennt. Alle per Reload änderbar.
 *
 * @param durabilityPerExtraBlock Anteil des normalen Werkzeugabzugs je zusätzlichem Block
 * @param sneakDisables           beim Schleichen nur den einen Block abbauen
 * @param onlyEffectiveBlocks     nur Blöcke, für die das Werkzeug gemacht ist
 * @param blockedBlocks           nie im Bereich abbauen
 */
record TunnelBoreSettings(double durabilityPerExtraBlock, boolean sneakDisables, boolean onlyEffectiveBlocks,
                          Set<Material> blockedBlocks) {

    static TunnelBoreSettings read(final EnchantmentSettings settings, final Logger logger) {
        final ConfigurationSection section = settings.section();

        double durability = section.getDouble("durability-per-extra-block");
        if (durability < 0 || durability > 1) {
            logger.warn("tunnel_bore.durability-per-extra-block must be between 0 and 1, got {}.", durability);
            durability = Math.clamp(durability, 0.0, 1.0);
        }

        final Set<Material> blocked = EnumSet.noneOf(Material.class);
        for (final String name : section.getStringList("blocked-blocks")) {
            final Material material = Material.matchMaterial(name);
            if (material == null || !material.isBlock()) {
                logger.warn("tunnel_bore.blocked-blocks: '{}' is not a block and is ignored.", name);
                continue;
            }
            blocked.add(material);
        }

        return new TunnelBoreSettings(durability, section.getBoolean("sneak-disables"),
                section.getBoolean("only-effective-blocks"), Set.copyOf(blocked));
    }
}
