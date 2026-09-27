package de.kaboomstudios.kaboomenchants.enchantment;

import de.kaboomstudios.kaboomenchants.enchantment.arise.Arise;
import de.kaboomstudios.kaboomenchants.enchantment.treefeller.TreeFeller;
import de.kaboomstudios.kaboomenchants.enchantment.tunnelbore.TunnelBore;

import java.util.List;
import java.util.Optional;

/** Die einzige Liste aller Verzauberungen des Plugins. Ihre Reihenfolge gilt auch für Anzeigen. */
public final class EnchantmentCatalog {

    public static final List<CustomEnchantment> ALL = List.of(
            new TunnelBore(),
            new TreeFeller(),
            new Arise()
    );

    private EnchantmentCatalog() {
    }

    public static Optional<CustomEnchantment> byId(final String id) {
        return ALL.stream().filter(enchantment -> enchantment.id().equals(id)).findFirst();
    }
}
