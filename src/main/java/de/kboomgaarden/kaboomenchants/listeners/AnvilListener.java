package de.kboomgaarden.kaboomenchants.listeners;

import de.kboomgaarden.kaboomenchants.KaBoomPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class AnvilListener implements Listener {

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        // Hier prüfen wir Slot 0 und Slot 1 und erschaffen das Ergebnis in Slot 2.

        // GET: Gib mir das Invetar von genau diesem Amboss.
        AnvilInventory inventory = event.getInventory();

        // GET: Wir lesen die Items aus dem linken (0) und mittleren (1) Fach aus.
        ItemStack leftItem = inventory.getItem(0);
        ItemStack rightItem = inventory.getItem(1);

        // Sicherheitscheck: Wenn auch nur eins der beiden Fächer leer ist.
        // return bedeutet: Wir brechen diese Methode ab:
        if (leftItem == null || rightItem == null) {
            return;
        }
        // Metadaten der Items links (0) und rechts(1) auslesen.
        ItemMeta leftMeta = leftItem.getItemMeta();
        ItemMeta rightMeta = rightItem.getItemMeta();

        if (rightMeta == null) return;

        // Unser bekannter Schlüssel
        NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(KaBoomPlugin.class), "bohrer_level");

        // Level des linken Items abfragen (Standardwert 0, falls nicht vorhanden
        int leftLevel = 0;
        if (leftMeta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
            leftLevel = leftMeta.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 1);
        }

        // Level des rechten Items abfragen (Standardwert 0 falls nicht vorhanden)
        int rightLevel = 0;
        if (rightMeta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
            rightLevel = rightMeta.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 1);
        }
        // Abbruch wenn keines der Items die Bohrer Verzauberung hat
        if (leftLevel == 0 && rightLevel == 0) return;

        // 1. Check: Hat das rechte Item das unsichtbare Etikett?
        if (rightMeta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {

            // 2. Check: ist das linke (0) Item ein gültiges Item
            String leftName = leftItem.getType().name();
            boolean isTool = leftName.endsWith("_PICKAXE") || leftName.endsWith("_SHOVEL");
            boolean isBook = leftName.equals("ENCHANTED_BOOK");
            // Abbruch wenn das linke Item kein gültiges Werkzeug noch ein Zuaberbuch ist.
            if (!isTool && !isBook) return;
            // Neues Level nach Vanilla regeln berechnen
            int finalLevel;
            if (leftLevel > 0 && leftLevel == rightLevel) {
                finalLevel = leftLevel + 1; // gleiche Stufen addieren sich
            } else {
                finalLevel = Math.max(leftLevel, rightLevel); // Ansonsten gewinnt die hähere Stufe
            }
            // Maximale Stufe begrenzen
            if (finalLevel > 3) {
                finalLevel = 3;
            }
            // Basis für das Ergebniss Item ermitteln
            // Prüfen ob Minecraft bereits ein Ergebnis (z.B. durch Umbenennung oder Reparatur) berechnet hat
            ItemStack resultItem = event.getResult();
            if (resultItem == null || resultItem.getType().isAir()){
                resultItem = leftItem.clone();
            }

            ItemMeta resultMeta = leftItem.getItemMeta();

            if (resultMeta != null) {
                // PDC-Etikett mit neuem Level speichern
                resultMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, finalLevel);
                // Lore aktualisieren und alte Einträge bereinigen
                List<String> lore = resultMeta.hasLore() ? resultMeta.getLore() : new ArrayList<>();
                // Liste durchsuchen und jede Zeile läschen die das Wort "Bohrer" enthält,
                // bevor die neue Stufe aufegeschrieben wird.
                lore.removeIf(line -> line.contains("Bohrer"));

                lore.add("§7Bohrer " + finalLevel);
                resultMeta.setLore(lore);

                resultItem.setItemMeta(resultMeta);
            }
            // Modifiziertes Werkzeug in das Ergebnisfach (2) legen.
            event.setResult(resultItem);
            // Ansichtfenster des Spielers holen und XP-Preis setzten.
            AnvilView anvilView = (AnvilView) event.getView();
            anvilView.setRepairCost(finalLevel * 5);

        }
    }
}
