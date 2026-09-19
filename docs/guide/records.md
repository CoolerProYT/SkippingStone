# Records

Your best number of skips and your best distance are saved for every player, per world.

- **Skips** counts the skips a throw actually made.
- **Distance** is measured from the first skip to the last one.

Only throws released in the green or yellow count. A red release, or a throw that never skips, is not recorded.

## Breaking a record

When a throw passes your previous best skip count or distance, a firework goes off over the stone mid-throw, and the result shows as **New record!** in gold.

Your very first counted throw just sets your record, so there is no firework until you have a record to beat.

## Checking records

### Statistics screen

Open **Esc → Statistics → General**. Your records are the **Best Stone Skips** and **Best Stone Skip Distance** rows.

### Command

```
/skippingstone records
/skippingstone records <player>
/skippingstone records top
/skippingstone records top distance
```

The first shows your own records, the second another player's (including offline players), and the last two a top 10 leaderboard. See [Commands](../commands).

### Scoreboard

The mod keeps two scoreboard objectives up to date with everyone's best, including offline players:

| Objective | Shows |
| --- | --- |
| `skippingstone.best_skips` | Best skips |
| `skippingstone.best_distance` | Best distance, in whole blocks |

Show one in the sidebar with:

```
/scoreboard objectives setdisplay sidebar skippingstone.best_skips
```

The objectives appear after the first counted throw in a world. Server owners can turn them off in the [configuration](../configuration#records).
