package de.kboomgaarden.kaboomenchants.listeners;


import de.kboomgaarden.kaboomenchants.enchantments.BohrerEnchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;


public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        String itemName = itemInHand.getType().name();

        // Der Türsteher prüft das Werkzeug
        if (itemName.endsWith("_PICKAXE") || itemName.endsWith("_SHOVEL")){
            // ab hier übernimmt die Bohrerklasse die Logik
            BohrerEnchantment.anwenden(event);
        }
    }

}
