<script setup lang="ts">
import { computed, ref } from 'vue'
import { blocks, calculate, data, percent, setting, tierIcon, type Zone } from '../skippingstone'
import ItemSlot from './ItemSlot.vue'
import SkipArcs from './SkipArcs.vue'

// The mod's throw formula with every input exposed, showing each intermediate value.
const tier = ref(data.tiers.length - 1)
const zone = ref<Zone>('green')
const pitch = ref(setting('throw.idealAngle'))

const result = computed(() => calculate(zone.value, tier.value, pitch.value))
const firstSkip = computed(() => result.value.baseVelocity * result.value.angleEfficiency * setting('throw.baseDistance'))
const pitchLabel = computed(() => {
  if (pitch.value === 0) return 'level with the horizon'
  return `${Math.abs(pitch.value)}° ${pitch.value > 0 ? 'below' : 'above'} the horizon`
})

const zones: { value: Zone; label: string }[] = [
  { value: 'green', label: 'Green' },
  { value: 'yellow', label: 'Yellow' },
  { value: 'red', label: 'Red' },
]
</script>

<template>
  <div class="ss-calc">
    <div class="inputs">
      <label>
        Stone
        <select v-model.number="tier">
          <option v-for="(t, i) in data.tiers" :key="t.name" :value="i">{{ t.displayName }} (×{{ t.velocityMultiplier }})</option>
        </select>
      </label>
      <fieldset class="zones">
        <legend>Released in</legend>
        <label v-for="z in zones" :key="z.value" :class="[z.value, { active: zone === z.value }]">
          <input v-model="zone" type="radio" name="ss-zone" :value="z.value" />
          {{ z.label }}
        </label>
      </fieldset>
      <label class="pitch">
        Aim: {{ pitchLabel }}
        <input v-model.number="pitch" type="range" min="-90" max="90" step="1" />
      </label>
    </div>

    <div class="summary">
      <ItemSlot :id="tierIcon(tier)" :name="data.tiers[tier].displayName" />
      <div>
        <div class="big">{{ result.skips.length }} skips · {{ blocks(result.total) }} blocks</div>
        <div class="ss-muted">{{ result.skips.length ? `first skip ${blocks(firstSkip)} blocks` : 'the stone sinks' }}</div>
      </div>
    </div>

    <SkipArcs :skips="result.skips" />

    <table class="steps">
      <tbody>
        <tr>
          <td>Power factor</td>
          <td><code>{{ result.powerFactor }}</code></td>
          <td class="ss-muted">from the meter zone</td>
        </tr>
        <tr>
          <td>Angle efficiency</td>
          <td><code>{{ percent(result.angleEfficiency) }}</code></td>
          <td class="ss-muted">1 − |{{ setting('throw.idealAngle') }} − {{ pitch < 0 ? `(${pitch})` : pitch }}| ÷ {{ setting('throw.angleTolerance') }}</td>
        </tr>
        <tr>
          <td>Base velocity</td>
          <td><code>{{ result.baseVelocity.toFixed(3) }}</code></td>
          <td class="ss-muted">{{ result.powerFactor }} × {{ data.tiers[tier].velocityMultiplier }}</td>
        </tr>
        <tr>
          <td>First skip</td>
          <td><code>{{ blocks(firstSkip) }}</code></td>
          <td class="ss-muted">{{ result.baseVelocity.toFixed(3) }} × {{ result.angleEfficiency.toFixed(2) }} × {{ setting('throw.baseDistance') }}</td>
        </tr>
        <tr>
          <td>Every next skip</td>
          <td><code>× {{ setting('throw.decayRate') }}</code></td>
          <td class="ss-muted">until a skip would be shorter than {{ setting('throw.minSkipThreshold') }} blocks</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.ss-calc {
  margin: 16px 0;
  padding: 16px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 12px;
  background: var(--vp-c-bg-soft);
}

.inputs {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 24px;
  align-items: flex-end;
  font-size: 14px;
}

.inputs > label {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

select {
  padding: 4px 8px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 6px;
  background: var(--vp-c-bg);
}

.zones {
  display: flex;
  gap: 6px;
  margin: 0;
  padding: 0;
  border: 0;
}

.zones legend {
  margin-bottom: 6px;
  padding: 0;
}

.zones label {
  padding: 4px 12px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 999px;
  cursor: pointer;
}

.zones input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.zones label:focus-within {
  outline: 2px solid var(--vp-c-brand-1);
  outline-offset: 2px;
}

.zones label.active.green {
  border-color: var(--ss-green);
  background: color-mix(in srgb, var(--ss-green) 20%, transparent);
}

.zones label.active.yellow {
  border-color: var(--ss-yellow);
  background: color-mix(in srgb, var(--ss-yellow) 25%, transparent);
}

.zones label.active.red {
  border-color: var(--ss-red);
  background: color-mix(in srgb, var(--ss-red) 20%, transparent);
}

.pitch {
  flex: 1;
  min-width: 220px;
}

.summary {
  display: flex;
  gap: 12px;
  align-items: center;
  margin: 16px 0 8px;
}

.big {
  font-size: 20px;
  font-weight: 700;
}

.steps {
  margin: 12px 0 0;
  font-size: 14px;
}

.steps td {
  padding: 6px 12px;
}
</style>
