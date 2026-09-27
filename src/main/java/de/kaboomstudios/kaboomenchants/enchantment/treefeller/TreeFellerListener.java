package de.kaboomstudios.kaboomenchants.enchantment.treefeller;

import de.kaboomstudios.kaboomenchants.KaBoomEnchants;
import de.kaboomstudios.kaboomenchants.breaking.PlayerBlockBreaker;
import de.kaboomstudios.kaboomenchants.config.PluginSettings;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Fällt den ganzen Baum, wenn ein Spieler mit Tree Feller einen Stamm abbaut.
 *
 * <p>Wie bei Tunnel Bore erst nach allen anderen Plugins und nur, wenn der erste Stamm erlaubt
 * war; jeder weitere Stamm wird einzeln über {@link PlayerBlockBreaker} geprüft. Blätter werden
 * nicht abgebaut, sie zerfallen wie in Vanilla von selbst.</p>
 */
final class TreeFellerListener implements Listener {

    private final KaBoomEnchants plugin;
    private final TreeFeller treeFeller;
    private final Enchantment enchantment;
    private final PlayerBlockBreaker breaker;

    private PluginSettings parsedFrom;
    private TreeFellerSettings settings;

    TreeFellerListener(final KaBoomEnchants plugin, final TreeFeller treeFeller, final Enchantment enchantment,
                       final PlayerBlockBreaker breaker) {
        this.plugin = plugin;
        this.treeFeller = treeFeller;
        this.enchantment = enchantment;
        this.breaker = breaker;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (breaker.isBreaking(player)) {
            return;
        }
        if (!plugin.startupSettings().of(treeFeller).enabled()) {
            return;
        }
        if (player.getInventory().getItemInMainHand().getEnchantmentLevel(enchantment) <= 0) {
            return;
        }
        final TreeFellerSettings current = settings();
        if (current.sneakDisables() && player.isSneaking()) {
            return;
        }

        final TreeScanner.Tree tree = TreeScanner.scan(event.getBlock(), current.maxBlocks(), current.minNaturalLeaves());
        if (tree == null || tree.logs().isEmpty() || tree.naturalLeaves() < current.minNaturalLeaves()) {
            // Kein Stamm, einzelner Stamm oder gebaut: nur der eine Block, wie ohne Verzauberung.
            return;
        }
        breaker.breakBlocks(player, tree.logs(), block -> true, current.durabilityPerExtraBlock());
    }

    private TreeFellerSettings settings() {
        final PluginSettings now = plugin.settings();
        if (now != parsedFrom) {
            settings = TreeFellerSettings.read(now.of(treeFeller), plugin.getSLF4JLogger());
            parsedFrom = now;
        }
        return settings;
    }
}
