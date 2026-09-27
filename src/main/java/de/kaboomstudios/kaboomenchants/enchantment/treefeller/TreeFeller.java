package de.kaboomstudios.kaboomenchants.enchantment.treefeller;

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

/** Tree Feller: fällt einen natürlichen Baum auf einmal, eine Stufe. */
@SuppressWarnings("UnstableApiUsage")
public final class TreeFeller implements CustomEnchantment {

    @Override
    public String id() {
        return "tree_feller";
    }

    @Override
    public int maxLevel() {
        return 1;
    }

    @Override
    public List<TagKey<ItemType>> supportedItemTags() {
        return List.of(ItemTypeTagKeys.AXES);
    }

    @Override
    public EquipmentSlotGroup activeSlots() {
        return EquipmentSlotGroup.MAINHAND;
    }

    @Override
    public EnchantmentCost minimumCost() {
        return EnchantmentCost.of(20, 0);
    }

    @Override
    public EnchantmentCost maximumCost() {
        return EnchantmentCost.of(70, 0);
    }

    @Override
    public int anvilCost() {
        return 4;
    }

    @Override
    public List<Listener> createListeners(final KaBoomEnchants plugin, final Enchantment registered) {
        return List.of(new TreeFellerListener(plugin, this, registered, plugin.blockBreaker()));
    }
}
