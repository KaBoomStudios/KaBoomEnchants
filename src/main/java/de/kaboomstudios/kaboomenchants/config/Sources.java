package de.kaboomstudios.kaboomenchants.config;

/** Die Wege, auf denen Spieler an eine Verzauberung kommen. {@code kbe give} geht immer. */
public record Sources(boolean enchantingTable, boolean anvil, boolean trading, boolean loot) {

    public static final Sources NONE = new Sources(false, false, false, false);

    public boolean any() {
        return enchantingTable || anvil || trading || loot;
    }
}
