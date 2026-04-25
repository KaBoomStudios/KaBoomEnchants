package de.kboomgaarden.kaboomenchants.listeners;


import de.kboomgaarden.kaboomenchants.KaBoomPlugin;
import de.kboomgaarden.kaboomenchants.enchantments.BohrerEnchantment;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;


public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        // wir brauchen die Meta (den Beipackzettel), um das Etikett zu lesen
        ItemMeta meta = itemInHand.getItemMeta();
        // Wenn das Item keine meta hat (zb. leere Hand), brechen wir hier ab
        if (meta == null) return;
        // wir erstellen den gleichen Schlüssel wie im Befehl
        // Hinweis: Wir brauchen hier zugriff auf die Hauptklasse für den Key!
        // Da wir im Listener sind können wir das Plugin einfach holen
        NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(KaBoomPlugin.class), "bohrer_level");

        // Die eigentliche kontrolle: Hat das Item unser Etikett
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)){
            // Nur wenn das Etikett da ist darf der Bohrer loslegen
            BohrerEnchantment.anwenden(event);
        }
    }

}
