package de.kaboomstudios.kaboomenchants.enchantment.tunnelbore;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Der Bereich, den Tunnel Bore um einen abgebauten Block herum abbaut.
 *
 * <p>Quer zur Blickrichtung reicht er {@code level} Blöcke nach jeder Seite (Stufe I 3×3,
 * II 5×5, III 7×7). In Blickrichtung geht er {@code 2 × level} Blöcke über den getroffenen
 * Block hinaus in die Wand, den Boden oder die Decke, also 3, 5 oder 7 Schichten tief.</p>
 */
final class TunnelBoreArea {

    /** Ab dieser Neigung des Kopfes gilt der Blick als nach unten beziehungsweise oben. */
    private static final float STEEP_PITCH = 45f;

    private TunnelBoreArea() {
    }

    /** Alle Blöcke des Bereichs außer dem Mittelblock, nach Abstand zu ihm sortiert. */
    static List<Block> around(final Block center, final Player player, final int level) {
        final int radius = level;
        final int depth = level * 2;
        final int x = center.getX();
        final int y = center.getY();
        final int z = center.getZ();

        int minX = x - radius;
        int maxX = x + radius;
        int minY = y - radius;
        int maxY = y + radius;
        int minZ = z - radius;
        int maxZ = z + radius;

        // Die Neigung entscheidet zuerst: Steiler Blick nach unten oder oben bohrt senkrecht.
        // Sonst bestimmt die Himmelsrichtung, in welche Richtung der Bereich in die Tiefe geht.
        final float pitch = player.getLocation().getPitch();
        if (pitch > STEEP_PITCH) {
            minY = y - depth;
            maxY = y;
        } else if (pitch < -STEEP_PITCH) {
            minY = y;
            maxY = y + depth;
        } else {
            final BlockFace facing = player.getFacing();
            switch (facing) {
                case NORTH -> {
                    minZ = z - depth;
                    maxZ = z;
                }
                case SOUTH -> {
                    minZ = z;
                    maxZ = z + depth;
                }
                case WEST -> {
                    minX = x - depth;
                    maxX = x;
                }
                case EAST -> {
                    minX = x;
                    maxX = x + depth;
                }
                default -> {
                    // getFacing liefert nur die vier Himmelsrichtungen; nichts zu tun.
                }
            }
        }

        final List<Block> blocks = new ArrayList<>();
        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    if (bx == x && by == y && bz == z) {
                        continue;
                    }
                    blocks.add(center.getWorld().getBlockAt(bx, by, bz));
                }
            }
        }
        blocks.sort(Comparator.comparingDouble(block -> distanceSquared(block, center)));
        return blocks;
    }

    private static double distanceSquared(final Block block, final Block center) {
        final int dx = block.getX() - center.getX();
        final int dy = block.getY() - center.getY();
        final int dz = block.getZ() - center.getZ();
        return dx * dx + dy * dy + dz * dz;
    }
}
