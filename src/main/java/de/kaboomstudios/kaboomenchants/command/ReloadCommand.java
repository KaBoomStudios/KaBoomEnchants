package de.kaboomstudios.kaboomenchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.kaboomstudios.kaboomenchants.KaBoomEnchants;
import de.kaboomstudios.kaboomenchants.message.Messages;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Lädt Konfiguration und Sprachdateien neu, ohne den Server zu starten. Werte, die in der
 * Registry oder in Vanilla-Tags stehen, wirken erst nach einem Neustart; welche davon geändert
 * wurden, wird ausdrücklich gemeldet, damit niemand eine wirkungslose Änderung für aktiv hält.
 */
public final class ReloadCommand implements SubCommand {

    private final KaBoomEnchants plugin;
    private final Messages messages;

    public ReloadCommand(final KaBoomEnchants plugin, final Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public String permission() {
        return Permissions.RELOAD;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal(name()).executes(this::reload);
    }

    private int reload(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final List<String> restartNeeded;
        try {
            restartNeeded = plugin.reloadPluginConfiguration();
        } catch (final IllegalStateException exception) {
            // Unlesbare Datei, meist ein Tippfehler: Die Meldung nennt Datei und Zeile, ein
            // Stacktrace hilft dem Betreiber dabei nicht. Der bisherige Stand bleibt geladen.
            plugin.getSLF4JLogger().error("Reload failed. {}", exception.getMessage());
            messages.send(sender, "plugin.reload-failed");
            return 0;
        } catch (final RuntimeException exception) {
            plugin.getSLF4JLogger().error("Reload failed.", exception);
            messages.send(sender, "plugin.reload-failed");
            return 0;
        }
        messages.send(sender, "plugin.reloaded");
        if (!restartNeeded.isEmpty()) {
            messages.send(sender, "plugin.restart-needed",
                    Placeholder.unparsed("changes", String.join(", ", restartNeeded)));
        }
        return Command.SINGLE_SUCCESS;
    }
}
