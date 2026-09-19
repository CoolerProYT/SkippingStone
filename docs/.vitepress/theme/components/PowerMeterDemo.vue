<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { blocks, calculate, data, meterPosition, setting, tierIcon, zoneAt, type ThrowResult } from '../skippingstone'
import ItemSlot from './ItemSlot.vue'
import MeterBar from './MeterBar.vue'
import SkipArcs from './SkipArcs.vue'

// Hold to charge, release to throw: the same sweep speed, zones and formula as the game, at the ideal angle.
const TICK_MS = 50

const tier = ref(Math.min(2, data.tiers.length - 1))
const charging = ref(false)
const position = ref(0)
const result = ref<ThrowResult | null>(null)

let startedAt = 0
let frame = 0

function currentPosition() {
  return meterPosition((performance.now() - startedAt) / TICK_MS)
}

// Frames only draw the marker; the release reads the clock directly, so a dropped frame never changes the result.
function tick() {
  position.value = currentPosition()
  frame = requestAnimationFrame(tick)
}

function start() {
  if (charging.value) return
  charging.value = true
  result.value = null
  startedAt = performance.now()
  frame = requestAnimationFrame(tick)
}

function release() {
  if (!charging.value) return
  charging.value = false
  cancelAnimationFrame(frame)
  position.value = currentPosition()
  result.value = calculate(zoneAt(tier.value, position.value), tier.value, setting('throw.idealAngle'))
}

function onKey(event: KeyboardEvent, down: boolean) {
  if (event.key !== ' ' && event.key !== 'Enter') return
  event.preventDefault()
  if (down) start()
  else release()
}

onBeforeUnmount(() => cancelAnimationFrame(frame))

const message = computed(() => {
  if (!result.value) return ''
  const { zone, skips, total } = result.value
  if (zone === 'red') return 'Red: the stone sinks straight away.'
  const label = zone === 'green' ? 'Green, full power' : 'Yellow, partial power'
  return `${label}: ${skips.length} skips, ${blocks(total)} blocks.`
})
</script>

<template>
  <div class="ss-demo">
    <div class="tiers" role="radiogroup" aria-label="Stone quality">
      <button
        v-for="(t, i) in data.tiers"
        :key="t.name"
        role="radio"
        :aria-checked="i === tier"
        :class="{ active: i === tier }"
        :disabled="charging"
        @click="tier = i; result = null"
      >
        <ItemSlot :id="tierIcon(i)" :name="t.displayName" />
        {{ t.displayName }}
      </button>
    </div>

    <MeterBar :tier="tier" :position="position" />

    <button
      class="hold"
      :class="{ charging }"
      @pointerdown.prevent="start"
      @pointerup="release"
      @pointerleave="release"
      @pointercancel="release"
      @keydown="onKey($event, true)"
      @keyup="onKey($event, false)"
    >
      {{ charging ? 'Let go to throw' : 'Hold to charge' }}
    </button>

    <p class="message" :class="result?.zone" aria-live="polite">{{ message }}</p>
    <SkipArcs v-if="result" :skips="result.skips" />
  </div>
</template>

<style scoped>
.ss-demo {
  margin: 16px 0;
  padding: 16px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 12px;
  background: var(--vp-c-bg-soft);
}

.tiers {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tiers button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 999px;
  font-size: 14px;
  color: var(--vp-c-text-2);
}

.tiers button.active {
  border-color: var(--vp-c-brand-1);
  background: var(--vp-c-brand-soft);
  color: var(--vp-c-brand-1);
  font-weight: 600;
}

.tiers button:disabled {
  cursor: not-allowed;
}

.tiers :deep(.ss-slot) {
  width: 28px;
  height: 28px;
}

.tiers :deep(.ss-slot img) {
  width: 24px;
  height: 24px;
}

.hold {
  display: block;
  width: 100%;
  margin-top: 16px;
  padding: 12px;
  border-radius: 8px;
  background: var(--vp-c-brand-1);
  color: var(--vp-c-white);
  font-weight: 600;
  touch-action: none;
  user-select: none;
}

.hold.charging {
  background: var(--vp-c-brand-3);
}

.message {
  min-height: 1.6em;
  margin: 12px 0 4px;
  font-weight: 600;
}

.message.green {
  color: var(--ss-green-text);
}

.message.yellow {
  color: var(--ss-yellow-text);
}

.message.red {
  color: var(--ss-red-text);
}
</style>
