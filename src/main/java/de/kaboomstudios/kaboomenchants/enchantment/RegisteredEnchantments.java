package de.kaboomstudios.kaboomenchants.enchantment;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.Nullable;

/** Findet zur Laufzeit die Verzauberung, die der Server beim Start aus einer {@link CustomEnchantment} gemacht hat. */
public final class RegisteredEnchantments {

    private RegisteredEnchantments() {
    }

    /**
     * {@code null}, wenn der Server sie nicht kennt. Das passiert nur, wenn das Plugin im
     * laufenden Betrieb ausgetauscht wurde, ohne den Server neu zu starten.
     */
    public static @Nullable Enchantment of(final CustomEnchantment enchantment) {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(enchantment.key());
    }
}
