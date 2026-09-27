package de.kaboomstudios.kaboomenchants.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.List;

/**
 * Baut {@code /kbenchants} aus den Unterbefehlen zusammen.
 *
 * <p>Die Rechte stehen als Bedingung am jeweiligen Zweig. Brigadier blendet Zweige ohne Recht
 * vollständig aus, auch in der Tab-Vervollständigung: Wer {@code give} nicht darf, sieht es nicht.
 * Der Hauptbefehl ohne Argument zeigt die Hilfe, die jeder aufrufen darf.</p>
 */
public final class KbEnchantsCommand {

    public static final String NAME = "kbenchants";
    public static final List<String> ALIASES = List.of("kbe");

    private KbEnchantsCommand() {
    }

    public static LiteralCommandNode<CommandSourceStack> build(final HelpCommand help, final List<SubCommand> subCommands) {
        final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(NAME).executes(help::show);
        for (final SubCommand subCommand : subCommands) {
            final String permission = subCommand.permission();
            root.then(subCommand.node().requires(source ->
                    permission == null || source.getSender().hasPermission(permission)));
        }
        return root.build();
    }
}
