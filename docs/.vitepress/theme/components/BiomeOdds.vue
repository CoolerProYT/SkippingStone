<script setup lang="ts">
import { biomeTagName, data, percent, tierChances, tierIcon } from '../skippingstone'
import ItemSlot from './ItemSlot.vue'

const rows = [
  ...data.biomeTierWeights.map((entry) => ({ name: biomeTagName(entry.biomeTag), tag: `#${entry.biomeTag}`, chances: tierChances(entry.weights) })),
  { name: 'Everywhere else', tag: '', chances: tierChances(data.defaultTierWeights) },
]
const best = data.tiers.length - 1
const maxBest = Math.max(...rows.map((row) => row.chances[best]))
</script>

<template>
  <table class="ss-biome-odds">
    <thead>
      <tr>
        <th>Where</th>
        <th v-for="(tier, i) in data.tiers" :key="tier.name" class="tier">
          <ItemSlot :id="tierIcon(i)" :name="tier.displayName" />
          <span>{{ tier.displayName }}</span>
        </th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="row in rows" :key="row.name">
        <td>
          {{ row.name }}
          <code v-if="row.tag" class="tag">{{ row.tag }}</code>
        </td>
        <td v-for="(chance, i) in row.chances" :key="i" :class="{ best: i === best && chance === maxBest }">
          <span class="bar" :style="{ width: `${chance * 100}%` }" />
          {{ percent(chance) }}
        </td>
      </tr>
    </tbody>
  </table>
</template>

<style scoped>
.tier {
  text-align: center;
}

.tier span {
  display: block;
  font-size: 12px;
  font-weight: 500;
}

.ss-biome-odds td {
  position: relative;
  vertical-align: middle;
}

.bar {
  position: absolute;
  left: 0;
  bottom: 0;
  height: 3px;
  background: var(--vp-c-brand-1);
  opacity: 0.5;
}

.tag {
  display: block;
  margin-top: 2px;
  font-size: 12px;
}

.best {
  color: var(--vp-c-brand-1);
  font-weight: 700;
}
</style>
