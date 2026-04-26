package de.kboomgaarden.kaboomenchants.listeners;

import de.kboomgaarden.kaboomenchants.KaBoomPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VillagerTradeListener implements Listener {

    @EventHandler
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event){
        // Prüfen ob es sich um einen Dorfbewohner handelt
        if (!(event.getEntity() instanceof Villager)) return;
        Villager villager = (Villager) event.getEntity();

        // Prüfen ob der Dorfbewohner den Beruf Bibliothekar hat.
        if (villager.getProfession() != Villager.Profession.LIBRARIAN) return;

        // Initialisierung des Zufallsgenerators
        Random random = new Random();
        int chance = random.nextInt(100);

        int level = 0;
        int basePrice = 0;

        // Definition der Wahrscheinlichkeiten und Basispreise
        if (chance < 1){
            level = 3;      // 1% Chance auf Stufe 3
            basePrice = 40;
        } else if (chance < 2) {
            level = 2;      // 1% Chance auf Stufe 2
            basePrice = 30;
        } else if (chance < 5) {
            level = 1;      // 3% Chance auf Stufe 1
            basePrice = 20;
        }
        // Abbruch, falls keine Bohrerverzauberung ausgewählt wurde
        if (level == 0) return;

        // Erstellung des Verzauberten Buches
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        if (meta != null) {
            NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(KaBoomPlugin.class), "bohrer_level");

            // Speichern der Stufe im PersistentDataContainer
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);
            // Optische gestaltung der Lore
            List<String> lore = new ArrayList<>();
            lore.add("§7Bohrer " + level);
            meta.setLore(lore);

            book.setItemMeta(meta);
        }
        // Berechnung eines leicht variirenden Preises (Basispreis +/- 5 Smaragde)
        int finalPrice = basePrice + random.nextInt(11) - 5;
        if (finalPrice < 10) finalPrice = 5;

        // Erstellung des Händler-Rezepts (Smaragde + Buch = Bohrer Buch
        // 12 Nutzungen sind der Standardwert für Vanilla Bücher
        MerchantRecipe recipe = new MerchantRecipe(book, 12);
        recipe.addIngredient(new ItemStack(Material.EMERALD, finalPrice));
        recipe.addIngredient(new ItemStack(Material.BOOK, 1));

        // Das neue Angebot dem Dorfbewohner hinzufügen
        event.setRecipe(recipe);
    }
}
