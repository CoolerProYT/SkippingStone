<script setup lang="ts">
import { computed } from 'vue'
import { data } from '../skippingstone'

// Settings read from the mod's config code by the sync script; `section` is the part before the first dot.
const props = defineProps<{ section: string }>()

const rows = computed(() =>
  Object.entries(data.settings)
    .filter(([path]) => path.startsWith(`${props.section}.`))
    .map(([path, value]) => ({ path, key: path.slice(props.section.length + 1), ...value })),
)
</script>

<template>
  <table class="ss-config">
    <thead>
      <tr>
        <th>Key</th>
        <th>Default</th>
        <th>Range</th>
        <th>What it does</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="row in rows" :key="row.path">
        <td><code>{{ row.key }}</code></td>
        <td><code>{{ row.default }}</code></td>
        <td class="range">{{ row.min === null ? '' : `${row.min} – ${row.max}` }}</td>
        <td>{{ row.comment }}</td>
      </tr>
    </tbody>
  </table>
</template>

<style scoped>
.range {
  white-space: nowrap;
}
</style>
