package de.kaboomstudios.kaboomenchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.kaboomstudios.kaboomenchants.KaBoomEnchants;
import de.kaboomstudios.kaboomenchants.enchantment.CustomEnchantment;
import de.kaboomstudios.kaboomenchants.enchantment.EnchantmentCatalog;
import de.kaboomstudios.kaboomenchants.enchantment.RegisteredEnchantments;
import de.kaboomstudios.kaboomenchants.message.Messages;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * {@code /kbe give <player> <enchantment> [level] [book|item]}: gibt ein verzaubertes Buch oder
 * ein fertig verzaubertes Item.
 *
 * <p>Gedacht für Konsole, Operatoren und Belohnungs-Plugins, die Belohnungen als Konsolenbefehl
 * ausführen. Deshalb gilt er unabhängig davon, welche Bezugswege in der Konfiguration offen sind.
 * Die Stufe ist auf die Höchststufe der Verzauberung begrenzt.</p>
 */
public final class GiveCommand implements SubCommand {

    private static final String PLAYER = "player";
    private static final String ENCHANTMENT = "enchantment";
    private static final String LEVEL = "level";

    private final KaBoomEnchants plugin;
    private final Messages messages;

    public GiveCommand(final KaBoomEnchants plugin, final Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public String name() {
        return "give";
    }

    @Override
    public String permission() {
        return Permissions.GIVE;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal(name())
                .executes(this::usage)
                .then(Commands.argument(PLAYER, ArgumentTypes.player())
                        .executes(this::usage)
                        .then(Commands.argument(ENCHANTMENT, StringArgumentType.word())
                                .suggests(GiveCommand::suggestEnchantments)
                                .executes(context -> give(context, 1, Form.BOOK))
                                // Die Stufe ist bewusst nicht durch Brigadier begrenzt: Die Grenze hängt
                                // von der Verzauberung ab, und so kommt eine verständliche Meldung.
                                .then(Commands.argument(LEVEL, IntegerArgumentType.integer())
                                        .suggests(GiveCommand::suggestLevels)
                                        .executes(context -> give(context, level(context), Form.BOOK))
                                        .then(Commands.literal("book")
                                                .executes(context -> give(context, level(context), Form.BOOK)))
                                        .then(Commands.literal("item")
                                                .executes(context -> give(context, level(context), Form.ITEM))))));
    }

    private int usage(final CommandContext<CommandSourceStack> context) {
        messages.send(context.getSource().getSender(), "command.usage-give");
        return Command.SINGLE_SUCCESS;
    }

    private int give(final CommandContext<CommandSourceStack> context, final int level, final Form form)
            throws CommandSyntaxException {
        final CommandSender sender = context.getSource().getSender();
        final Player target = context.getArgument(PLAYER, PlayerSelectorArgumentResolver.class)
                .resolve(context.getSource()).getFirst();
        final String id = context.getArgument(ENCHANTMENT, String.class).toLowerCase(Locale.ROOT);

        final Optional<CustomEnchantment> custom = EnchantmentCatalog.byId(id);
        if (custom.isEmpty()) {
            messages.send(sender, "give.unknown-enchantment",
                    Placeholder.unparsed("id", id),
                    Placeholder.unparsed("ids", EnchantmentCatalog.ALL.stream()
                            .map(CustomEnchantment::id).collect(Collectors.joining(", "))));
            return 0;
        }
        final Enchantment enchantment = RegisteredEnchantments.of(custom.get());
        if (enchantment == null) {
            messages.send(sender, "give.not-registered", Placeholder.unparsed("id", id));
            return 0;
        }
        if (level < 1 || level > enchantment.getMaxLevel()) {
            messages.send(sender, "give.invalid-level",
                    Placeholder.unparsed("level", Integer.toString(level)),
                    Placeholder.component("enchantment", enchantment.description()),
                    Placeholder.unparsed("max", Integer.toString(enchantment.getMaxLevel())));
            return 0;
        }

        final ItemStack stack = form == Form.BOOK
                ? book(enchantment, level)
                : item(custom.get(), enchantment, level);
        if (stack == null) {
            messages.send(sender, "give.invalid-item",
                    Placeholder.unparsed("item", plugin.settings().of(custom.get()).giveItem()),
                    Placeholder.unparsed("id", id));
            return 0;
        }

        final boolean dropped = deliver(target, stack);
        final var itemName = Placeholder.component("item", stack.displayName());
        final var enchantmentName = Placeholder.component("enchantment", enchantment.displayName(level));
        final var playerName = Placeholder.component("player", target.name());
        messages.send(sender, dropped ? "give.given-dropped" : "give.given", itemName, enchantmentName, playerName);
        if (!target.equals(sender)) {
            messages.send(target, "give.received", itemName, enchantmentName);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static ItemStack book(final Enchantment enchantment, final int level) {
        final ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        book.editMeta(EnchantmentStorageMeta.class, meta -> meta.addStoredEnchant(enchantment, level, false));
        return book;
    }

    /** {@code null}, wenn das eingestellte Grund-Item fehlt oder die Verzauberung nicht tragen kann. */
    private ItemStack item(final CustomEnchantment custom, final Enchantment enchantment, final int level) {
        final Material material = Material.matchMaterial(plugin.settings().of(custom).giveItem());
        if (material == null || !material.isItem()) {
            return null;
        }
        final ItemStack item = new ItemStack(material);
        if (!enchantment.canEnchantItem(item)) {
            return null;
        }
        item.addEnchantment(enchantment, level);
        return item;
    }

    /**
     * Legt das Item ins Inventar. Passt es nicht hinein, fällt es wie beim Vanilla-Befehl
     * {@code /give} vor die Füße und kann nur vom Empfänger aufgehoben werden, damit eine
     * Belohnung bei vollem Inventar nicht verloren geht oder an andere fällt.
     *
     * @return ob etwas fallen gelassen werden musste
     */
    private static boolean deliver(final Player target, final ItemStack stack) {
        final Map<Integer, ItemStack> leftover = target.getInventory().addItem(stack.clone());
        for (final ItemStack rest : leftover.values()) {
            final Item drop = target.getWorld().dropItem(target.getLocation(), rest);
            drop.setOwner(target.getUniqueId());
            drop.setPickupDelay(0);
        }
        return !leftover.isEmpty();
    }

    private static int level(final CommandContext<CommandSourceStack> context) {
        return context.getArgument(LEVEL, Integer.class);
    }

    private static CompletableFuture<Suggestions> suggestEnchantments(final CommandContext<CommandSourceStack> context,
                                                                      final SuggestionsBuilder builder) {
        final String typed = builder.getRemainingLowerCase();
        for (final CustomEnchantment enchantment : EnchantmentCatalog.ALL) {
            if (enchantment.id().startsWith(typed)) {
                builder.suggest(enchantment.id());
            }
        }
        return builder.buildFuture();
    }

    /** Schlägt nur die Stufen vor, die es für die bereits eingegebene Verzauberung gibt. */
    private static CompletableFuture<Suggestions> suggestLevels(final CommandContext<CommandSourceStack> context,
                                                                final SuggestionsBuilder builder) {
        final String id = context.getArgument(ENCHANTMENT, String.class).toLowerCase(Locale.ROOT);
        EnchantmentCatalog.byId(id).ifPresent(enchantment -> {
            for (int level = 1; level <= enchantment.maxLevel(); level++) {
                builder.suggest(level);
            }
        });
        return builder.buildFuture();
    }

    private enum Form {
        BOOK,
        ITEM
    }
}
