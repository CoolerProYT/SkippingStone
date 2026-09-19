# Finding stones

Skipping stones come from the edge of the water. With an **empty hand**, right-click one of these blocks:

<div class="ss-block-list">
  <ItemSlot id="minecraft:sand" label />
  <ItemSlot id="minecraft:red_sand" label />
  <ItemSlot id="minecraft:gravel" label />
</div>

The block only gives a stone when it is at the water's edge:

- **Water beside it.** At least one of the four blocks around it, on the same level, has to be water.
- **No water on top.** Sand and gravel under water, like riverbeds and the seafloor, never give stones.

Each block gives exactly one stone and then becomes a sifted block, so a shoreline runs out as you collect from it.

## Sifted blocks

Once its stone is taken, a block turns into its sifted version. It looks the same but sits two pixels lower, so you can see which parts of a shore are already picked.

<div class="ss-block-list">
  <ItemSlot id="skippingstone:sifted_sand" label />
  <ItemSlot id="skippingstone:sifted_red_sand" label />
  <ItemSlot id="skippingstone:sifted_gravel" label />
</div>

Sifted blocks never give stones. You can mine them with a shovel and place them like any other block.

## Which stone you get

Every stone has a random [quality](./stone-quality). The odds depend on the biome the block is in. Beaches give the best stones. Lakes, swamps and any other water outside beaches, rivers and oceans give the worst:

<BiomeOdds />

Server owners can change which blocks give stones, and the odds per biome, in the [configuration](../configuration#pickup).
