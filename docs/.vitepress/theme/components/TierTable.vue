<script setup lang="ts">
import { blocks, calculate, data, meter, percent, setting, tierIcon } from '../skippingstone'
import ItemSlot from './ItemSlot.vue'
import MeterBar from './MeterBar.vue'

// Results at the ideal angle, so the table shows what each tier is capable of.
const idealPitch = setting('throw.idealAngle')
const rows = data.tiers.map((tier, index) => ({
  tier,
  index,
  green: calculate('green', index, idealPitch),
  yellow: calculate('yellow', index, idealPitch),
}))
</script>

<template>
  <table class="ss-tier-table">
    <thead>
      <tr>
        <th>Stone</th>
        <th>Power meter</th>
        <th>Speed</th>
        <th>Green release</th>
        <th>Yellow release</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="row in rows" :key="row.tier.name">
        <td><ItemSlot :id="tierIcon(row.index)" :name="row.tier.displayName" label /></td>
        <td class="meter">
          <MeterBar :tier="row.index" compact />
          <span class="ss-muted">green {{ percent(meter(row.index).green) }}</span>
        </td>
        <td>×{{ row.tier.velocityMultiplier }}</td>
        <td><strong>{{ row.green.skips.length }}</strong> skips, {{ blocks(row.green.total) }} blocks</td>
        <td>{{ row.yellow.skips.length }} skips, {{ blocks(row.yellow.total) }} blocks</td>
      </tr>
    </tbody>
  </table>
</template>

<style scoped>
.ss-tier-table td {
  vertical-align: middle;
}

.meter {
  min-width: 150px;
}

.meter .ss-muted {
  display: block;
  margin-top: 4px;
}
</style>
