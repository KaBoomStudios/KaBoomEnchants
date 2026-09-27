package de.kaboomstudios.kaboomenchants.registration;

import de.kaboomstudios.kaboomenchants.config.LoadMode;
import de.kaboomstudios.kaboomenchants.config.PluginFiles;
import de.kaboomstudios.kaboomenchants.enchantment.CustomEnchantment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Anzeigenamen der Verzauberungen in der gewählten Sprache.
 *
 * <p>Der Name steht in den Registry-Daten, die der Client beim Betreten erhält und selbst im
 * Tooltip anzeigt. Eine Übersetzung je Spieler ist darüber nicht möglich; es gilt die Sprache
 * aus der config.yml zum Zeitpunkt des Serverstarts.</p>
 */
public final class EnchantmentNames {

    private static final String SECTION = "enchantment-name.";

    private EnchantmentNames() {
    }

    public static Map<String, Component> load(final Path dataDirectory, final String language,
                                              final List<CustomEnchantment> enchantments, final Logger logger) {
        final YamlConfiguration texts = PluginFiles.loadMessages(dataDirectory, language, LoadMode.STARTUP, logger);

        final MiniMessage miniMessage = MiniMessage.miniMessage();
        final Map<String, Component> names = new LinkedHashMap<>();
        for (final CustomEnchantment enchantment : enchantments) {
            final String text = texts.getString(SECTION + enchantment.id(), enchantment.id());
            names.put(enchantment.id(), miniMessage.deserialize(text));
        }
        return Map.copyOf(names);
    }
}
