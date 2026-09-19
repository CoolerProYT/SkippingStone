# The math

There is no fixed maximum distance. Every throw comes from a formula: your aim, your timing and the stone's quality go in, and a list of skip distances comes out. The stone then skips exactly those distances in the world.

## Try it

<ThrowCalculator />

## The formula

**1. Power** comes from where you released on the [power meter](./throwing#the-power-meter): {{ settings['meter.greenPowerFactor'] }} in the green, {{ settings['meter.yellowPowerFactor'] }} in the yellow, 0 in the red.

**2. Angle efficiency** measures how close your aim was to ideal:

```
angleEfficiency = 1 − clamp(|idealAngle − pitch| ÷ angleTolerance, 0, 1)
```

The ideal pitch (`idealAngle`) is **{{ settings['throw.idealAngle'] }}°**, slightly downward. Efficiency drops to zero {{ settings['throw.angleTolerance'] }}° (`angleTolerance`) away from it.

**3. Base velocity** combines your power with the stone's [speed](./stone-quality):

```
baseVelocity = power × qualityVelocityMultiplier(stone)
```

**4. The first skip**, where `baseDistance` is {{ settings['throw.baseDistance'] }} blocks:

```
firstSkip = baseVelocity × angleEfficiency × baseDistance
```

**5. Every skip after that** is `decayRate` ({{ settings['throw.decayRate'] }}) times the one before. The throw ends when the next skip would be shorter than `minSkipThreshold` ({{ settings['throw.minSkipThreshold'] }} blocks). The number of skips and the total distance fall out of that:

```
skip[i] = firstSkip × decayRate^i      while skip[i] > minSkipThreshold
```

A red release, or an aim {{ settings['throw.angleTolerance'] }}° or more off, gives a first skip of zero, so the stone sinks on contact.

## From numbers to motion

The stone is a real projectile with normal gravity and air drag. Each time it touches the water, the mod works out the launch speed that will land it exactly one skip distance further on. Longer skips launch higher, so a good throw starts with big arcs that shrink into quick little hops.

Every number above is a setting. See [Configuration](../configuration#throw).

<script setup>
import { data } from '../.vitepress/theme/skippingstone'
const settings = Object.fromEntries(Object.entries(data.settings).map(([k, v]) => [k, v.default]))
</script>
