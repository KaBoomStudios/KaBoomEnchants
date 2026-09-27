package de.kaboomstudios.kaboomenchants.message;

import de.kaboomstudios.kaboomenchants.config.LoadMode;
import de.kaboomstudios.kaboomenchants.config.PluginFiles;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

/**
 * Sämtliche Texte des Plugins, geladen aus einer Sprachdatei und in MiniMessage geschrieben.
 *
 * <p>Kein Text steht im Quelltext: Wer die Ausgabe ändern will, ändert nur die YAML-Datei.
 * Die mitgelieferte englische Datei dient zugleich als Rückfall, damit eine unvollständige
 * Übersetzung keine leeren Nachrichten erzeugt.</p>
 */
public final class Messages {

    private final Plugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private YamlConfiguration texts = new YamlConfiguration();
    private TagResolver prefix = TagResolver.empty();

    public Messages(final Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Lädt die Sprachdatei zur übergebenen Sprache neu.
     *
     * @throws IllegalStateException bei {@link LoadMode#RELOAD}, wenn die Datei unlesbar ist;
     *                               die bisherigen Texte bleiben dann aktiv
     */
    public void reload(final String language, final LoadMode mode) {
        addNewTexts(PluginFiles.messagesFile(language));
        final YamlConfiguration loaded = PluginFiles.loadMessages(plugin.getDataPath(), language, mode,
                plugin.getSLF4JLogger());
        this.texts = loaded;

        final String rawPrefix = loaded.getString("prefix", "");
        // Bewusst ohne den Präfix-Platzhalter aufgelöst: sonst würde sich das Präfix selbst enthalten.
        this.prefix = TagResolver.resolver("prefix", Tag.selfClosingInserting(miniMessage.deserialize(rawPrefix)));
    }

    /**
     * Baut den Text zu einem Schlüssel. Zusätzliche Platzhalter werden über
     * {@link net.kyori.adventure.text.minimessage.tag.resolver.Placeholder} übergeben.
     */
    public Component get(final String key, final TagResolver... placeholders) {
        final String raw = texts.getString(key);
        if (raw == null) {
            // Auffällig, aber harmlos: Der Schlüssel erscheint im Spiel und in der Konsole,
            // statt dass die Nachricht spurlos verschwindet.
            plugin.getSLF4JLogger().warn("No text found for the key '{}'.", key);
            return Component.text(key);
        }
        return miniMessage.deserialize(raw, TagResolver.resolver(prefix, TagResolver.resolver(placeholders)));
    }

    /**
     * Der Text zu einem Schlüssel, unverändert und ohne MiniMessage auszuwerten. Gedacht für
     * Texte, die wörtlich erscheinen sollen, etwa die Schreibweise eines Befehls.
     */
    public String raw(final String key) {
        final String raw = texts.getString(key);
        if (raw == null) {
            plugin.getSLF4JLogger().warn("No text found for the key '{}'.", key);
            return key;
        }
        return raw;
    }

    /** Schickt den Text zu einem Schlüssel an einen Spieler oder die Konsole. */
    public void send(final CommandSender receiver, final String key, final TagResolver... placeholders) {
        receiver.sendMessage(get(key, placeholders));
    }

    /**
     * Ergänzt in der Sprachdatei des Servers die Texte, die eine neuere Fassung des Plugins
     * mitbringt.
     *
     * <p>Ohne das würden nach einer Aktualisierung neue Meldungen auf Englisch erscheinen,
     * obwohl es sie in der eingestellten Sprache gibt. Eigene Änderungen an bereits
     * vorhandenen Texten bleiben unangetastet.</p>
     */
    private void addNewTexts(final String fileName) {
        final File file = new File(plugin.getDataFolder(), fileName);
        if (!file.isFile() || !PluginFiles.existsInJar(fileName)) {
            // Nur mitgelieferte Sprachen lassen sich ergänzen; eine selbst angelegte hat kein Vorbild.
            return;
        }
        final YamlConfiguration bundled = PluginFiles.readFromJar(fileName, plugin.getSLF4JLogger());

        final YamlConfiguration onDisk = new YamlConfiguration();
        try {
            onDisk.load(file);
        } catch (final IOException | InvalidConfigurationException exception) {
            // Unlesbar: nicht anfassen, sonst ginge der Inhalt beim Speichern verloren.
            return;
        }

        boolean somethingMissing = false;
        for (final String key : bundled.getKeys(true)) {
            if (!bundled.isConfigurationSection(key) && !onDisk.contains(key, true)) {
                somethingMissing = true;
                break;
            }
        }
        if (!somethingMissing) {
            return;
        }

        onDisk.setDefaults(bundled);
        onDisk.options().copyDefaults(true);
        try {
            onDisk.save(file);
            plugin.getSLF4JLogger().info("Added new texts to {}.", file.getName());
        } catch (final IOException exception) {
            plugin.getSLF4JLogger().warn("Could not write new texts to {}: {}", file.getName(), exception.getMessage());
        }
    }
}
