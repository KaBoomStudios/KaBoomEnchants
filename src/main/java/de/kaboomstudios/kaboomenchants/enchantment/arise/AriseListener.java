package de.kaboomstudios.kaboomenchants.enchantment.arise;

import de.kaboomstudios.kaboomenchants.KaBoomEnchants;
import de.kaboomstudios.kaboomenchants.message.Messages;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Gibt nach einer Rettung durch ein Totem mit Arise ein Totem mit einer Stufe weniger zurück.
 *
 * <p>Das Totem selbst verbraucht das Spiel wie immer. Zurück kommt eine Kopie mit einer Stufe
 * weniger, aus Stufe I ein Totem ohne Arise. So zeigt der Tooltip jederzeit, wie viele
 * Rettungen übrig sind, und ein gewöhnliches Totem rettet am Ende genau einmal wie in Vanilla.</p>
 *
 * <p>Die Stufe wird im Ereignis gelesen, zurückgegeben wird einen Tick später: Erst dann hat das
 * Spiel das Totem sicher aus der Hand genommen, und die neue Kopie landet nicht im selben Zug
 * wieder im Verbrauch.</p>
 */
final class AriseListener implements Listener {

    private final KaBoomEnchants plugin;
    private final Arise arise;
    private final Enchantment enchantment;
    private final Messages messages;

    AriseListener(final KaBoomEnchants plugin, final Arise arise, final Enchantment enchantment, final Messages messages) {
        this.plugin = plugin;
        this.arise = arise;
        this.enchantment = enchantment;
        this.messages = messages;
    }

    /** Nur nach einer tatsächlichen Rettung: ohne Totem kommt das Ereignis abgebrochen an. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResurrect(final EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof final Player player) || event.getHand() == null) {
            return;
        }
        if (!plugin.startupSettings().of(arise).enabled()) {
            return;
        }
        final EquipmentSlot hand = event.getHand();
        final ItemStack totem = player.getInventory().getItem(hand);
        final int level = totem.getEnchantmentLevel(enchantment);
        if (level <= 0) {
            return;
        }

        final ItemStack replacement = totem.clone();
        replacement.setAmount(1);
        replacement.removeEnchantment(enchantment);
        if (level > 1) {
            replacement.addUnsafeEnchantment(enchantment, level - 1);
        }

        player.getScheduler().run(plugin, task -> giveBack(player, hand, replacement, level), null);
    }

    /**
     * Legt das Totem in die Hand zurück, aus der es kam. Ist die inzwischen belegt, ins Inventar,
     * und ist auch das voll, vor die Füße, aufhebbar nur für den Spieler selbst.
     *
     * @param savesLeft so oft rettet das zurückgegebene Totem noch
     */
    private void giveBack(final Player player, final EquipmentSlot hand, final ItemStack replacement, final int savesLeft) {
        if (player.getInventory().getItem(hand).isEmpty()) {
            player.getInventory().setItem(hand, replacement);
        } else {
            final Map<Integer, ItemStack> leftover = player.getInventory().addItem(replacement);
            for (final ItemStack rest : leftover.values()) {
                final Item drop = player.getWorld().dropItem(player.getLocation(), rest);
                drop.setOwner(player.getUniqueId());
                drop.setPickupDelay(0);
            }
        }
        player.sendActionBar(messages.get("arise.saves-left",
                Placeholder.unparsed("count", Integer.toString(savesLeft))));
    }
}
