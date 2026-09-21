# Gameoverse Difficulty Hearts

Small server-side Fabric mod for Gameoverse, bridging the server's own
Heart Crystal system (a datapack: consume a crystal for +1 max heart,
uncapped; lose one on death, floored at 3) into two other mods already
running on the server.

## What this does

- **Mob difficulty scales with hearts.** Implements
  [Dynamic Difficulty](https://modrinth.com/mod/dynamic-difficulty-mod)'s
  `PlayerLevelProvider` API: nearby mob level shifts by 1 for every 4
  hearts a player has above (or below) the starting baseline of 6, using
  Dynamic Difficulty's own default average-aggregation across nearby
  players.
- **More hearts, more loot.** Two independent, non-wrapping hooks (kills,
  chests, general block breaks) boost loot quantity (a virtual bonus to
  the killer's effective Looting enchantment level, without touching
  their weapon) and the chance an [Apotheosis](https://github.com/WasabiIceCream/apotheosis-fabric)
  affix item drops at all. Deliberately never touches item *rarity* or a
  player's Apotheosis World Tier — those stay earned through normal
  progression, only drop *frequency* scales with hearts.
- **A rare, flat-chance Heart Crystal drop** from almost every action
  (kills, chest opens, harvesting a mature crop, breaking most other
  blocks), as a discovery path for players who haven't crafted one yet.
  Immature crops and player-placed blocks are excluded (a small
  attachment-based tracker records placement, so a "place a cheap block,
  instantly break it" loop can't farm it). Rates are configurable via
  `config/gameoverse_difficulty_hearts.json`, no rebuild needed.

## Why it's a mod and not entirely a datapack

The Heart Crystal item and its gain/lose-on-death mechanic are pure
datapack (no mod needed at all for that part). This mod exists only for
the pieces that need real code: Dynamic Difficulty's Java API has no
data-driven equivalent, and the loot/Looting hooks need mixins into
vanilla and Apotheosis internals that a datapack can't reach.

## A real bug, found and fixed in the open

An early version (`v1.3.0`) tried to add the rare-drop feature by
independently wrapping the same `LootTable#getRandomItemsRaw` consumer
Apotheosis's own loot-modifier system already wraps, assuming two
independent reentrancy-safe wrap/flush pairs on the same method would
compose safely. That assumption was wrong — it broke **every block drop
on the server**, caught live within the same session, reverted, and
rebuilt (`v1.4.0`) using three separate, non-wrapping hooks instead
(`ServerLivingEntityEvents.AFTER_DEATH` for kills, the existing
block-break event for blocks, a plain `@Inject` on
`RandomizableContainer`'s own loot-unpack method for chests). Kept in
git history rather than squashed, since it's a useful real-world example
of why two mods wrapping the same consumer parameter isn't automatically
safe.

## License

MIT.
