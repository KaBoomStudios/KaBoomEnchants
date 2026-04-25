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
        // prüfen, ob der Befehl von einem echten Spieler kommt (und nicht von der Serverkonsole)
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen");
            return true;
        }

        Player player = (Player) sender;

        // Wir legen ein Standard level fest, falls der Spieler keine Zahl eingibt
        int level = 1;

        // Wir prüfen ob der spieler eine Zahl eingegeben hat.
        if (args.length > 0){
            try {
                // wir versuchen, den Text aus dem Chat in einen Integer umzuwandeln.
                level = Integer.parseInt(args[0]);
            } catch (NumberFormatException e){
                // Falls der Spieler quatsch eingibt.
                player.sendMessage("§cBitte gib eine gültige Zahl ein!");
                return true;
            }
        }

        // wir erschaffen unsere nackte Diamantspitzhacke
        ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = pickaxe.getItemMeta();

        if (meta != null) {
            // hier erschaffen wir unser unsichtbares Etikett
            NamespacedKey key = new NamespacedKey(plugin, "bohrer_level");
            // hier nutzen wir die dynamische variable "level" anstatt der festen 1
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);
            // ein Name damit ich sie im Inventar erkenne
            meta.setDisplayName("§6Tunnelbohrer Level " + level);
            // Die veränderten daten wieder auf das Item kleben
            pickaxe.setItemMeta(meta);
        }

        // Dem Spieler das Item ins Inventar legen
        player.getInventory().addItem(pickaxe);
        player.sendMessage("§aDu hast die Bohrer-Spitzhacke (Stufe " + level + ")  erhalten!");
        return true;
    }
}
