import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import BiomeOdds from './components/BiomeOdds.vue'
import ConfigTable from './components/ConfigTable.vue'
import ItemSlot from './components/ItemSlot.vue'
import MeterBar from './components/MeterBar.vue'
import PowerMeterDemo from './components/PowerMeterDemo.vue'
import SkipArcs from './components/SkipArcs.vue'
import ThrowCalculator from './components/ThrowCalculator.vue'
import TierTable from './components/TierTable.vue'
import './style.css'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('BiomeOdds', BiomeOdds)
    app.component('ConfigTable', ConfigTable)
    app.component('ItemSlot', ItemSlot)
    app.component('MeterBar', MeterBar)
    app.component('PowerMeterDemo', PowerMeterDemo)
    app.component('SkipArcs', SkipArcs)
    app.component('ThrowCalculator', ThrowCalculator)
    app.component('TierTable', TierTable)
  },
} satisfies Theme
