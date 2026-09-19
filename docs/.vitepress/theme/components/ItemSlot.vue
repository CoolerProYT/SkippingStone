<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { itemIcon, itemName } from '../skippingstone'

const props = withDefaults(defineProps<{ id?: string | null; name?: string; count?: number; label?: boolean }>(), {
  id: null,
  name: '',
  count: 1,
  label: false,
})

const displayName = computed(() => props.name || (props.id ? itemName(props.id) : ''))
const src = computed(() => (props.id ? itemIcon(props.id) : null))

// Falls back to initials when an item has no icon or the hosted icon fails to load.
const failed = ref(false)
watch(src, () => (failed.value = false))
const initials = computed(() =>
  displayName.value
    .split(' ')
    .filter((word) => /^[A-Z]/.test(word))
    .slice(0, 2)
    .map((word) => word[0])
    .join(''),
)
</script>

<template>
  <span class="ss-item" :class="{ 'with-label': label }">
    <span class="ss-slot" :title="displayName" :aria-label="displayName" role="img">
      <img v-if="src && !failed" class="pixelated" :src="src" alt="" loading="lazy" @error="failed = true" />
      <span v-else-if="id" class="ss-initials">{{ initials }}</span>
      <span v-if="count > 1" class="ss-count">{{ count }}</span>
    </span>
    <span v-if="label && id" class="ss-label">{{ displayName }}</span>
  </span>
</template>

<style scoped>
.ss-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  vertical-align: middle;
}

.ss-slot {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  flex: none;
  background: var(--ss-slot-bg);
  border: 2px solid;
  border-color: var(--ss-slot-dark) var(--ss-slot-light) var(--ss-slot-light) var(--ss-slot-dark);
}

.ss-slot img {
  width: 32px;
  height: 32px;
}

.ss-initials {
  font: 600 12px/1 var(--vp-font-family-mono);
  color: #fff;
  text-shadow: 1px 1px 0 #3f3f3f;
}

.ss-count {
  position: absolute;
  right: 1px;
  bottom: -1px;
  font: 700 12px/1 var(--vp-font-family-mono);
  color: #fff;
  text-shadow: 1px 1px 0 #3f3f3f;
}

.ss-label {
  font-weight: 500;
}
</style>
