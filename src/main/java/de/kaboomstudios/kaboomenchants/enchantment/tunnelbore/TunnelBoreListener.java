package de.kaboomstudios.kaboomenchants.enchantment.tunnelbore;

import de.kaboomstudios.kaboomenchants.KaBoomEnchants;
import de.kaboomstudios.kaboomenchants.breaking.BlockRules;
import de.kaboomstudios.kaboomenchants.breaking.PlayerBlockBreaker;
import de.kaboomstudios.kaboomenchants.config.PluginSettings;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Löst den Bereichsabbau aus, wenn ein Spieler mit Tunnel Bore einen Block abbaut.
 *
 * <p>Hört auf {@link EventPriority#MONITOR} und nur auf nicht abgebrochene Ereignisse: Erst wenn
 * alle anderen Plugins, auch Schutz-Plugins, den mittleren Block erlaubt haben, beginnt der
 * Bereich. Jeder weitere Block wird über {@link PlayerBlockBreaker} einzeln erneut geprüft.</p>
 */
final class TunnelBoreListener implements Listener {

    private final KaBoomEnchants plugin;
    private final TunnelBore tunnelBore;
    private final Enchantment enchantment;
    private final PlayerBlockBreaker breaker;

    /** Die Einstellungen werden nach jedem Reload einmal neu ausgewertet, nicht bei jedem Block. */
    private PluginSettings parsedFrom;
    private TunnelBoreSettings settings;

    TunnelBoreListener(final KaBoomEnchants plugin, final TunnelBore tunnelBore, final Enchantment enchantment,
                       final PlayerBlockBreaker breaker) {
        this.plugin = plugin;
        this.tunnelBore = tunnelBore;
        this.enchantment = enchantment;
        this.breaker = breaker;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (breaker.isBreaking(player)) {
            // Ein Block aus dem eigenen Bereich: nicht erneut einen Bereich auslösen.
            return;
        }
        if (!plugin.startupSettings().of(tunnelBore).enabled()) {
            return;
        }
        final ItemStack tool = player.getInventory().getItemInMainHand();
        final int level = tool.getEnchantmentLevel(enchantment);
        if (level <= 0) {
            return;
        }

        final TunnelBoreSettings current = settings();
        if (current.sneakDisables() && player.isSneaking()) {
            return;
        }
        final Block center = event.getBlock();
        if (current.onlyEffectiveBlocks() && !BlockRules.isEffective(center, tool)) {
            // Wer mit der Spitzhacke Erde abbaut, will keinen Tunnel durch den Stein dahinter.
            return;
        }

        breaker.breakBlocks(player, TunnelBoreArea.around(center, player, level),
                block -> !current.blockedBlocks().contains(block.getType())
                        && (!current.onlyEffectiveBlocks() || BlockRules.isEffective(block, tool)),
                current.durabilityPerExtraBlock());
    }

    private TunnelBoreSettings settings() {
        final PluginSettings now = plugin.settings();
        if (now != parsedFrom) {
            settings = TunnelBoreSettings.read(now.of(tunnelBore), plugin.getSLF4JLogger());
            parsedFrom = now;
        }
        return settings;
    }
}
