package de.kboomgaarden.kaboomenchants;

import de.kboomgaarden.kaboomenchants.commands.KaBoomCommand;
import de.kboomgaarden.kaboomenchants.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;

public class KaBoomPlugin extends JavaPlugin {

    @Override
    public void onEnable() {    // lädt das Plugin beim Server start
        // Diese Zeile gibt eine Nachricht in der Serverconsole aus
        getLogger().info("KaBoomEnchants wurde erfolgreich geladen");   // quasi die Minecraft version von sout

        // Hier werden die Listener offiziell beim Server angemeldet.
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new AnvilListener(), this);
        getServer().getPluginManager().registerEvents(new EnchantmentTableListener(), this);
        getServer().getPluginManager().registerEvents(new VillagerTradeListener(), this);
        getServer().getPluginManager().registerEvents(new BaumesserListener(), this);


        // Entwicklerbefehl um item zu geben
        getCommand("kaboom").setExecutor(new KaBoomCommand(this));
    }

    @Override
    public void onDisable() {
        getLogger().info("KaBoomEnchants wurde deaktiviert");
    }
}
