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

import java.util.ArrayList;
import java.util.List;


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

        // Abfrage ob überhaupt ein Argument (bohrer oder baumesser) eingegeben wurde
        if (args.length == 0) {
            player.sendMessage("§cBitte nutze: /kaboom <bohrer|baumesser> [level]");
            return true;
        }

        String type = args[0].toLowerCase();

        if (type.equals("bohrer")) {
            int level = 1;

            // prüfen ob der spieler eine Zahl eingegeben hat.
            if (args.length > 1) {
                try {
                    // wir versuchen, den Text aus dem Chat in einen Integer umzuwandeln.
                    level = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    // Falls der Spieler quatsch eingibt.
                    player.sendMessage("§cBitte gib eine gültige Zahl ein!");
                    return true;
                }
            }

            // aus der Spitzhacke wird ein verzaubertes Buch
            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                // hier erschaffen wir unser unsichtbares Etikett
                NamespacedKey key = new NamespacedKey(plugin, "bohrer_level");
                // hier nutzen wir die dynamische variable "level" anstatt der festen 1
                meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);

                List<String> lore = new ArrayList<>();
                // Lore exakt wie im Amboss benennen, für eine fehlerfrei Illusion
                lore.add("§7Bohrer " + level);
                meta.setLore(lore);
                // Die veränderten daten wieder auf das Item kleben
                item.setItemMeta(meta);
            }

            // Dem Spieler das Item ins Inventar legen
            player.getInventory().addItem(item);
            player.sendMessage("§aDu hast das Zauberbuch (Stufe " + level + ")  erhalten!");

        } else if (type.equals("baumesser")) {
            // Für den test direkt eine fertig verzauberte Axt ausgeben
            ItemStack item = new ItemStack(Material.DIAMOND_AXE);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                NamespacedKey key = new NamespacedKey(plugin, "baumesser");

                // Der Baumesser hat nur 1 Stufe deswegen wird der Wert fest auf 1 gesetzt.
                meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);

                List<String> lore = new ArrayList<>();
                // keine Zahl in der Lore, da es keine hähere Stufe gibt
                lore.add("§7Baumesser");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            player.getInventory().addItem(item);
            player.sendMessage("§aDu hast eine Diamantaxt (Baumesser) erhalten!");
        } else {
            // Fehlermeldung bei falscher eingabe
            player.sendMessage("§cUnbekannte Verzauberung. Nutze: /kaboom <bohrer|baumesser>");
        }
        return true;
    }
}
