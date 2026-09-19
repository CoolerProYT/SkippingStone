// @ts-ignore
import raw from '../data/data.json'

export interface Tier {
  name: string
  displayName: string
  velocityMultiplier: number
}

export interface Setting {
  type: 'int' | 'double' | 'boolean'
  default: number | boolean
  min: number | null
  max: number | null
  comment: string
}

export interface BiomeWeights {
  biomeTag: string
  weights: number[]
}

export const data = raw as unknown as {
  tiers: Tier[]
  settings: Record<string, Setting>
  blockConversions: Record<string, string>
  defaultTierWeights: number[]
  biomeTierWeights: BiomeWeights[]
  names: Record<string, string>
  textures: Record<string, string>
  lang: Record<string, string>
}

export function setting(path: string): number {
  return data.settings[path].default as number
}

export type Zone = 'green' | 'yellow' | 'red'

// Port of the mod's pure throw logic (throwing/logic/*.java). Keep in step with the Java when the formula changes.

/** PowerMeter.greenWidthForTier: narrows linearly from the lowest tier's width to the highest's. */
export function greenWidth(tier: number): number {
  const count = data.tiers.length
  const lowest = setting('meter.greenWidthLowestTier')
  const highest = setting('meter.greenWidthHighestTier')
  if (count <= 1) return lowest
  const t = Math.min(Math.max(tier, 0), count - 1) / (count - 1)
  return lowest + (highest - lowest) * t
}

/** ModCommonConfig.meterForTier: yellow is shrunk if the bands would not fit. */
export function meter(tier: number) {
  const green = greenWidth(tier)
  const yellow = Math.min(setting('meter.yellowWidth'), (1 - green) / 2)
  const greenStart = 0.5 - green / 2
  return { green, yellow, greenStart, yellowStart: greenStart - yellow }
}

/** PowerMeter.position: a triangle wave from 0 to 1 and back over one period. */
export function meterPosition(chargeTicks: number): number {
  const period = setting('meter.periodTicks')
  const phase = (Math.max(chargeTicks, 0) % period) / period
  return phase < 0.5 ? phase * 2 : 2 - phase * 2
}

export function zoneAt(tier: number, position: number): Zone {
  const { green, yellow } = meter(tier)
  const fromCentre = Math.abs(position - 0.5)
  if (fromCentre <= green / 2) return 'green'
  if (fromCentre <= green / 2 + yellow) return 'yellow'
  return 'red'
}

export function powerFactor(zone: Zone): number {
  if (zone === 'green') return setting('meter.greenPowerFactor')
  if (zone === 'yellow') return setting('meter.yellowPowerFactor')
  return 0
}

export function angleEfficiency(pitch: number): number {
  const deviation = Math.abs(setting('throw.idealAngle') - pitch) / setting('throw.angleTolerance')
  return 1 - Math.min(Math.max(deviation, 0), 1)
}

export interface ThrowResult {
  zone: Zone
  powerFactor: number
  angleEfficiency: number
  baseVelocity: number
  skips: number[]
  total: number
}

/** StoneThrowCalculator.calculate */
export function calculate(zone: Zone, tier: number, pitch: number): ThrowResult {
  const power = powerFactor(zone)
  const efficiency = angleEfficiency(pitch)
  const multiplier = data.tiers[Math.min(Math.max(tier, 0), data.tiers.length - 1)].velocityMultiplier
  const baseVelocity = power * multiplier
  const skips: number[] = []
  if (zone !== 'red') {
    let distance = baseVelocity * efficiency * setting('throw.baseDistance')
    while (distance > setting('throw.minSkipThreshold') && skips.length < 10_000) {
      skips.push(distance)
      distance *= setting('throw.decayRate')
    }
  }
  return { zone, powerFactor: power, angleEfficiency: efficiency, baseVelocity, skips, total: skips.reduce((a, b) => a + b, 0) }
}

/** Chance of each tier from a set of roll weights. Missing trailing weights count as 0. */
export function tierChances(weights: number[]): number[] {
  const used = data.tiers.map((_, i) => Math.max(weights[i] ?? 0, 0))
  const total = used.reduce((a, b) => a + b, 0)
  return used.map((w) => (total === 0 ? 0 : w / total))
}

export function percent(fraction: number): string {
  const value = Math.round(fraction * 1000) / 10
  return `${Number.isInteger(value) ? value : value.toFixed(1)}%`
}

export function blocks(distance: number): string {
  return distance.toFixed(1)
}

/** Readable name for a vanilla or mod id. */
export function itemName(id: string): string {
  if (data.names[id]) return data.names[id]
  const path = id.split(':').pop() ?? id
  return path
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}

/** Readable name for a biome tag such as minecraft:is_beach. */
export function biomeTagName(tag: string): string {
  const path = (tag.split(':').pop() ?? tag).replace(/^is_/, '')
  const name = path.charAt(0).toUpperCase() + path.slice(1).replace(/_/g, ' ')
  return name.endsWith('s') ? name : `${name}s`
}

/** Hosted renders of vanilla items, one PNG per item id. Mojang's textures are not bundled here. */
const VANILLA_ICONS = 'https://storage.googleapis.com/coolerpromc/textures'

/** Sifted blocks look like the block they came from, only shorter, so they borrow its icon. */
const ICON_ALIASES: Record<string, string> = Object.fromEntries(
  Object.entries(data.blockConversions).map(([from, to]) => [to, from]),
)

export function itemIcon(id: string): string | null {
  const itemId = ICON_ALIASES[id] ?? id
  if (data.textures[itemId]) return data.textures[itemId]
  const [namespace, path] = itemId.includes(':') ? itemId.split(':') : ['minecraft', itemId]
  if (namespace !== 'minecraft') return null
  return `${VANILLA_ICONS}/${namespace}/${path}.png`
}

/** The item model picks a texture by tier index (not name), falling back to the first for extra config tiers. */
const TIER_TEXTURES = ['chipped', 'rough', 'smooth', 'perfect']

export function tierIcon(tier: number): string {
  return `skippingstone:skipping_stone_${TIER_TEXTURES[tier] ?? TIER_TEXTURES[0]}`
}
