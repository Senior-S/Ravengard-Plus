<p align="center">
  <img src="icon.png" alt="Ravengard Plus icon" width="128" height="128">
</p>

<h1 align="center">Ravengard Plus</h1>

<p align="center">
  Client-side item utilities for Hypixel Ravengard on Minecraft 26.2.
</p>

Ravengard Plus adds clear inventory markers for item value and equipment upgrades. Every feature is visual and runs on the client.

## Features

### Crown indicators

Items with a Crown value in their lore receive a configurable color indicator. The default tiers are:

- `0-4` Crowns: white
- `5-9` Crowns: green
- `10+` Crowns: red

Soft slot tint is the default style. Pixel corners, a bottom stripe, side markers, and a full outline are also available. You can add or remove tiers and choose a color and maximum Crown value for each one.

### Armor upgrades

An arrow marks armor with more Defense than the item currently equipped in the same slot. Armor models the current class cannot equip get skipped. The indicator has its own toggle and color setting.

### Accessory upgrades

Necklaces, earrings, belts, and rings are compared with the matching accessory. An arrow appears when a candidate preserves every current buff and improves at least one value. Extra positive buffs count as an improvement. Accessory indicators have a separate toggle and color.

## Configuration

Press `O` in game to open the configuration screen. You can rebind this key under Minecraft's Controls settings.

The menu is powered by [YetAnotherConfigLib](https://modrinth.com/mod/yacl) and covers Crown tiers, indicator styles, feature toggles, and colors.

## Installation

1. Install [Fabric](https://fabricmc.net/use/installer/) for Minecraft 26.2.
2. Add [Fabric API](https://modrinth.com/mod/fabric-api) and [YetAnotherConfigLib](https://modrinth.com/mod/yacl) to the `mods` folder.
3. Add the Ravengard Plus jar to the same folder.

Ravengard Plus requires Java 25, Fabric Loader 0.19.3 or newer, and YACL 3.9.5 or newer for Minecraft 26.2.

## Disclaimer

This project is an independent community mod. It is not affiliated with or endorsed by Hypixel Studios, Hypixel Inc., Mojang Studios, or Microsoft.

## License

Ravengard Plus is released under the [Apache License 2.0](LICENSE).
