package de.kaboomstudios.kaboomenchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.kaboomstudios.kaboomenchants.KaBoomEnchants;
import de.kaboomstudios.kaboomenchants.config.EnchantmentSettings;
import de.kaboomstudios.kaboomenchants.enchantment.CustomEnchantment;
import de.kaboomstudios.kaboomenchants.enchantment.EnchantmentCatalog;
import de.kaboomstudios.kaboomenchants.enchantment.RegisteredEnchantments;
import de.kaboomstudios.kaboomenchants.message.Messages;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code /kbe list}: alle Verzauberungen mit höchster Stufe und den Wegen, die gerade wirklich offen sind.
 *
 * <p>Tisch, Handel und Loot zeigen den Stand seit dem Serverstart, weil sie in Vanilla-Tags
 * stehen; der Amboss den aktuellen Stand der Konfiguration, weil er per Reload wechselt.
 * Was nach einem Reload erst beim Neustart wirkt, meldet {@code /kbe reload}.</p>
 */
public final class ListCommand implements SubCommand {

    private final KaBoomEnchants plugin;
    private final Messages messages;

    public ListCommand(final KaBoomEnchants plugin, final Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public String name() {
        return "list";
    }

    @Override
    public String permission() {
        return Permissions.LIST;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal(name()).executes(this::list);
    }

    private int list(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        messages.send(sender, "list.header",
                Placeholder.unparsed("count", Integer.toString(EnchantmentCatalog.ALL.size())));
        for (final CustomEnchantment custom : EnchantmentCatalog.ALL) {
            final Enchantment enchantment = RegisteredEnchantments.of(custom);
            messages.send(sender, "list.line",
                    Placeholder.component("name", enchantment != null ? enchantment.description() : Component.text(custom.id())),
                    Placeholder.unparsed("id", custom.id()),
                    Placeholder.unparsed("max", Integer.toString(custom.maxLevel())),
                    Placeholder.component("sources", sources(custom)));
        }
        return Command.SINGLE_SUCCESS;
    }

    private Component sources(final CustomEnchantment custom) {
        final EnchantmentSettings startup = plugin.startupSettings().of(custom);
        if (!startup.enabled()) {
            return messages.get("list.disabled");
        }
        final EnchantmentSettings current = plugin.settings().of(custom);

        final List<String> open = new ArrayList<>();
        if (startup.sources().enchantingTable()) {
            open.add("enchanting-table");
        }
        if (current.sources().anvil()) {
            open.add("anvil");
        }
        if (startup.sources().trading()) {
            open.add("trading");
        }
        if (startup.sources().loot()) {
            open.add("loot");
        }
        if (open.isEmpty()) {
            return messages.get("list.command-only");
        }

        final List<Component> parts = new ArrayList<>();
        for (final String source : open) {
            parts.add(messages.get("list.source-on",
                    Placeholder.component("source", messages.get("list.source." + source))));
        }
        return Component.join(JoinConfiguration.separator(messages.get("list.separator")), parts);
    }
}
