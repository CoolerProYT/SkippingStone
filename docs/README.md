# Skipping Stone wiki

VitePress site for the mod. Stone qualities, biome odds, throw tuning and config descriptions are read from the mod itself, so the wiki follows the mod's defaults automatically.

```bash
cd docs
npm install
npm run dev     # syncs data, then serves http://localhost:5173/SkippingStone/
npm run build   # syncs data, then builds to .vitepress/dist
```

`npm run sync` (run automatically by `dev` and `build`) reads the config defaults from `common/src/main/java/com/coolerpromc/skippingstone/config/ModCommonConfig.java` and names from the mod's `en_us.json`, and writes `.vitepress/data/data.json` (git-ignored). If the config code changes shape and a value can no longer be found, the sync fails instead of publishing stale numbers.

The throw calculator and power meter demo use a TypeScript port of the mod's throw logic in `.vitepress/theme/skippingstone.ts`. Update it when the formula in `throwing/logic/` changes.

Icons load from the hosted textures at `https://storage.googleapis.com/coolerpromc/textures/`: vanilla under `minecraft/`, this mod's under `skippingstone/` (1024x1024, nearest-neighbour). When a texture is added or changed in the mod, upload it there before building, or the wiki shows the item's initials instead. Sifted blocks reuse the render of the block they came from.

The site is published by `.github/workflows/docs.yml`.
