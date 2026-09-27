package de.kaboomstudios.kaboomenchants.registration;

import de.kaboomstudios.kaboomenchants.config.EnchantmentSettings;
import de.kaboomstudios.kaboomenchants.config.PluginSettings;
import de.kaboomstudios.kaboomenchants.config.Sources;
import de.kaboomstudios.kaboomenchants.enchantment.CustomEnchantment;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import io.papermc.paper.tag.TagEntry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Trägt die Verzauberungen beim Serverstart in die Registry und in die Vanilla-Tags ein.
 *
 * <p>Die Bezugswege laufen über dieselben Tags, die auch Vanilla benutzt: Der Verzauberungstisch
 * liest {@code #in_enchanting_table}, Bibliothekare würfeln aus {@code #tradeable}, Truhen-Loot
 * aus {@code #on_random_loot}. Wichtig: Alle drei enthalten {@code #non_treasure}. Eine
 * Verzauberung dort einzutragen würde alle drei Wege auf einmal öffnen, deshalb wird jeder
 * Tag einzeln befüllt.</p>
 */
@SuppressWarnings("UnstableApiUsage")
public final class EnchantmentRegistration {

    private final List<CustomEnchantment> enchantments;
    private final PluginSettings settings;
    private final Map<String, Component> names;

    public EnchantmentRegistration(final List<CustomEnchantment> enchantments, final PluginSettings settings,
                                   final Map<String, Component> names) {
        this.enchantments = enchantments;
        this.settings = settings;
        this.names = names;
    }

    public void register(final LifecycleEventManager<BootstrapContext> manager) {
        registerItemTags(manager);
        registerEnchantments(manager);
        registerEnchantmentTags(manager);
    }

    /**
     * Ein eigener Item-Tag je Verzauberung fasst die unterstützten Items zusammen. Die Registry
     * nimmt nur eine Menge an; so lassen sich mehrere Vanilla-Tags (Spitzhacken und Schaufeln)
     * verbinden, und neue Werkzeuge späterer Versionen kommen über die Vanilla-Tags automatisch dazu.
     */
    private void registerItemTags(final LifecycleEventManager<BootstrapContext> manager) {
        manager.registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ITEM), event -> {
            for (final CustomEnchantment enchantment : enchantments) {
                final List<TagEntry<ItemType>> entries = new ArrayList<>();
                enchantment.supportedItemTags().forEach(tag -> entries.add(TagEntry.tagEntry(tag)));
                enchantment.supportedItemTypes().forEach(type -> entries.add(TagEntry.valueEntry(type)));
                event.registrar().setTag(supportedItemsTag(enchantment), entries);
            }
        });
    }

    private void registerEnchantments(final LifecycleEventManager<BootstrapContext> manager) {
        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.compose(), event -> {
            for (final CustomEnchantment enchantment : enchantments) {
                final EnchantmentSettings config = settings.of(enchantment);
                event.registry().register(enchantment.typedKey(), builder -> builder
                        .description(names.get(enchantment.id()))
                        .supportedItems(event.getOrCreateTag(supportedItemsTag(enchantment)))
                        .maxLevel(enchantment.maxLevel())
                        .weight(config.weight())
                        .minimumCost(enchantment.minimumCost())
                        .maximumCost(enchantment.maximumCost())
                        .anvilCost(enchantment.anvilCost())
                        .activeSlots(enchantment.activeSlots())
                        .exclusiveWith(exclusiveWith(config)));
            }
        });
    }

    /**
     * Mending wird über die Vanilla-Exklusivität ausgeschlossen. Sie gilt in beide Richtungen,
     * also weder Mending auf ein Item mit dieser Verzauberung noch umgekehrt.
     */
    private static RegistryKeySet<Enchantment> exclusiveWith(final EnchantmentSettings config) {
        final List<TypedKey<Enchantment>> exclusive = config.mendingAllowed()
                ? List.of()
                : List.of(EnchantmentKeys.MENDING);
        return RegistrySet.keySet(RegistryKey.ENCHANTMENT, exclusive);
    }

    /**
     * Nach dem Auflösen verschachtelter Tags eingetragen, damit nur genau die gewünschten Tags
     * die Verzauberung enthalten. Deshalb steht sie auch ausdrücklich in
     * {@code #double_trade_price}: Dieser Tag verweist in Vanilla auf {@code #treasure}, ist zu
     * diesem Zeitpunkt aber schon aufgelöst.
     */
    private void registerEnchantmentTags(final LifecycleEventManager<BootstrapContext> manager) {
        manager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT), event -> {
            final PostFlattenTagRegistrar<Enchantment> registrar = event.registrar();
            addWhere(registrar, EnchantmentTagKeys.IN_ENCHANTING_TABLE, Sources::enchantingTable);
            addWhere(registrar, EnchantmentTagKeys.TRADEABLE, Sources::trading);
            addWhere(registrar, EnchantmentTagKeys.ON_RANDOM_LOOT, Sources::loot);
            // Wie Mending: nicht vom Tisch, also Schatz, und beim Handel zum doppelten Preis.
            addWhere(registrar, EnchantmentTagKeys.TREASURE, sources -> !sources.enchantingTable());
            addWhere(registrar, EnchantmentTagKeys.DOUBLE_TRADE_PRICE, sources -> !sources.enchantingTable());
        });
    }

    private void addWhere(final PostFlattenTagRegistrar<Enchantment> registrar, final TagKey<Enchantment> tag,
                          final Predicate<Sources> condition) {
        final Collection<TypedKey<Enchantment>> keys = enchantments.stream()
                .filter(enchantment -> condition.test(settings.of(enchantment).sources()))
                .map(CustomEnchantment::typedKey)
                .toList();
        if (!keys.isEmpty()) {
            registrar.addToTag(tag, keys);
        }
    }

    private static TagKey<ItemType> supportedItemsTag(final CustomEnchantment enchantment) {
        return ItemTypeTagKeys.create(Key.key(CustomEnchantment.NAMESPACE, "enchantable/" + enchantment.id()));
    }
}
