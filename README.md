<p align="center">
  <img src="icon.png" alt="Ravengard Plus icon" width="128" height="128">
</p>

<h1 align="center">Ravengard Plus</h1>

<p align="center">
  Client-side item utilities for Hypixel Ravengard on Minecraft 1.21.5-1.21.11 and 26.1-26.2, with Fabric builds.
</p>

<p align="center">
  <a href="https://www.curseforge.com/minecraft/mc-mods/ravengardplus"><img src="https://img.shields.io/curseforge/dt/1635875?logo=curseforge&amp;label=CurseForge%20downloads" alt="CurseForge downloads"></a>
  <a href="https://github.com/Senior-S/Ravengard-Plus/releases"><img src="https://img.shields.io/github/downloads/Senior-S/Ravengard-Plus/total?logo=github&amp;label=GitHub%20downloads" alt="GitHub downloads"></a>
</p>

Ravengard Plus adds clear inventory markers for item value and equipment upgrades. Every feature is visual and runs on the client.

## Features

### Crown indicators

Items with a Crown value in their lore receive a configurable color indicator. The default tiers are:

- `0-4` Crowns: white
- `5-9` Crowns: green
- `10+` Crowns: red

Soft slot tint is the default style. Pixel corners, a bottom stripe, side markers, and a full outline are also available. You can add or remove tiers and choose a color and maximum Crown value for each one.

### Container replacement hints

When every usable inventory slot is occupied, opening a container compares its Crown-valued items with the cheapest replaceable item you carry. A bright tint marks the inventory item to replace whenever the container offers something more valuable. Container items worth the same or less receive a light-gray tint. Accessory slots and the final hotbar slot remain reserved and do not count toward inventory fullness.

An optional Crown-value overlay can show the lore glyph and number in the upper-left corner of each item. The overlay is disabled by default, with separate glyph and number colors and an option to hide the glyph.

The player inventory screen shows the combined Crown value of carried and equipped items below the ability slots. Stack values are multiplied by item count, and container contents are excluded.

### Armor upgrades

An arrow marks the best armor upgrade for each equipment slot. Armor models the current class cannot equip get skipped. The indicator has its own toggle and color setting.

### Accessory upgrades

Necklaces, earrings, belts, and rings are compared with the matching accessory. One arrow marks the strongest candidate that preserves every current buff and improves at least one value. Extra positive buffs count as an improvement. Accessory indicators have a separate toggle and color.

### Ability cooldowns

The two abilities in the player crafting grid receive on-screen cooldown timers beside the hotbar. The left side tracks a drop-key attempt while an item is held, and the right side tracks a swap-hand-key attempt. A timer starts after the game confirms the ability use in chat, so blocked attempts are ignored. Abilities described as lasting `for X seconds` begin their cooldown after that effect ends. Cooldown lore supports decimal seconds, minutes, and combined values such as `1m 44s`. Timer color and text scale are configurable, with scale limited to 50-150%. An optional expiry sound can be selected from Minecraft's registered sound events and previewed from the configuration screen.

### Hitboxes

Hitboxes can remain visible without Minecraft's debug shortcut. Player and mob outlines use separate configurable colors, with bright red players and white mobs by default. Armor stand hitboxes are hidden by default. Always-on hitboxes remain disabled until enabled in the configuration screen.

## Configuration

Press `O` in game to open the configuration screen. You can rebind this key under Minecraft's Controls settings.

The menu is powered by [YetAnotherConfigLib](https://modrinth.com/mod/yacl) and covers Crown tiers, indicator styles, feature toggles, and colors.

## Installation

### Fabric

1. Install [Fabric](https://fabricmc.net/use/installer/) for your Minecraft version.
2. Add [Fabric API](https://modrinth.com/mod/fabric-api) and [YetAnotherConfigLib](https://modrinth.com/mod/yacl) to the `mods` folder.
3. Add the matching Ravengard Plus jar to the same folder.

| Minecraft | Ravengard Plus jar | Java | YACL |
| --- | --- | --- | --- |
| 1.21.5-1.21.8 | `+mc1.21.5-1.21.8` | 21 or newer | 3.8.2 or newer for your Minecraft version |
| 1.21.9-1.21.10 | `+mc1.21.9-1.21.10` | 21 or newer | 3.8.0 or newer for your Minecraft version |
| 1.21.11 | `+mc1.21.11` | 21 or newer | 3.8.2 or newer for Minecraft 1.21.11 |
| 26.1-26.1.2 | `+mc26.1-26.1.2` | 25 or newer | 3.9.5 or newer for Minecraft 26.1 |
| 26.2 | `+mc26.2` | 25 or newer | 3.9.5 or newer for Minecraft 26.2 |

All versions require Fabric Loader 0.19.3 or newer.

## Building from source

Build the Minecraft 26.2 jar with Java 25:

```shell
./gradlew build
```

Build the Minecraft 26.1-26.1.2 jar with Java 25:

```shell
./gradlew -p modern-26.1 build
```

Build the Minecraft 1.21.5-1.21.8 jar with Java 21 or newer:

```shell
./gradlew -p legacy build
```

Build the Minecraft 1.21.9-1.21.10 jar with Java 21 or newer:

```shell
./gradlew -p legacy-1.21.9 build
```

Build the Minecraft 1.21.11 jar with Java 21 or newer:

```shell
./gradlew -p legacy-1.21.11 build
```

Each jar is written to its project's `build/libs` directory.

## Disclaimer

This project is an independent community mod. It is not affiliated with or endorsed by Hypixel Studios, Hypixel Inc., Mojang Studios, or Microsoft.

## License

Ravengard Plus is released under the [Apache License 2.0](LICENSE).
