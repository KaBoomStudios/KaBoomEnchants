package de.kaboomstudios.kaboomenchants.breaking;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * Baut zusätzliche Blöcke so ab, als hätte der Spieler sie selbst abgebaut.
 *
 * <p>Jeder Block geht über {@link Player#breakBlock(Block)}. Das löst für jeden Block ein
 * eigenes {@code BlockBreakEvent} aus, sodass Schutz-Plugins wie WorldGuard, Grundstücks- und
 * Protokoll-Plugins ihn einzeln prüfen und sehen, ohne dass dieses Plugin eines davon kennen
 * muss. Wird ein Block verweigert, bleibt er stehen. Drops, Glück, Behutsamkeit und Erfahrung
 * kommen vom Server wie beim normalen Abbau.</p>
 *
 * <p>Die Abnutzung des Werkzeugs steuert diese Klasse selbst: Den Abzug, den {@code breakBlock}
 * je Block vornimmt, unterdrückt sie und bucht stattdessen einen einstellbaren Anteil ab. Das
 * geschieht über {@link Player#damageItemStack(EquipmentSlot, int)}, damit Haltbarkeit
 * (Unbreaking) und das Zerbrechen des Werkzeugs wie in Vanilla wirken.</p>
 */
public final class PlayerBlockBreaker implements Listener {

    /** Spieler, für die gerade ein Bereich abgebaut wird. Nur auf dem Hauptthread benutzt. */
    private final Map<UUID, Session> sessions = new HashMap<>();

    /**
     * Ob für diesen Spieler gerade ein Bereich abgebaut wird. Listener, die auf
     * {@code BlockBreakEvent} reagieren, müssen dann still bleiben; sonst löste jeder
     * zusätzliche Block wieder einen ganzen Bereich aus.
     */
    public boolean isBreaking(final Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    /**
     * Baut die Blöcke in der gegebenen Reihenfolge ab.
     *
     * @param blocks            nahe Blöcke zuerst, damit bei einem zerbrechenden Werkzeug das
     *                          Naheliegende erledigt ist
     * @param allowed           zusätzliche Bedingung je Block, geprüft direkt vor dem Abbau
     * @param durabilityPerBlock Anteil des normalen Abzugs je Block, 0 bis 1
     * @return Anzahl der tatsächlich abgebauten Blöcke
     */
    public int breakBlocks(final Player player, final List<Block> blocks, final Predicate<Block> allowed,
                           final double durabilityPerBlock) {
        if (isBreaking(player)) {
            return 0;
        }
        final Material tool = player.getInventory().getItemInMainHand().getType();
        final Session session = new Session();
        sessions.put(player.getUniqueId(), session);

        int broken = 0;
        double owedDamage = 0;
        try {
            for (final Block block : blocks) {
                if (!BlockRules.canBreakWith(block, player.getInventory().getItemInMainHand()) || !allowed.test(block)) {
                    continue;
                }
                session.suppressDamage = true;
                final boolean success;
                try {
                    success = player.breakBlock(block);
                } finally {
                    session.suppressDamage = false;
                }
                if (!success) {
                    // Von einem Schutz-Plugin oder dem Server verweigert: stehen lassen.
                    continue;
                }
                broken++;

                owedDamage += durabilityPerBlock;
                final int whole = (int) owedDamage;
                if (whole > 0) {
                    owedDamage -= whole;
                    player.damageItemStack(EquipmentSlot.HAND, whole);
                    if (toolBroke(player, tool)) {
                        return broken;
                    }
                }
            }
            // Der angebrochene Rest wird ausgewürfelt statt verworfen: So kostet ein kleiner
            // Bereich im Mittel genau den eingestellten Anteil und nicht regelmäßig nichts.
            if (owedDamage > 0 && ThreadLocalRandom.current().nextDouble() < owedDamage) {
                player.damageItemStack(EquipmentSlot.HAND, 1);
            }
            return broken;
        } finally {
            sessions.remove(player.getUniqueId());
        }
    }

    /**
     * Unterdrückt den Vanilla-Abzug, den {@code breakBlock} je Block vornimmt. Den eigenen Abzug
     * über {@code damageItemStack} lässt er durch, weil dort {@code suppressDamage} aus ist.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onItemDamage(final PlayerItemDamageEvent event) {
        final Session session = sessions.get(event.getPlayer().getUniqueId());
        if (session != null && session.suppressDamage) {
            event.setCancelled(true);
        }
    }

    private static boolean toolBroke(final Player player, final Material tool) {
        final ItemStack inHand = player.getInventory().getItemInMainHand();
        return inHand.isEmpty() || inHand.getType() != tool;
    }

    private static final class Session {
        private boolean suppressDamage;
    }
}
