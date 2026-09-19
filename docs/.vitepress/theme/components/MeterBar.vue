<script setup lang="ts">
import { computed } from 'vue'
import { meter } from '../skippingstone'

// Static drawing of a tier's power meter, laid out like the in-game bar: red | yellow | green | yellow | red.
const props = withDefaults(defineProps<{ tier: number; position?: number | null; compact?: boolean }>(), {
  position: null,
  compact: false,
})

const bands = computed(() => {
  const m = meter(props.tier)
  const pct = (fraction: number) => `${fraction * 100}%`
  return [
    { zone: 'red', width: pct(m.yellowStart) },
    { zone: 'yellow', width: pct(m.yellow) },
    { zone: 'green', width: pct(m.green) },
    { zone: 'yellow', width: pct(m.yellow) },
    { zone: 'red', width: pct(m.yellowStart) },
  ]
})
</script>

<template>
  <div class="ss-meter" :class="{ compact }" role="img" :aria-label="`Power meter, green zone ${Math.round(meter(tier).green * 100)}% of the bar`">
    <span v-for="(band, i) in bands" :key="i" :class="band.zone" :style="{ width: band.width }" />
    <span v-if="position !== null" class="marker" :style="{ left: `${position * 100}%` }" />
  </div>
</template>

<style scoped>
.ss-meter {
  position: relative;
  display: flex;
  height: 14px;
  padding: 2px;
  border-radius: 3px;
  background: #1e1e1e;
  box-shadow: inset 0 0 0 1px #000;
}

.ss-meter.compact {
  height: 10px;
  min-width: 120px;
}

.ss-meter > span:not(.marker) {
  height: 100%;
}

.red {
  background: var(--ss-red);
}

.yellow {
  background: var(--ss-yellow);
}

.green {
  background: var(--ss-green);
}

.marker {
  position: absolute;
  top: -4px;
  bottom: -4px;
  width: 3px;
  margin-left: -1.5px;
  background: #fff;
  box-shadow: 0 0 0 1px #000;
}
</style>
