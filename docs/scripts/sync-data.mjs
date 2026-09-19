// Pulls wiki data straight from the mod so the docs never drift from the game.
// Skipping Stone has no datagen: its tiers, biome odds and throw tuning are the config defaults in
// ModCommonConfig.java, so they are read from that file. Names come from the lang file, icons from the item textures.
// Every value is required: if the config code changes shape, this fails loudly instead of publishing stale numbers.
import { mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs'
import { basename, dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const docs = join(dirname(fileURLToPath(import.meta.url)), '..')
const root = join(docs, '..')
const configSource = join(root, 'common/src/main/java/com/coolerpromc/skippingstone/config/ModCommonConfig.java')
const assets = join(root, 'common/src/main/resources/assets/skippingstone')

const source = readFileSync(configSource, 'utf8')
const lang = JSON.parse(readFileSync(join(assets, 'lang/en_us.json'), 'utf8'))

function fail(message) {
  console.error(`sync-data: ${message} in ${configSource}`)
  process.exit(1)
}

/** The source text between `start` and the matching closing parenthesis of the call that follows it. */
function callBody(start) {
  const at = source.indexOf(start)
  if (at < 0) fail(`could not find ${start}`)
  let depth = 0
  for (let i = source.indexOf('(', at); i < source.length; i++) {
    if (source[i] === '(') depth++
    else if (source[i] === ')' && --depth === 0) return source.slice(at, i + 1)
  }
  fail(`unbalanced parentheses after ${start}`)
}

const intList = (text) => text.split(',').map((n) => Number.parseInt(n.trim(), 10))

// Tiers, lowest first
const tiers = [...callBody('DEFAULT_TIERS = List.of').matchAll(/new StoneTierConfig\("([^"]+)",\s*([-\d.]+)\)/g)].map(([, name, multiplier]) => ({
  name,
  displayName: lang[`stone_tier.skippingstone.${name}`] ?? name,
  velocityMultiplier: Number(multiplier),
}))
if (tiers.length === 0) fail('no StoneTierConfig defaults found')

// Plain define{Int,Double,Boolean}(path, default, [min, max,] "comment")
const settings = {}
const definePattern = /builder\.define(Int|Double|Boolean)\(\s*"([^"]+)",\s*([^,]+?),\s*(?:([^,]+?),\s*([^,]+?),\s*)?"((?:[^"\\]|\\.)*)"\s*\)/g
for (const [, type, path, value, min, max, comment] of source.matchAll(definePattern)) {
  const parse = (text) => (type === 'Boolean' ? text.trim() === 'true' : Number(text))
  settings[path] = {
    type: type.toLowerCase(),
    default: parse(value),
    min: min === undefined ? null : parse(min),
    max: max === undefined ? null : parse(max),
    comment: comment.replace(/\\(.)/g, '$1'),
  }
}

const required = [
  'meter.periodTicks',
  'meter.greenWidthLowestTier',
  'meter.greenWidthHighestTier',
  'meter.yellowWidth',
  'meter.greenPowerFactor',
  'meter.yellowPowerFactor',
  'throw.idealAngle',
  'throw.angleTolerance',
  'throw.baseDistance',
  'throw.decayRate',
  'throw.minSkipThreshold',
]
for (const path of required) {
  if (!(path in settings)) fail(`missing default for ${path}`)
}

// Structured (codec) settings
const conversionsBody = callBody('BLOCK_CONVERSIONS = builder.defineCodec')
const blockConversions = Object.fromEntries(
  [...conversionsBody.slice(conversionsBody.indexOf('Map.of')).matchAll(/"([a-z0-9_.-]+:[a-z0-9_/.-]+)",\s*"([a-z0-9_.-]+:[a-z0-9_/.-]+)"/g)].map(([, from, to]) => [from, to]),
)
if (Object.keys(blockConversions).length === 0) fail('no pickup.blockConversions defaults found')

const defaultWeightsMatch = callBody('DEFAULT_TIER_WEIGHTS = builder.defineCodec').match(/List\.of\(([\d,\s]+)\)/)
if (!defaultWeightsMatch) fail('no pickup.defaultTierWeights default found')
const defaultTierWeights = intList(defaultWeightsMatch[1])

const biomeTierWeights = [
  ...callBody('BIOME_TIER_WEIGHTS = builder.defineCodec').matchAll(/new BiomeTierWeights\(Identifier\.withDefaultNamespace\("([^"]+)"\),\s*List\.of\(([\d,\s]+)\)\)/g),
].map(([, tag, weights]) => ({ biomeTag: `minecraft:${tag}`, weights: intList(weights) }))
if (biomeTierWeights.length === 0) fail('no pickup.biomeTierWeights defaults found')

// Item and block names for mod content
const names = {}
for (const [key, value] of Object.entries(lang)) {
  const match = key.match(/^(item|block)\.skippingstone\.([a-z0-9_]+)$/)
  if (match) names[`skippingstone:${match[2]}`] = value
}

// Mod textures are hosted at 1024x1024 alongside the vanilla renders; upload new ones there before syncing.
// Sifted blocks have no texture of their own and reuse the hosted render of the block they came from.
const HOSTED_TEXTURES = 'https://storage.googleapis.com/coolerpromc/textures/skippingstone'
const textures = {}
for (const file of readdirSync(join(assets, 'textures/item')).filter((f) => f.endsWith('.png'))) {
  textures[`skippingstone:${basename(file, '.png')}`] = `${HOSTED_TEXTURES}/${file}`
}

const data = {
  tiers,
  settings,
  blockConversions,
  defaultTierWeights,
  biomeTierWeights,
  names,
  textures,
  lang: Object.fromEntries(Object.entries(lang).filter(([key]) => key.startsWith('stat.') || key.startsWith('objective.'))),
}

mkdirSync(join(docs, '.vitepress/data'), { recursive: true })
writeFileSync(join(docs, '.vitepress/data/data.json'), JSON.stringify(data, null, 2) + '\n')
console.log(`sync-data: ${tiers.length} tiers, ${Object.keys(settings).length} settings, ${biomeTierWeights.length} biome weight sets, ${Object.keys(textures).length} textures`)
