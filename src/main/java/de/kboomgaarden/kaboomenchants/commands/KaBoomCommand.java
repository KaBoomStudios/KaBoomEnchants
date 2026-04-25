package de.kboomgaarden.kaboomenchants.commands;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;


public class KaBoomCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    // Konstruktor holt sich die Hauptklasse, da wir diese für das Etikett (NamespaceKey) zwingend brauchen
    public KaBoomCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // prüfen ob der Befehl von einem echten Spieler kommt (und nicht von der Serverkonsole)
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen");
            return true;
        }

        Player player = (Player) sender;

        // wir erschaffen unsere nackte Diamantspitzhacke
        ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = pickaxe.getItemMeta();

        if (meta != null) {
            // hier erschaffen wir unser unsichtbares Etikett
            NamespacedKey key = new NamespacedKey(plugin, "bohrer_level");
            // wir speichern die Zahl 1 (für Stufe 1) auf diesem Etikett
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
            // ein Name damit ich sie im Inventar erkenne
            meta.setDisplayName("§6Tunnelbohrer I");
            // Die veränderten daten wieder auf das Item kleben
            pickaxe.setItemMeta(meta);
        }

        // Dem Spieler das Items ins Inventar legen
        player.getInventory().addItem(pickaxe);
        player.sendMessage("§aDu hast die Bohrer Spitzhacke Stufe 1 erhalten!");
        return true;
    }
}
