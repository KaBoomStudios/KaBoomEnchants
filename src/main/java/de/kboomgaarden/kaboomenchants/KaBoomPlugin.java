package de.kboomgaarden.kaboomenchants;

import de.kboomgaarden.kaboomenchants.listeners.BlockBreakListener;
import org.bukkit.plugin.java.JavaPlugin;

public class KaBoomPlugin extends JavaPlugin {

    @Override
    public void onEnable() {    // lädt das Plugin beim Server start
        // Diese Zeile gibt eine Nachricht in der Serverconsole aus
        getLogger().info("KaBoomEnchants wurde erfolgreich geladen");   // quasi die Minecraft version von sout

        // Hier melden wir den Listener offiziell beim Server an
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("KaBoomEnchants wurde deaktiviert");
    }
}
