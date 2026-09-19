<script setup lang="ts">
import { computed } from 'vue'

// Side view of a throw: one arc per skip, each as long as its distance. Arc heights follow the mod's HopPhysics,
// where longer hops launch higher (0.08 + 0.035 per block, clamped to 0.12..0.42).
const props = defineProps<{ skips: number[] }>()

const WIDTH = 640
const HEIGHT = 120
const WATER = HEIGHT - 16

const layout = computed(() => {
  const total = props.skips.reduce((a, b) => a + b, 0)
  const scale = total > 0 ? (WIDTH - 24) / Math.max(total, 20) : 1
  let x = 12
  const arcs = props.skips.map((distance) => {
    const lift = Math.min(Math.max(0.08 + 0.035 * distance, 0.12), 0.42)
    const width = distance * scale
    const height = Math.min(lift * 190, WATER - 8)
    const path = `M ${x} ${WATER} Q ${x + width / 2} ${WATER - height * 2} ${x + width} ${WATER}`
    const start = x
    x += width
    return { path, start }
  })
  return { arcs, end: x, scale }
})
</script>

<template>
  <svg class="ss-arcs" :viewBox="`0 0 ${WIDTH} ${HEIGHT}`" role="img" :aria-label="`${skips.length} skips`">
    <rect x="0" :y="WATER" :width="WIDTH" :height="HEIGHT - WATER" class="water" />
    <path v-for="(arc, i) in layout.arcs" :key="i" :d="arc.path" class="arc" />
    <g v-for="(arc, i) in layout.arcs" :key="`r${i}`">
      <ellipse :cx="arc.start" :cy="WATER" :rx="4 + 10 * (skips[i] / (skips[0] || 1))" ry="2.5" class="ripple" />
    </g>
    <circle v-if="skips.length" :cx="layout.end" :cy="WATER + 6" r="3.5" class="sunk" />
    <text v-else x="12" :y="WATER - 10" class="note">Sinks on contact</text>
  </svg>
</template>

<style scoped>
.ss-arcs {
  display: block;
  width: 100%;
  height: auto;
}

.water {
  fill: var(--ss-water);
}

.arc {
  fill: none;
  stroke: var(--vp-c-text-2);
  stroke-width: 1.5;
  stroke-dasharray: 4 3;
}

.ripple {
  fill: none;
  stroke: #fff;
  stroke-width: 1.2;
  opacity: 0.85;
}

.sunk {
  fill: var(--vp-c-text-3);
}

.note {
  fill: var(--vp-c-text-2);
  font-size: 13px;
}
</style>
