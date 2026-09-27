package de.kaboomstudios.kaboomenchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.kaboomstudios.kaboomenchants.message.Messages;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Übersicht über alle Befehle: {@code /kbe help}.
 *
 * <p>Es erscheinen nur Befehle, für die der Aufrufende das Recht hat. Schreibweise und
 * Erklärung jedes Befehls stehen in den Sprachdateien unter {@code help.entries}; hier stehen
 * nur Reihenfolge und Berechtigung. Ein neuer Befehl braucht einen Eintrag in {@link #ENTRIES}
 * und zwei Texte.</p>
 *
 * <p>Im Spiel schreibt ein Klick auf einen Befehl ihn in die Chatzeile, die Erklärung erscheint
 * beim Darüberfahren. Die Konsole kann beides nicht und bekommt die Erklärung in der Zeile.</p>
 */
public final class HelpCommand implements SubCommand {

    private static final List<Entry> ENTRIES = List.of(
            new Entry("give", Permissions.GIVE),
            new Entry("list", Permissions.LIST),
            new Entry("reload", Permissions.RELOAD),
            new Entry("help", null));

    private final Messages messages;

    public HelpCommand(final Messages messages) {
        this.messages = messages;
    }

    @Override
    public String name() {
        return "help";
    }

    /** Die Hilfe darf jeder aufrufen; was er darin sieht, hängt von seinen Rechten ab. */
    @Override
    public String permission() {
        return null;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal(name()).executes(this::show);
    }

    int show(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final List<Entry> visible = visibleEntries(sender);

        if (!(sender instanceof Player)) {
            messages.send(sender, "help.console-header", Placeholder.unparsed("count", Integer.toString(visible.size())));
            for (final Entry entry : visible) {
                final Usage usage = usage(entry);
                messages.send(sender, "help.console-line",
                        Placeholder.unparsed("command", usage.command()),
                        Placeholder.unparsed("arguments", usage.arguments()),
                        Placeholder.component("description", description(entry)));
            }
            return Command.SINGLE_SUCCESS;
        }

        messages.send(sender, "help.header");
        messages.send(sender, "help.hint");
        for (final Entry entry : visible) {
            final Usage usage = usage(entry);
            final Component line = messages.get("help.line",
                            Placeholder.unparsed("command", usage.command()),
                            Placeholder.unparsed("arguments", usage.arguments()))
                    .hoverEvent(HoverEvent.showText(messages.get("help.hover",
                            Placeholder.component("description", description(entry)))))
                    .clickEvent(ClickEvent.suggestCommand(usage.suggestion()));
            sender.sendMessage(line);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static List<Entry> visibleEntries(final CommandSender sender) {
        final List<Entry> visible = new ArrayList<>();
        for (final Entry entry : ENTRIES) {
            if (entry.permission() == null || sender.hasPermission(entry.permission())) {
                visible.add(entry);
            }
        }
        return visible;
    }

    private Component description(final Entry entry) {
        return messages.get("help.entries." + entry.id() + ".description");
    }

    /**
     * Zerlegt die Schreibweise aus der Sprachdatei in den Befehl und seine Argumente. Die
     * Schreibweise wird als reiner Text eingesetzt, nicht als MiniMessage: Platzhalter wie
     * {@code <player>} erscheinen so wörtlich, ohne dass sie in der Sprachdatei maskiert werden müssen.
     */
    private Usage usage(final Entry entry) {
        final String raw = messages.raw("help.entries." + entry.id() + ".usage").strip();
        int split = raw.length();
        for (final String marker : List.of(" <", " [")) {
            final int index = raw.indexOf(marker);
            if (index >= 0 && index < split) {
                split = index;
            }
        }
        final String command = raw.substring(0, split);
        final String arguments = raw.substring(split);
        // Mit Leerzeichen am Ende, damit man nach dem Klick gleich das erste Argument tippt.
        return new Usage(command, arguments, arguments.isEmpty() ? command : command + " ");
    }

    /** @param permission Berechtigung, oder {@code null}, wenn jeder den Befehl sieht */
    private record Entry(String id, String permission) {
    }

    /**
     * @param command    der feste Teil, etwa {@code /kbe give}
     * @param arguments  der Rest mit führendem Leerzeichen, oder leer
     * @param suggestion was ein Klick in die Chatzeile schreibt
     */
    private record Usage(String command, String arguments, String suggestion) {
    }
}
