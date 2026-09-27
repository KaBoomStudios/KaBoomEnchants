package de.kaboomstudios.kaboomenchants.rules;

import de.kaboomstudios.kaboomenchants.KaBoomEnchants;
import de.kaboomstudios.kaboomenchants.config.EnchantmentSettings;
import de.kaboomstudios.kaboomenchants.enchantment.CustomEnchantment;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Die Sperren an Amboss, Schleifstein, Werkbank und Schmiedetisch, je Verzauberung einstellbar.
 *
 * <p>Alle Regeln setzen nur das angezeigte Ergebnis auf leer; der Spieler sieht dann wie bei
 * einer ungültigen Vanilla-Kombination einfach nichts zum Herausnehmen. Sie lesen den aktuellen
 * Stand der Konfiguration und wechseln deshalb per Reload. Gehört wird auf
 * {@link EventPriority#HIGHEST}, damit andere Plugins das Ergebnis vorher bilden können und die
 * Sperre trotzdem das letzte Wort hat.</p>
 */
public final class StationRules implements Listener {

    private final KaBoomEnchants plugin;
    private final Map<CustomEnchantment, Enchantment> enchantments;

    public StationRules(final KaBoomEnchants plugin, final Map<CustomEnchantment, Enchantment> enchantments) {
        this.plugin = plugin;
        this.enchantments = Map.copyOf(enchantments);
    }

    /**
     * Amboss. Zwei Fälle, beide über den Vergleich von linkem Item und Ergebnis:
     * <ul>
     *   <li>Neu oder höher: Die Verzauberung kommt per Buch oder von einem zweiten Item hinzu.
     *       Gesperrt, wenn der Amboss als Weg aus ist. So lässt sie sich auch nicht von einem
     *       Item auf ein anderes übertragen.</li>
     *   <li>Reparatur: Das Ergebnis trägt die Verzauberung und ist weniger beschädigt als das
     *       linke Item, egal ob per Material oder zweitem Item. Gesperrt, wenn sie nicht
     *       reparierbar ist. Umbenennen bleibt erlaubt.</li>
     * </ul>
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvil(final PrepareAnvilEvent event) {
        final ItemStack result = event.getResult();
        final ItemStack left = event.getInventory().getFirstItem();
        if (isEmpty(result) || isEmpty(left)) {
            return;
        }
        for (final Map.Entry<CustomEnchantment, Enchantment> entry : enchantments.entrySet()) {
            final EnchantmentSettings settings = plugin.settings().of(entry.getKey());
            final int before = level(left, entry.getValue());
            final int after = level(result, entry.getValue());
            if (after > before && !settings.sources().anvil()) {
                event.setResult(null);
                return;
            }
            if (after > 0 && !settings.repairable() && damage(result) < damage(left)) {
                event.setResult(null);
                return;
            }
        }
    }

    /**
     * Schleifstein. Ein einzelnes Item verliert dort seine Verzauberungen, zwei gleiche Items
     * werden zusammen repariert und verlieren sie ebenfalls.
     *
     * <p>Ist die Verzauberung nicht entfernbar, nimmt der Schleifstein das Item gar nicht an,
     * statt sie auf dem Ergebnis zu belassen: Die ausgezahlte Erfahrung berechnet das Spiel aus
     * den Verzauberungen des Eingangs, ohne dass sich das über die API ändern lässt. Ein Item,
     * das seine Verzauberung behält, ließe sich sonst beliebig oft für Erfahrung schleifen.</p>
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGrindstone(final PrepareGrindstoneEvent event) {
        if (isEmpty(event.getResult())) {
            return;
        }
        final ItemStack upper = event.getInventory().getUpperItem();
        final ItemStack lower = event.getInventory().getLowerItem();
        final boolean repair = !isEmpty(upper) && !isEmpty(lower);
        if (carriesAny(upper, settings -> !settings.grindstoneRemovable() || repair && !settings.repairable())
                || carriesAny(lower, settings -> !settings.grindstoneRemovable() || repair && !settings.repairable())) {
            event.setResult(null);
        }
    }

    /**
     * Werkbank: Zwei gleiche beschädigte Items ergeben ein repariertes ohne Verzauberungen. Das
     * ist zugleich eine Reparatur und ein Entfernen, deshalb gilt beides.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraft(final PrepareItemCraftEvent event) {
        if (!event.isRepair()) {
            return;
        }
        for (final ItemStack ingredient : event.getInventory().getMatrix()) {
            if (carriesAny(ingredient, settings -> !settings.repairable() || !settings.grindstoneRemovable())) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    /**
     * Schmiedetisch: nur die Netherit-Aufwertung, Rüstungsverzierungen bleiben erlaubt. Das
     * Ergebnis wird über das Ereignis gesetzt, nicht über das Inventar: Paper übernimmt nach dem
     * Ereignis dessen Ergebnis und würde eine Änderung am Inventar wieder überschreiben.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSmithing(final PrepareSmithingEvent event) {
        final ItemStack template = event.getInventory().getInputTemplate();
        if (isEmpty(template) || template.getType() != Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE) {
            return;
        }
        if (carriesAny(event.getInventory().getInputEquipment(), settings -> !settings.netheriteUpgrade())) {
            event.setResult(null);
        }
    }

    /** Ob das Item eine der Verzauberungen trägt, deren Einstellungen die Bedingung erfüllen. */
    private boolean carriesAny(final ItemStack item, final Predicate<EnchantmentSettings> blocked) {
        if (isEmpty(item)) {
            return false;
        }
        for (final Map.Entry<CustomEnchantment, Enchantment> entry : enchantments.entrySet()) {
            if (level(item, entry.getValue()) > 0 && blocked.test(plugin.settings().of(entry.getKey()))) {
                return true;
            }
        }
        return false;
    }

    /** Stufe auf dem Item; bei verzauberten Büchern die gespeicherte. */
    private static int level(final ItemStack item, final Enchantment enchantment) {
        final ItemMeta meta = item.getItemMeta();
        if (meta instanceof final EnchantmentStorageMeta storage) {
            return storage.getStoredEnchantLevel(enchantment);
        }
        return item.getEnchantmentLevel(enchantment);
    }

    private static int damage(final ItemStack item) {
        return item.getItemMeta() instanceof final Damageable damageable ? damageable.getDamage() : 0;
    }

    private static boolean isEmpty(final ItemStack item) {
        return item == null || item.isEmpty();
    }
}
