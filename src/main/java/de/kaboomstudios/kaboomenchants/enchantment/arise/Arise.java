package de.kaboomstudios.kaboomenchants.enchantment.arise;

import de.kaboomstudios.kaboomenchants.enchantment.CustomEnchantment;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry.EnchantmentCost;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;

import java.util.List;

/**
 * Arise: ein Totem der Unsterblichkeit, das mehrfach rettet. Stufe I bis III stehen für
 * eine, zwei oder drei Rettungen über die normale hinaus.
 */
@SuppressWarnings("UnstableApiUsage")
public final class Arise implements CustomEnchantment {

    @Override
    public String id() {
        return "arise";
    }

    @Override
    public int maxLevel() {
        return 3;
    }

    @Override
    public List<TagKey<ItemType>> supportedItemTags() {
        return List.of();
    }

    @Override
    public List<TypedKey<ItemType>> supportedItemTypes() {
        return List.of(ItemTypeKeys.TOTEM_OF_UNDYING);
    }

    // Das Totem rettet aus beiden Händen, also muss die Verzauberung auch in beiden gelten.
    @Override
    public EquipmentSlotGroup activeSlots() {
        return EquipmentSlotGroup.HAND;
    }

    @Override
    public EnchantmentCost minimumCost() {
        return EnchantmentCost.of(25, 10);
    }

    @Override
    public EnchantmentCost maximumCost() {
        return EnchantmentCost.of(75, 10);
    }

    @Override
    public int anvilCost() {
        return 8;
    }

    @Override
    public boolean enchantingTableApplicable() {
        return false;
    }
}
