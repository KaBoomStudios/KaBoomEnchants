# KaBoom Enchants

Custom enchantments for Paper that are **real enchantments**: registered through Paper's registry
API, shown in the item tooltip like vanilla ones, with vanilla levels and vanilla anvil rules.

What sets it apart:

- **You decide how players get them.** For every enchantment the configuration switches each way
  into the game on or off: enchanting table, anvil, librarian trades and loot chests. With
  everything off, an enchantment only exists through `/kbe give`, for example as a rare crate or
  event reward.
- **You decide how they behave afterwards.** Per enchantment: can the grindstone remove it, can
  items with it be repaired, may Mending sit on the same item, may the item be upgraded to
  netherite.
- **Protection plugins just work.** Every extra block the mining enchantments break goes through
  the normal block break event, so WorldGuard and other protection plugins decide block by block,
  without being a dependency.

## Enchantments

| Enchantment | German name | Items | Levels | Effect |
|---|---|---|---|---|
| **Tunnel Bore** | Tunnelbohrer | Pickaxes, shovels | I to III | Mines an area around the block: 3x3, 5x5 or 7x7 across, and 3, 5 or 7 blocks deep in the direction the player looks. Looking steeply down or up bores a shaft |
| **Tree Feller** | Baumesser | Axes | I | Fells the whole tree at once, overworld trees and nether stems alike |
| **Arise** | Arise | Totem of Undying | I to III | The totem saves its holder 2, 3 or 4 times in total. After each save it comes back one level lower; level I turns into a plain totem, which saves one last time |

**Tunnel Bore**

- Only starts when the block the player mined was allowed (for example by protection plugins).
- Every extra block is mined as if the player mined it by hand: Fortune and Silk Touch apply,
  experience and drops come from the server as usual.
- Never mined: air, liquids, unbreakable blocks such as bedrock, blocks with an inventory (chests,
  shulker boxes, furnaces, barrels, hoppers and the like) and blocks the tool would not drop, such
  as obsidian with an iron pickaxe. On top of that there is a configurable block list.
- Only blocks the tool is made for: a pickaxe skips dirt, a shovel skips stone. Mining a block the
  tool is not made for mines just that block. Can be turned off.
- Sneaking mines only the single block, for precise work.
- Each extra block costs a configurable share of the normal tool damage, 10 % by default.
  Unbreaking works as usual. When the tool breaks, mining stops.

**Tree Feller**

- Only fells natural trees: enough leaves grown by the game have to touch the logs. Leaves placed
  by players never count, so log houses stay standing, even with leaves as decoration.
- Only logs of the same wood type: a birch next to an oak stays. Stripped logs and wood blocks of
  the same type belong to the tree. Leaves are not mined, they decay as usual.
- Giant 2x2 trees and diagonal branches are felled completely. Huge mushrooms are not.
- Protection, tool damage, sneaking and the stop on a broken tool work like Tunnel Bore.

**Arise**

- Works in either hand. With a totem in both hands, the game uses the main hand first, as in vanilla.
- The new totem goes back into the same hand; other properties of the totem, such as a custom
  name, are kept. Above the hotbar the player sees how many saves the totem has left.
- A totem with Arise in the inventory but not in a hand does not save the player, as in vanilla.
- The enchanting table never offers Arise, because vanilla never enchants totems there and Arise
  would be the only possible result. Anvil, trading and loot work: trading and loot hand out an
  enchanted book, which the anvil then puts onto a totem.

## Requirements

| | Version |
|---|---|
| Server | [Paper](https://papermc.io) 26.1.2 |
| Java | 25 |

No other plugins are needed. KaBoom Enchants is a Paper plugin (it registers enchantments while
the server starts, which Paper only allows for Paper plugins), so it does not run on Spigot. It
only uses the official Paper API.

## Installation

1. Put the jar into the `plugins` folder of your server.
2. Start the server. The plugin creates its folder with `config.yml`, `messages_en.yml` and
   `messages_de.yml`.
3. Choose the ways players get each enchantment in `config.yml` and restart the server.

Always restart the server to install or update the plugin, never use `/reload`: enchantments can
only be registered while the server starts.

## Commands and permissions

`/kbenchants` can also be used as `/kbe`. All arguments have tab completion. Commands a player may
not use are hidden from them, including in tab completion.

| Command | Description | Permission |
|---|---|---|
| `/kbe help` | Lists the commands you may use. `/kbe` alone does the same. In game, hover a command for an explanation and click it to put it into the chat line | everyone |
| `/kbe give <player> <enchantment> [level] [book\|item]` | Gives an enchanted book (default) or a ready enchanted item. The level defaults to I and cannot exceed the maximum level. Works from the console, see below | `kbenchants.give` |
| `/kbe list` | Lists all enchantments with their maximum level and the ways players can currently get them | `kbenchants.list` |
| `/kbe reload` | Reloads `config.yml` and the language files, and names every changed value that needs a restart | `kbenchants.reload` |

| Permission | Default | Allows |
|---|---|---|
| `kbenchants.admin` | op | All of the permissions below |
| `kbenchants.give` | op | `/kbe give` |
| `kbenchants.list` | op | `/kbe list` |
| `kbenchants.reload` | op | `/kbe reload` |

Using the enchantments needs no permission.

### Rewards from other plugins

KaBoom Enchants knows nothing about crate, shop or event plugins, and needs no integration. There
are two ways to use its enchantments as rewards:

- **Store the item directly.** Enchant an item (for example with `/kbe give <you> tunnel_bore 3
  item`), hold it and save it as a reward in the other plugin. The enchantment is part of the item.
- **Run a console command.** Most reward plugins can run console commands:

```
kbe give {player} tunnel_bore 3 item
kbe give {player} arise 1 book
```

`/kbe give` always works, no matter which ways are switched on in the configuration. If the
player's inventory is full, the item drops at their feet and only they can pick it up.

## Configuration

Values marked **[restart]** are read while the server starts and only take effect after a full
restart; `/kbe reload` tells you when you changed one of them. Values marked **[reload]** apply
with `/kbe reload`. The shipped `config.yml` explains every value in comments.

```yaml
language: en            # "en" or "de", or your own messages_<language>.yml

enchantments:
  tunnel_bore:
    enabled: true
    sources:
      enchanting-table: false
      anvil: false
      trading: false
      loot: false
    grindstone-removable: false
    repairable: false
    mending-allowed: false
    netherite-upgrade: false
    weight: 1
    give-item: diamond_pickaxe
    # ... values only Tunnel Bore has, see below
```

The shipped configuration switches every way off, so out of the box the enchantments only exist
through `/kbe give`. Switch on what fits your server.

**Values of every enchantment**

| Value | | Meaning |
|---|---|---|
| `enabled` | restart | `false`: the enchantment stays known to the server, so items keep it, but it has no effect and players cannot obtain it (`/kbe give` still works) |
| `sources.enchanting-table` | restart | The enchanting table can offer it. Not available for Arise |
| `sources.anvil` | reload | It can be put onto an item at the anvil, from a book or from another item, including higher levels. `false` also stops moving it from one item to another |
| `sources.trading` | restart | Librarian villagers can offer it as an enchanted book, at the doubled price of treasure enchantments unless it is also on the enchanting table |
| `sources.loot` | restart | It can appear in loot chests that enchant their items randomly, for example enchanted books in dungeons |
| `grindstone-removable` | reload | The grindstone removes it. `false`: the grindstone does not accept items or books with it at all, and neither does the crafting grid repair |
| `repairable` | reload | `false`: items with it cannot be repaired at all, not at the anvil (with material or a second item), not at the grindstone, not in the crafting grid. Renaming still works. The item breaks for good |
| `mending-allowed` | restart | Mending may be on the same item. `false` works both ways, like vanilla exclusive enchantments |
| `netherite-upgrade` | reload | Items with it may be upgraded to netherite at the smithing table. Armor trims are not affected |
| `weight` | restart | How often the enchanting table and random loot pick it, from 1 (rarest) to 1024. For comparison on pickaxes: Efficiency 10, Unbreaking 5, Fortune 2, Silk Touch 1 |
| `give-item` | reload | The item `/kbe give ... item` creates |

**Tunnel Bore** (all reload)

| Value | Default | Meaning |
|---|---|---|
| `durability-per-extra-block` | `0.1` | Share of the normal tool damage each extra block costs, from 0.0 to 1.0. The mined block always costs the normal amount |
| `sneak-disables` | `true` | Sneaking mines only the single block |
| `only-effective-blocks` | `true` | Only mine blocks the tool is made for |
| `blocked-blocks` | spawners, trial spawners, vaults, budding amethyst, reinforced deepslate, suspicious sand and gravel | Blocks the area never mines. The area mines instantly, no matter how hard a block is: add `obsidian`, `crying_obsidian` or `ancient_debris` here if players should not clear them in bulk |

**Tree Feller** (all reload)

| Value | Default | Meaning |
|---|---|---|
| `durability-per-extra-block` | `0.1` | As for Tunnel Bore, per further log |
| `sneak-disables` | `true` | Sneaking fells only the single log |
| `max-blocks` | `1500` | Upper limit of further logs per tree |
| `min-natural-leaves` | `4` | How many natural leaves have to touch the logs. `0` turns the check off |

**Language files:** every text uses [MiniMessage](https://docs.advntr.dev/minimessage/format.html)
and can be changed freely. For another language, copy one of the files, rename it (for example
`messages_fr.yml`) and set `language: fr`. Texts missing from a language file fall back to English.
When an update adds new texts, they are added to the language file in use automatically, your own
changes stay untouched. The names of the enchantments are in the language files as well, under
`enchantment-name`.

### Updating

Replace the jar and restart the server. Your `config.yml` is never overwritten. Settings that an
update adds work with their default value right away, even though they do not appear in your file;
to see and change them, copy them over from the `config.yml` inside the jar or from this README.

## Compatibility with protection plugins

Tunnel Bore and Tree Feller only start after the block the player mined was allowed by every other
plugin. Each extra block is then broken with Paper's "break as the player" method, which fires a
separate, normal block break event for it. Protection plugins treat that event exactly like a
player breaking the block by hand: if they deny it, the block stays and the rest of the area is
still mined. No protection plugin is required and none needs special support.

Tested with WorldGuard (mining and felling across the border of a region: only the blocks outside
the region are mined) and vanilla spawn protection. Other plugins that react to broken blocks, such as block
loggers or job plugins, receive the same event for every extra block.

Crates of other plugins often are real blocks such as chests or shulker boxes. Blocks with an
inventory are never mined by the area.

## Building

The Gradle wrapper downloads Gradle and a matching JDK by itself:

```
./gradlew build          # jar in build/libs/
./gradlew runServer      # starts a local Paper 26.1.2 test server
```

The test server runs in the folder `run` inside the project. To use a different folder, create a file
`gradle-local.properties` next to `build.gradle.kts`. It is ignored by Git:

```properties
kbenchants.runDirectory=/path/to/test-server
# Optional: further plugin jars to load into the test server, separated by commas
kbenchants.extraPluginJars=/path/to/OtherPlugin.jar
```

## Status

The plugin is in active development and not released yet.

| Part | State |
|---|---|
| Registration, configurable ways into the game, names in English and German | done |
| `/kbe give`, `list`, `reload`, `help` with permissions and tab completion | done |
| Tunnel Bore | done |
| Tree Feller | done |
| Arise | done |
| Rules at anvil, grindstone, crafting grid and smithing table | done |
| Tested together with a crate plugin (stored item reward and `kbe give` as console command) | done |

Planned later: more enchantments, support for Paper 26.2 and newer, and a safe way to remove the
plugin's enchantments from all items before uninstalling.

## Known limitations

- **Removing the plugin removes its enchantments from items, for good.** Without the plugin, the
  server does not know these enchantments anymore. Items keep everything else (vanilla
  enchantments, damage, names), only Tunnel Bore, Tree Feller and Arise disappear from items and
  books in inventories and chests. Paper logs this as "Serialization errors". Once the server has
  saved without the plugin, installing it again does not bring them back. To take the
  enchantments out of the game, set `enabled: false` for them instead of removing the plugin. If
  you do remove it, back up your worlds first.
- **One language for enchantment names.** The name is sent to players as part of the
  enchantment itself when they join, so all players see it in the language set when the server
  started. Chat texts can be switched with `/kbe reload`.
- **The registry API is experimental.** Paper marks the API for custom enchantments as
  experimental. A future Paper version may need an update of the plugin.
- **Instant area mining.** Tunnel Bore mines the whole area at once, regardless of how hard the
  blocks are. Use `blocked-blocks` for blocks that should stay rare.
- **Nether roofs.** For nether trees, wart blocks and shroomlights count instead of leaves. The
  game does not mark those as placed by players, so a house of stems with a nether wart roof can
  be felled.
- **Trading.** Only the standard librarian trades are covered, not the experimental villager trade
  rebalance.
- **Arise and mobs.** Arise only works for players. A mob holding a totem with Arise is saved once,
  like with a plain totem.
