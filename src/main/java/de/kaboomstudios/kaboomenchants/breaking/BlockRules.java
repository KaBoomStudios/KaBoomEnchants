package de.kaboomstudios.kaboomenchants.breaking;

import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/** Feste Regeln, welche Blöcke ein Bereichsabbau nie anfasst, unabhängig von der Konfiguration. */
public final class BlockRules {

    private BlockRules() {
    }

    /**
     * Ob der Block mit diesem Werkzeug abgebaut werden darf.
     *
     * <p>Nie: Luft und Flüssigkeiten; unzerstörbare Blöcke wie Grundgestein; Blöcke mit Inventar
     * wie Kisten, Shulkerboxen, Öfen oder Fässer, weil deren Inhalt herausfallen würde und
     * Crates anderer Plugins oft genau solche Blöcke sind; und Blöcke, die mit diesem Werkzeug
     * nichts droppen würden, etwa Obsidian mit einer Eisenspitzhacke. {@code breakBlock} baut
     * unabhängig von der Härte sofort ab und würde solche Blöcke ersatzlos zerstören.</p>
     */
    public static boolean canBreakWith(final Block block, final ItemStack tool) {
        if (block.isEmpty() || block.isLiquid()) {
            return false;
        }
        if (block.getType().getHardness() < 0) {
            return false;
        }
        if (block.getState(false) instanceof InventoryHolder) {
            return false;
        }
        return block.isPreferredTool(tool);
    }

    /**
     * Ob das Werkzeug für diesen Block gemacht ist, also schneller abbaut als die bloße Hand.
     * Eine Spitzhacke gilt so für Stein, nicht für Erde; eine Schaufel umgekehrt.
     */
    public static boolean isEffective(final Block block, final ItemStack tool) {
        return block.getBlockData().getDestroySpeed(tool, false) > 1.0f;
    }
}
