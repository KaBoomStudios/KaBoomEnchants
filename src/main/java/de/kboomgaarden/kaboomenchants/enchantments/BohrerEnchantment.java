package de.kboomgaarden.kaboomenchants.enchantments;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class BohrerEnchantment {

    // diese Methode wird später von unserem Listener (Manager) aufgerufen
    public static void anwenden(BlockBreakEvent event){
        // 1. Wir holen uns den Spieler
        Player player = event.getPlayer();
        // 2. Wir erfassen den abgebauten Block
        Block centerBlock = event.getBlock();
            // wir speichern die exakten Koordinaten des abgebauten Blocks
            int centerX = centerBlock.getX();
            int centerY = centerBlock.getY();
            int centerZ = centerBlock.getZ();

            // Start- und Endpunkte (Standard: flach 3x3)
            int startX = centerX - 1;
            int endX = centerX + 1;
            int startY = centerY - 1;
            int endY = centerY + 1;
            int startZ = centerZ - 1;
            int endZ = centerZ + 1;

            // Wir lesen die Blickrichtung des Spielers aus
            // getPitch() gibt die Neigung des Kopfes in Grad (-90 bis 90)
            float pitch = player.getLocation().getPitch();
            // getFacing gibt die Himmelsrichtung (North, South, East, West)
            BlockFace facing = player.getFacing();

            // jetzt schieben wir den Bohrer in die Tiefe, je nachdem wo du hinschaust
            // Stufe 1: 3 Blöcke tief
            if (pitch > 45){    // Spieler guckt stark nach unten, Bohrer geht tief in den Boden
                startY = centerY - 2;
                endY = centerY;
            } else if (pitch < -45) {   // Spieler guckt stark nach oben, Bohrer geht tief in die decke
                startY = centerY;
                endY = centerY + 2;
            } else {
                // Spieler guckt gerade aus. Wir prüfen die Himmelsrichtung
                if (facing == BlockFace.NORTH){
                    // Norden ist bei Minecraft auf der Z-Achse im Minus Bereich
                    startZ = centerZ - 2;
                    endZ = centerZ;
                } else if (facing == BlockFace.SOUTH) {
                    // Süden ist auf der Z-Achse im Plus-bereich
                    startZ = centerZ;
                    endZ = centerZ + 2;
                } else if (facing == BlockFace.WEST) {
                    // Westen ist auf der X-Achse im Minus-Bereich
                    startX = centerX - 2;
                    endX = centerX;
                } else if (facing == BlockFace.EAST) {
                    // Osten ist auf der X-Achse im Plus-Bereich
                    startX = centerX;
                    endX = centerX + 2;
                }
            }
            // Angepasste Schleife. Wir nutzen die dynamischen Start- und Enwerte die wir oben berechnet haben
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    for (int z = startZ; z <= endZ; z++) {

                        // Wir holen uns den Block an der aktuellen Position der Schleife
                        Block currentBlock = centerBlock.getWorld().getBlockAt(x, y, z);

                        // Schutzmaßnahme. Wir können kein Bedrock abbauen
                        if (currentBlock.getType() != Material.BEDROCK) {
                            // breakNaturally() baut den block ab und dropt die Itemsm
                            currentBlock.breakNaturally();
                        }
                    }
                }
            }
        }
    }

