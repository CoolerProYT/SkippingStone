# Configuration

Settings live in `config/skippingstone-common.json5`, created the first time the game starts. The file is written with comments for every setting. Changes apply the next time the game or server starts.

::: warning Servers
The server's file decides what a throw does. Each player's own file decides how their power meter looks, so keep the `meter` settings the same for everyone on a server.
:::

## Stone

`stone.tiers` is the list of [stone qualities](./guide/stone-quality), worst first. Each has a `name` and a `velocityMultiplier`. The number of entries is the number of qualities. The default is:

```json5
tiers: [
  { name: "chipped", velocityMultiplier: 0.7 },
  { name: "rough", velocityMultiplier: 0.9 },
  { name: "smooth", velocityMultiplier: 1.1 },
  { name: "perfect", velocityMultiplier: 1.35 },
]
```

Names are shown through the translation key `stone_tier.skippingstone.<name>`, or as written if there is no translation. Textures follow the position in the list, so a fifth quality uses the first texture.

## Pickup

| Key | What it does |
| --- | --- |
| `blockConversions` | Which blocks give stones, and the block each turns into. Keys and values are block ids. |
| `defaultTierWeights` | Quality odds, one weight per quality, used where no biome entry matches. |
| `biomeTierWeights` | A list of `{ biomeTag, weights }`. The first entry whose biome tag contains the block's biome is used. |

```json5
biomeTierWeights: [
  { biomeTag: "minecraft:is_beach", weights: [30, 35, 25, 10] },
  { biomeTag: "minecraft:is_river", weights: [35, 35, 22, 8] },
  { biomeTag: "minecraft:is_ocean", weights: [40, 34, 20, 6] },
]
```

Weights are relative. `[30, 35, 25, 10]` means a 10% chance of a Perfect stone.

## Meter

<ConfigTable section="meter" />

## Throw

These are the numbers in [the formula](./guide/the-math).

<ConfigTable section="throw" />

## Records

<ConfigTable section="records" />
