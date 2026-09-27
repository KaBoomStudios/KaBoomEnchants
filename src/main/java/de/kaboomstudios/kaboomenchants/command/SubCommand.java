package de.kaboomstudios.kaboomenchants.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

/**
 * Ein Unterbefehl von {@code /kbenchants}.
 *
 * <p>Jeder Unterbefehl ist eine eigene Klasse und wird in {@link KbEnchantsCommand} eingetragen.
 * So wächst der Befehlsbaum, ohne dass bestehender Code umgebaut werden muss.</p>
 */
public interface SubCommand {

    /** Erstes Wort hinter {@code /kbenchants}, immer klein geschrieben. */
    String name();

    /** Berechtigung, die der Aufrufende braucht, oder {@code null}, wenn jeder ihn benutzen darf. */
    String permission();

    /**
     * Der Zweig des Befehlsbaums ab dem Namen. Die Berechtigung setzt {@link KbEnchantsCommand}
     * einheitlich, der Zweig selbst braucht sie nicht zu prüfen.
     */
    LiteralArgumentBuilder<CommandSourceStack> node();
}
