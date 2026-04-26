package de.kboomgaarden.kaboomenchants.listeners;

import de.kboomgaarden.kaboomenchants.KaBoomPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class EnchantmentTableListener implements Listener {

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        String itemName = item.getType().name();

        // Prüfen, ob das Item eine Spitzhacke oder eine Schaufel ist.
        boolean isTool = itemName.endsWith("_PICKAXE") || itemName.endsWith("_SHOVEL");
        if (!isTool) return;

        // Prüfen, ob die Verzauberung mindestens 30 Level kostet (höchste Stufe)
        if (event.whichButton() != 2) return;

        // Zufallsgenerator initialisieren (erzeugt eine Zahl zwischen 0 und 100)
        Random random = new Random();
        int chance = random.nextInt(100);
        int level = 0;

        // 3% Chance auf Stufe 2 (Zahlen 0, 1, 2)
        if (chance < 3) {
            level = 2;
        }
        // 7% Chance auf Stufe 1 (Zahlen 3 - 9)
        else if (chance < 10) { // Standardwert 10
            level = 1;
        }
        // Abbruch wenn nichts davon zutrifft
        if (level == 0) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(KaBoomPlugin.class), "bohrer_level");

        // PDC-Etikett mit der ausgewähten Stufe hinzufügen
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);

        // Lore aktualisieren und die Verzauberung optisch hinzufügen.
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        lore.add("§7Bohrer " + level);
        meta.setLore(lore);

        item.setItemMeta(meta);

    }
}
