package de.kaboomstudios.kaboomenchants.enchantment.tunnelbore;

import de.kaboomstudios.kaboomenchants.KaBoomEnchants;
import de.kaboomstudios.kaboomenchants.enchantment.CustomEnchantment;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry.EnchantmentCost;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;

import java.util.List;

/** Tunnel Bore: baut beim Abbau einen Bereich um den Block ab, Stufe I bis III. */
@SuppressWarnings("UnstableApiUsage")
public final class TunnelBore implements CustomEnchantment {

    @Override
    public String id() {
        return "tunnel_bore";
    }

    @Override
    public int maxLevel() {
        return 3;
    }

    @Override
    public List<TagKey<ItemType>> supportedItemTags() {
        return List.of(ItemTypeTagKeys.PICKAXES, ItemTypeTagKeys.SHOVELS);
    }

    @Override
    public EquipmentSlotGroup activeSlots() {
        return EquipmentSlotGroup.MAINHAND;
    }

    // Stufe I erscheint ab einem gut ausgebauten Tisch, Stufe III praktisch nur bei vollem Tisch.
    @Override
    public EnchantmentCost minimumCost() {
        return EnchantmentCost.of(15, 9);
    }

    @Override
    public EnchantmentCost maximumCost() {
        return EnchantmentCost.of(65, 9);
    }

    @Override
    public int anvilCost() {
        return 4;
    }

    @Override
    public List<Listener> createListeners(final KaBoomEnchants plugin, final Enchantment registered) {
        return List.of(new TunnelBoreListener(plugin, this, registered, plugin.blockBreaker()));
    }
}
