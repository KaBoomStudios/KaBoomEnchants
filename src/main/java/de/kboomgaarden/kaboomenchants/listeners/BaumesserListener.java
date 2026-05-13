package de.kboomgaarden.kaboomenchants.listeners;

import de.kboomgaarden.kaboomenchants.KaBoomPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class BaumesserListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();

        // Event Abbrechen, falls es durch den Weltschutz blockiert wird
        if (event.isCancelled()) return;

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        String itemName = item.getType().name();

        // Prüfen ob es eine Axt ist.
        if (!itemName.endsWith("_AXE")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            //player.sendMessage("§cDebug: Die Axt hat keine Meta-Daten");
            return;
        }

        NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(KaBoomPlugin.class), "baumesser");

        // Prüfung auf das Baumesser Etikett
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
            player.sendMessage("§cDebug: Die Axt hat kein unsichtbares Etikett");
            return;
        }

        Block startBlock = event.getBlock();
        String blockName = startBlock.getType().name();

        // Prüfung auf Stammholz
        boolean isLog = blockName.endsWith("_LOG") || blockName.endsWith("_STEM");
        if (!isLog) {
            //player.sendMessage("§cDebug: Der Block ist kein gültiges Holz. Block-name: " + blockName);
            return;
        }
        //Extrahiert den reinen Präfix (z.B. "SPRUCE" aus "SPRUCE_LOG"
        String baseTreeName = blockName.split("_")[0];

        //player.sendMessage("§aDebug: Alle prüfungen bestanden! Starte den Abbau...");

        // Den reinen Baumnamen extrahieren (z.b. SPRUCE aus SPRUCELOG)
        //String baseTreeName = blockName.replace("_LOG", "").replace("_WOOD", "").replace("_STEM", "");

        // Kugelsichere speicherung über Koordinaten-Strings.
        Set<String> searchedCoords = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        List<Block> blocksToBreak = new ArrayList<>();

        String startKey = startBlock.getX() + "," + startBlock.getY() + "," + startBlock.getZ();
        searchedCoords.add(startKey);
        queue.add(startBlock);
        blocksToBreak.add(startBlock);

        // Limit zur vermeidung von Server-Abstürzen bei sehr großen Bäumen
        int maxBlocks = 1500;

        // Breitensuche: Überprüft benachbarte Blöcke
        while (!queue.isEmpty() && blocksToBreak.size() < maxBlocks) {
            Block current = queue.poll();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z < 1; z++) {
                        Block neighbor = current.getRelative(x, y, z);
                        String neighborKey = neighbor.getX() + "," + neighbor.getY() + "," + neighbor.getZ();

                        // Wenn die Koordinate schon im Text-Set steht, sofort überspringen
                        if (searchedCoords.contains(neighborKey)) continue;
                        searchedCoords.add(neighborKey);

                        String neighborName = neighbor.getType().name();
                        // Prüfen ob der nachbar zum selben Baum gehört (Log, Wood oder Stem)
                        boolean isSameTree = neighborName.startsWith(baseTreeName) && (neighborName.endsWith("_LOG") || neighborName.endsWith("_WOOD") || neighborName.endsWith("_STEM"));

                        // Prüfen der mathematisch eindeutigen Location
                        if (isSameTree) {
                            blocksToBreak.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        player.sendMessage("§aDebug: gefundene Holzblöcke " + blocksToBreak.size());
        // Abbau der gefundenen Blöcke
        for (Block b : blocksToBreak) {
            if (!b.equals(startBlock)){
                b.breakNaturally();
            }
        }
    }
}
