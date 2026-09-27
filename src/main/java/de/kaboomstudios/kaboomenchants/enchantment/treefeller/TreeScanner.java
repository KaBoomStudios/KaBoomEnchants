package de.kaboomstudios.kaboomenchants.enchantment.treefeller;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

/**
 * Findet die Stämme eines Baums per Breitensuche und prüft, ob er natürlich gewachsen ist.
 *
 * <p>Die Suche geht wie im früheren Stand über alle 26 Nachbarn, damit auch schräg versetzte
 * Äste und 2×2-Stämme vollständig erfasst werden. Sie bleibt bei einer Holzart: Eine Eiche neben
 * einer Birke wird nicht mitgefällt.</p>
 *
 * <p>Natürlich heißt: Am Stamm hängen genug Blätter, die das Spiel selbst gesetzt hat. Blätter,
 * die ein Spieler platziert, sind in Vanilla immer „persistent“ und zerfallen nie; natürliche
 * nicht. Ein Holzhaus hat keine oder nur solche gesetzten Blätter und wird deshalb nicht gefällt.
 * Nether-Bäume haben statt Blättern Warzenblöcke und Schroomlights. Die haben keine solche
 * Markierung, dort zählt nur ihre Anzahl.</p>
 */
final class TreeScanner {

    private TreeScanner() {
    }

    /** Ergebnis der Suche. {@code logs} enthält den Startblock nicht, nächstgelegene zuerst. */
    record Tree(List<Block> logs, int naturalLeaves) {
    }

    /** {@code null}, wenn der Block kein Stamm ist. */
    static Tree scan(final Block start, final int maxBlocks, final int enoughLeaves) {
        final String species = species(start.getType());
        if (species == null) {
            return null;
        }

        final Set<Block> seen = new HashSet<>();
        final Set<Block> leaves = new HashSet<>();
        final Queue<Block> queue = new ArrayDeque<>();
        final List<Block> logs = new ArrayList<>();
        seen.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            final Block current = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        final Block neighbor = current.getRelative(dx, dy, dz);
                        if (!seen.add(neighbor)) {
                            continue;
                        }
                        if (species.equals(species(neighbor.getType()))) {
                            if (logs.size() < maxBlocks) {
                                logs.add(neighbor);
                                queue.add(neighbor);
                            }
                        } else if (leaves.size() < enoughLeaves && isNaturalLeaf(neighbor)) {
                            leaves.add(neighbor);
                        }
                    }
                }
            }
        }
        return new Tree(logs, leaves.size());
    }

    /**
     * Die Holzart eines Stammblocks, etwa {@code OAK} für Eichenstamm, entrindeten Eichenstamm
     * und Eichenholz. {@code null} für alles, was kein Stamm ist.
     */
    static String species(final Material material) {
        if (!Tag.LOGS.isTagged(material)) {
            return null;
        }
        String name = material.name().toUpperCase(Locale.ROOT);
        if (name.startsWith("STRIPPED_")) {
            name = name.substring("STRIPPED_".length());
        }
        for (final String suffix : List.of("_LOG", "_WOOD", "_STEM", "_HYPHAE")) {
            if (name.endsWith(suffix)) {
                return name.substring(0, name.length() - suffix.length());
            }
        }
        return name;
    }

    private static boolean isNaturalLeaf(final Block block) {
        if (block.getBlockData() instanceof final Leaves leaves) {
            return !leaves.isPersistent();
        }
        return Tag.WART_BLOCKS.isTagged(block.getType()) || block.getType() == Material.SHROOMLIGHT;
    }
}
