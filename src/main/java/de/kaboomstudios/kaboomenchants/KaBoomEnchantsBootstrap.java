package de.kaboomstudios.kaboomenchants;

import de.kaboomstudios.kaboomenchants.config.EnchantmentSettings;
import de.kaboomstudios.kaboomenchants.config.PluginFiles;
import de.kaboomstudios.kaboomenchants.config.PluginSettings;
import de.kaboomstudios.kaboomenchants.config.Sources;
import de.kaboomstudios.kaboomenchants.enchantment.CustomEnchantment;
import de.kaboomstudios.kaboomenchants.enchantment.EnchantmentCatalog;
import de.kaboomstudios.kaboomenchants.registration.EnchantmentNames;
import de.kaboomstudios.kaboomenchants.registration.EnchantmentRegistration;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import net.kyori.adventure.text.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Einstieg in der Bootstrap-Phase, also bevor der Server Registries einfriert und Welten lädt.
 *
 * <p>Nur hier können neue Verzauberungen in die Registry eingetragen und Vanilla-Tags
 * ergänzt werden. Die Werte, die dafür gebraucht werden, liest diese Klasse einmalig; sie
 * gelten bis zum nächsten Neustart. Alles Weitere gehört in {@link KaBoomEnchants}.</p>
 */
@SuppressWarnings("UnstableApiUsage")
public final class KaBoomEnchantsBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(final BootstrapContext context) {
        final Path dataDirectory = context.getDataDirectory();
        PluginFiles.writeMissingDefaults(dataDirectory, context.getLogger());

        final List<CustomEnchantment> enchantments = EnchantmentCatalog.ALL;
        final PluginSettings settings = PluginSettings.load(dataDirectory, enchantments, context.getLogger());
        final Map<String, Component> names =
                EnchantmentNames.load(dataDirectory, settings.language(), enchantments, context.getLogger());

        new EnchantmentRegistration(enchantments, settings, names).register(context.getLifecycleManager());
        logSummary(context, enchantments, settings);
    }

    /** Eine Zeile je Verzauberung, damit der Betreiber im Log sieht, was nach dem Start gilt. */
    private static void logSummary(final BootstrapContext context, final List<CustomEnchantment> enchantments,
                                   final PluginSettings settings) {
        for (final CustomEnchantment enchantment : enchantments) {
            final EnchantmentSettings config = settings.of(enchantment);
            context.getLogger().info("Registering {} (max level {}, {}, sources: {})",
                    enchantment.key().asString(), enchantment.maxLevel(),
                    config.enabled() ? "enabled" : "disabled", describe(config.sources()));
        }
    }

    private static String describe(final Sources sources) {
        final List<String> names = new ArrayList<>();
        if (sources.enchantingTable()) {
            names.add("enchanting table");
        }
        if (sources.anvil()) {
            names.add("anvil");
        }
        if (sources.trading()) {
            names.add("trading");
        }
        if (sources.loot()) {
            names.add("loot");
        }
        return names.isEmpty() ? "command only" : String.join(", ", names);
    }
}
