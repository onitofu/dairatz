# Changelog

## 2.0.0 (unreleased)

- Added Jungle, Cherry, Pale, and Ender fairies with species-specific spawn eggs and projectiles.
- Added two persistent cosmetic appearances each for Jungle and Pale fairies.
- Added subtle flight particles matching each fairy species.
- Added species-specific idle habits, resting poses, night patrol posts, and safe Ender curiosity blinks.
- Cherry residents now approach their patients before healing; idle behavior yields to combat and useful work.
- Kept original fairies and items first in registration and creative tabs, with standard item tooltips.
- Added spruce, cherry, jungle, and pale oak fairy hollows with species-specific residents.
- Jungle fairies poison enemies, reduce new poison duration while mounted, and tend cocoa and berry bushes.
- Cherry fairies heal their owner and owned pets outside combat, with shared healing cooldowns.
- Pale fairies apply weakness and privately reveal nearby threats to their owner; residents patrol at night.
- Ender fairies attract their owner's mined and hunted loot while mounted and evade melee hits after taking damage.
- Added persistent loot provenance, bounded private threat snapshots, and configuration sections for new species.
- Preserved existing entity identifiers, oak hollow saves, and resident recovery data.

- Added oak fairy hollows that generate in natural oak trees in new Overworld chunks.
- Regular fairies can live in hollows, shelter at night or in rain, and pollinate nearby plants.
- Fairies carry pollen from flowers to immature crops, with particles showing their work and successful growth.
- Pollination no longer spreads grass or duplicates decorative flowers, and a single garden flower is found reliably.
- Added occupied and empty hollow appearances, resident interaction messages, and Silk Touch drops.
- Preserved fairy identity, ownership, names, and health when entering or leaving a hollow.
- Added persistent recovery when a blocked entrance prevents a resident from escaping a broken hollow.
- Fixed fairy teleportation and winter freezing to respect loaded chunks and safe placement.
- Validated configuration values and made configuration updates atomic.
- Added strict Checkstyle checks and automated tests for hollow behavior, rendering, and world restarts.

## 1.1.1 (unreleased)

- Added Fabric, Forge, and NeoForge support for Minecraft 1.21.11.
- Updated the spawn egg textures and mod logo.
- Added a unique texture and creative inventory item for the Ice Furball.
- Ice Furballs deal extra damage to fire-based mobs.
- Fairies now chase enemies, circle them during combat, and try to dodge attacks.
- Added Japanese, Simplified Chinese, Ukrainian, and Kazakh translations.
- Fixed an incorrect experimental features warning when creating worlds on Forge.

## 1.1

- **Winter Fairy**: Added a new fairy variant that spawns in snowy biomes, tamed with sweet berries.
- **Frostball projectile**: Winter Fairy shoots snowballs that deal damage and apply Slowness II.
- **Water freezing**: Winter Fairy freezes nearby water into frosted ice; acts as Frost Walker when on the player's head.
- **Fire protection**: Winter Fairy extinguishes fire on the owner when sitting on their head.
- **Throw sound**: Fairies now play a snowball throw sound when shooting projectiles.
- **Target reset**: Fairies drop their target when the owner moves far away.
- **Hard teleport**: Fairies teleport directly to the owner when too far apart.
- **No fairy-on-fairy aggression**: Fairies no longer target or attack other fairies.
- **Owner protection fix**: Fairy projectiles no longer harm their owner.
- **Extended configuration**: Added configurable spawn parameters (weight, group size), heal amount, tame chance, and all Winter Fairy parameters (health, damage, fire rate, flight speed, slowness, freeze radius).

## 1.0

- **Flight physics**: Fairies now fly smoothly.
- **Flower pollination**: Wild (untamed) fairies pollinate nearby flowers, promoting crop and plant growth.
- **Head mount limit**: Only one fairy can sit on the player's head at a time.
- **Friendly fire protection**: Tamed fairies no longer damage their owner or other fairies with furball projectiles.
- **Expanded spawn biomes**: Fairies now spawn naturally in Flower Forest, Meadow, and Sunflower Plains.
