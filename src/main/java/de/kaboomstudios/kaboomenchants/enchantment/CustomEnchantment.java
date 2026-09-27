package de.kaboomstudios.kaboomenchants.enchantment;

import de.kaboomstudios.kaboomenchants.KaBoomEnchants;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry.EnchantmentCost;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;

import java.util.List;

/**
 * Beschreibung einer eigenen Verzauberung: alles, was die Registry beim Serverstart braucht.
 *
 * <p>Eine neue Verzauberung ist eine neue Klasse mit dieser Schnittstelle, ein Eintrag in
 * {@link EnchantmentCatalog} und ein Block in der config.yml. Die Registrierung behandelt
 * alle Verzauberungen gleich und muss dafür nicht geändert werden.</p>
 */
@SuppressWarnings("UnstableApiUsage")
public interface CustomEnchantment {

    /** Namespace aller Verzauberungen dieses Plugins, damit sie nie mit Vanilla kollidieren. */
    String NAMESPACE = "kaboomenchants";

    /**
     * Feste ID, zugleich Name des Config-Blocks und Teil des Schlüssels auf jedem Item.
     * Darf nach der Veröffentlichung nie geändert werden, sonst verlieren Items die Verzauberung.
     */
    String id();

    int maxLevel();

    /** Item-Tags, deren Items die Verzauberung tragen dürfen. */
    List<TagKey<ItemType>> supportedItemTags();

    /** Einzelne Items zusätzlich zu den Tags, für Items ohne passenden Vanilla-Tag. */
    default List<TypedKey<ItemType>> supportedItemTypes() {
        return List.of();
    }

    /** Wo das Item liegen muss, damit die Verzauberung zählt. */
    EquipmentSlotGroup activeSlots();

    /** Kosten am Verzauberungstisch; bestimmen, ab welcher Tischstufe sie erscheinen kann. */
    EnchantmentCost minimumCost();

    EnchantmentCost maximumCost();

    /** Grundpreis am Amboss, wird dort mit der Stufe multipliziert. */
    int anvilCost();

    /**
     * Ob der Verzauberungstisch sie überhaupt anbieten kann. Der Tisch verzaubert nur Items,
     * die in Vanilla verzauberbar sind; ein Totem gehört nicht dazu.
     */
    default boolean enchantingTableApplicable() {
        return true;
    }

    /**
     * Listener, die die Wirkung umsetzen. Werden einmal beim Aktivieren des Plugins angemeldet;
     * ob die Verzauberung eingeschaltet ist, prüfen sie selbst bei jedem Ereignis.
     *
     * @param registered die Verzauberung, wie der Server sie beim Start registriert hat
     */
    default List<Listener> createListeners(final KaBoomEnchants plugin, final Enchantment registered) {
        return List.of();
    }

    default Key key() {
        return Key.key(NAMESPACE, id());
    }

    default TypedKey<Enchantment> typedKey() {
        return EnchantmentKeys.create(key());
    }
}
