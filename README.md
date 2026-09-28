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

## A second real bug: firing from the player's own storage chests

Found live 2026-09-25 - the user reported that after weeks of dungeon
crawling, they hadn't found a single Heart Crystal, despite the
mechanic being intended to be commonly obtainable. Root cause was two
compounding issues:

1. `chestChance` (`0.01`) was simply too low for a "should find roughly
   one per dungeon crawl" experience against a typical dungeon's small
   handful of loot chests. Raised to `0.1` (the user's own choice among
   a couple of options) in `config/gameoverse_difficulty_hearts.json`.
2. A real bug made the drop chance apply to *every chest a player ever
   opened*, not just genuine loot chests - diluting even the raised
   rate far below what it looked like on paper. `RandomizableContainerMixin`
   injected at `TAIL` of vanilla's own `unpackLootTable`, which runs
   unconditionally on *every* container open; only its own *internal*
   `getLootTable() != null` check gates whether real loot generation
   happens (a plain storage chest always has a null loot table, so it
   silently takes the no-op branch every time). A `TAIL` injection
   fires regardless of which branch ran, so the roll happened even
   when opening the player's own long-emptied home storage. Fixed by
   moving the injection to `HEAD` and checking `getLootTable() != null`
   there - the real pre-open state, read before vanilla's own body
   clears it via `setLootTable(null)` partway through.

## Peaceful mobs get no level (1.5.2)

Dynamic Difficulty's "cancel levels for passives" option only skips `Animal`s with no attack
damage, so fish, squid, bats, villagers, wandering traders and allays were still leveled. With
that option on, `PassiveMobLevelsMixin` extends it to any non-hostile mob with no attack damage,
plus anything in `#gameoverse_difficulty_hearts:peaceful` (the allay, which carries an
attack-damage attribute but never attacks). Mobs that fight back (wolves, bees, polar bears,
golems, goats, dolphins) keep their levels, and Dynamic Difficulty's own
`#dynamic_difficulty:passive_whitelist` still opts a mob back in. Checked with
`/dynamic_difficulty level get`: villager, cod, squid, bat, wandering trader, allay and cow
unleveled; wolf, zombie and iron golem leveled.

1.5.3 adds `StaleLevelMixin`: mobs leveled before 1.5.2 keep Dynamic Difficulty's persistent
level attachment, so `LevelingSystem.hasLevel` now answers "no" for any non-player that can't
have a level. That level is then never synced to clients, used for extra XP or loot, or matched by
level loot conditions. (The client-side "hide the nameplate of mobs with no level" part lives in
`gameoverse-content-fixes`, since this mod is server-only.)

## License

MIT.
