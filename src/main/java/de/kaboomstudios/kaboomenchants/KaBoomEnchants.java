package de.kaboomstudios.kaboomenchants;

import de.kaboomstudios.kaboomenchants.breaking.PlayerBlockBreaker;
import de.kaboomstudios.kaboomenchants.command.GiveCommand;
import de.kaboomstudios.kaboomenchants.command.HelpCommand;
import de.kaboomstudios.kaboomenchants.command.KbEnchantsCommand;
import de.kaboomstudios.kaboomenchants.command.ListCommand;
import de.kaboomstudios.kaboomenchants.command.ReloadCommand;
import de.kaboomstudios.kaboomenchants.config.LoadMode;
import de.kaboomstudios.kaboomenchants.config.PluginSettings;
import de.kaboomstudios.kaboomenchants.enchantment.CustomEnchantment;
import de.kaboomstudios.kaboomenchants.enchantment.EnchantmentCatalog;
import de.kaboomstudios.kaboomenchants.enchantment.RegisteredEnchantments;
import de.kaboomstudios.kaboomenchants.message.Messages;
import de.kaboomstudios.kaboomenchants.rules.StationRules;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hauptklasse zur Laufzeit: meldet Listener und Befehle an, sobald der Server bereit ist.
 *
 * <p>Hält zwei Stände der Konfiguration: den vom Serverstart, nach dem die Verzauberungen
 * registriert wurden und der bis zum Neustart gilt, und den aktuellen, den {@code /kbe reload}
 * erneuert und nach dem sich alles Verhalten richtet, was ohne Neustart wechseln kann.</p>
 */
public final class KaBoomEnchants extends JavaPlugin {

    private final PluginSettings startupSettings;
    private final PlayerBlockBreaker blockBreaker = new PlayerBlockBreaker();
    private volatile PluginSettings settings;
    private Messages messages;

    /** Wird vom Bootstrap erzeugt, damit beide Seiten mit demselben gelesenen Stand arbeiten. */
    KaBoomEnchants(final PluginSettings startupSettings) {
        this.startupSettings = startupSettings;
        this.settings = startupSettings;
    }

    @Override
    public void onEnable() {
        messages = new Messages(this);
        messages.reload(settings.language(), LoadMode.STARTUP);
        registerCommands();
        registerEnchantmentListeners();
    }

    private void registerEnchantmentListeners() {
        getServer().getPluginManager().registerEvents(blockBreaker, this);
        final Map<CustomEnchantment, Enchantment> registeredEnchantments = new LinkedHashMap<>();
        for (final CustomEnchantment custom : EnchantmentCatalog.ALL) {
            final Enchantment registered = RegisteredEnchantments.of(custom);
            if (registered == null) {
                getSLF4JLogger().error("The enchantment {} is not registered on this server, it has no effect. "
                        + "Restart the server after installing or updating the plugin.", custom.key().asString());
                continue;
            }
            registeredEnchantments.put(custom, registered);
            for (final Listener listener : custom.createListeners(this, registered)) {
                getServer().getPluginManager().registerEvents(listener, this);
            }
        }
        getServer().getPluginManager().registerEvents(new StationRules(this, registeredEnchantments), this);
    }

    /** Die Texte in der eingestellten Sprache; nach einem Reload dasselbe Objekt mit neuem Inhalt. */
    public Messages messages() {
        return messages;
    }

    /** Gemeinsamer Abbau „wie vom Spieler“ für alle Verzauberungen, die mehrere Blöcke abbauen. */
    public PlayerBlockBreaker blockBreaker() {
        return blockBreaker;
    }

    /**
     * Liest config.yml und die Sprachdateien neu. Ist eine davon unlesbar, bleibt der bisherige
     * Stand vollständig aktiv.
     *
     * @return die geänderten Werte, die erst nach einem Neustart wirken
     * @throws IllegalStateException wenn eine Datei unlesbar ist
     */
    public List<String> reloadPluginConfiguration() {
        final PluginSettings loaded = PluginSettings.load(getDataPath(), EnchantmentCatalog.ALL, LoadMode.RELOAD,
                getSLF4JLogger());
        // Erst die Texte: Scheitern sie, ist settings noch unverändert.
        messages.reload(loaded.language(), LoadMode.RELOAD);
        settings = loaded;
        return startupSettings.changesNeedingRestart(loaded);
    }

    /** Der aktuelle Stand, für alles, was per Reload wechseln darf. */
    public PluginSettings settings() {
        return settings;
    }

    /** Der Stand beim Serverstart, nach dem Registry und Vanilla-Tags aufgebaut sind. */
    public PluginSettings startupSettings() {
        return startupSettings;
    }

    private void registerCommands() {
        final HelpCommand help = new HelpCommand(messages);
        final var root = KbEnchantsCommand.build(help, List.of(
                new GiveCommand(this, messages),
                new ListCommand(this, messages),
                new ReloadCommand(this, messages),
                help));
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(root, "Custom enchantments: give, list, reload, help.",
                        KbEnchantsCommand.ALIASES));
    }
}
