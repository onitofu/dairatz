# Dairatz

A Minecraft mod that adds tameable flying fairy companions.

## Supported platforms

Release `2.0.0` targets Minecraft `1.21.11` and Java `21`:

| Loader | Minimum version | Artifact |
|---|---:|---|
| Fabric | Fabric Loader `0.18.4`, Fabric API `0.141.3+1.21.11` | `dairatz-2.0.0+mc1.21.11-fabric.jar` |
| Forge | Forge `61.1.7` | `dairatz-2.0.0+mc1.21.11-forge.jar` |
| NeoForge | NeoForge `21.11.42` | `dairatz-2.0.0+mc1.21.11-neoforge.jar` |

Forge and NeoForge use separate JAR files. A Forge artifact is not treated as an
automatically supported NeoForge artifact.

## Building

Build one target using the same interface as CI:

```bash
./gradlew buildTarget -Ptarget=1.21.11-fabric
./gradlew buildTarget -Ptarget=1.21.11-forge
./gradlew buildTarget -Ptarget=1.21.11-neoforge
```

The selected production JAR is staged in `build/ci/<target>/`. To build the complete
implemented matrix, run:

```bash
./gradlew buildAll
```

## Development and code style

The shared gameplay code is under `common/`; loader APIs belong only in `fabric/`,
`forge/`, or `neoforge/`. Strict style rules are defined in `checkstyle.xml`.
Checkstyle 14.3.0 runs separately on Java 21+, using a pinned standalone release
verified by SHA-256. Set `CHECKSTYLE_JAVA_HOME` to your Java 21+ installation, or
pass `-PcheckstyleJavaHome`; the Gradle property takes precedence.

```bash
export CHECKSTYLE_JAVA_HOME="$JAVA_HOME"
./gradlew verifyCommonIsolation :fabric:checkstyleMain :fabric:checkstyleClient \
  -Ptarget=1.21.11-fabric
```

## CI artifacts

The GitHub Actions workflow runs one matrix job per loader. Each job calls
`buildTarget`, creates `SHA256SUMS`, and uploads a loader-specific artifact retained
for 30 days. A `v*` tag attaches all JARs and checksums to a GitHub Release.

## Fairy hollows

Oak fairy hollows occasionally appear in the lower trunks of naturally generated
oaks in newly explored Overworld chunks. Half start with one wild fairy.

Use Silk Touch to collect an empty hollow; other tools drop an ordinary oak log.
Breaking an occupied hollow releases its resident. Place the hollow with an open
entrance beside flowers and crops, then right-click it with your regular fairy on
your head to settle her.

An ordinary resident tends plants within eight blocks horizontally and three vertically.
She gathers pollen from a flower, then flies to an immature crop and applies one
bonemeal growth step. Gold particles show pollen gathering and carrying; green
particles mark successful crop growth. Pollination affects bonemealable plants
in the crop block tag, including wheat, carrots, potatoes, beetroot, and stems.
It does not spread grass or duplicate decorative flowers. One flower anywhere
within the garden is enough; it does not have to touch the crop.
She stays by the garden while you travel,
rests inside at night or during rain, and resumes work the next dry morning.
As with other farms, work only happens while the relevant chunks are ticking.
The face on the block means she is currently inside; an empty-looking entrance
may still belong to a resident working outside. If an entity spawn is refused when
a hollow is destroyed, the resident is preserved in world data and retried only
while its origin chunk is loaded. Recovery keeps the resident UUID and never
puts its data into the dropped block item.

Right-click with an empty hand to knock and let the resident out for at least ten
seconds. Sneak-right-click with an empty hand to recall your resident onto your
head, freeing the home. Your head must be free, and an outside resident must be
loaded and within sixteen blocks. Each hollow accepts only its matching fairy species.

## Fairy species

| Species | Taming and healing item | Main abilities | Home |
|---|---|---|---|
| Ordinary | Poppy | Pollinates immature field crops | Oak |
| Winter | Sweet berries | Freezes water, extinguishes mounted owner, slowing projectiles | Spruce |
| Jungle | Cocoa beans | Poison projectiles; halves new poison duration while mounted; tends cocoa and sweet berries at home | Jungle |
| Cherry | Pink petals | Heals mounted owner or owned pets near home outside combat | Cherry |
| Pale | Resin clump | Weakness projectiles; private threat detection; nocturnal home patrol | Pale oak |
| Ender | Chorus fruit | Mounted loot attraction; safe evasive teleport after melee damage | None |

Jungle and Pale fairies have two equally likely cosmetic appearances. Appearance
is synchronized and survives world reloads, hollow storage, and resident recovery.
An appearance does not change abilities, food, projectiles, or the occupied hollow face.

Flying fairies leave sparse species-specific particles: pink sparks, snowflakes,
green spores, cherry petals, pale leaves, and purple Ender sparks. Ambient particles
are local to the client and use the game particle settings. They stop while the
fairy is mounted, resting, invisible, or on the ground; ability effects remain separate.

New species spawn in jungles, cherry groves, pale gardens, and End highlands/midlands,
respectively. New home types occasionally generate in matching natural trees in
new Overworld chunks, including 2x2 trunks. Sapling growth does not generate homes.
Ordinary, Winter, Jungle, and Cherry residents rest at night or in rain. Pale
residents work on clear nights and rest during the day or rain.

Between their main tasks, fairies have short species-specific habits. Winter
fairies rest on snow and ice, glide above water, rise in snowfall, and keep away
from nearby heat. Jungle fairies inspect mature plants, vines, and foliage without
harvesting them. Cherry fairies visit their owner and owned pets; wild ones take
an interest in peaceful animals and flowers. Pale residents use three stable
observation posts at night and pause to watch threats; wild Pale fairies seek
cover under leaves during the day. Ender fairies inspect dropped items and End
plants, sometimes making a short safe blink without picking up or claiming items.

Observation pauses vary between individuals. Combat, following the owner, shelter,
and useful work take priority; unreachable interests time out. Habits stay inside
the home or companion area and do not load chunks. Resting fairies fold their wings.

Cherry healing restores 1 HP per ten seconds after five seconds without damage.
A home heals at most one eligible target per cycle within eight blocks. The
Cherry fairy approaches its patient and needs line of sight within 2.5 blocks. Only its
owner and pets with the same owner qualify; multiple Cherry fairies share the
recipient's healing cooldown. Wild Cherry fairies do not heal players.

Pale detection has an eight-block radius while mounted and a twelve-block radius
around home. Home detection reaches only the owner within sixteen blocks of home.
Only that owner sees outlines; vanilla glowing remains independent. The snapshot
is limited to sixteen threats, refreshed once per second, and expires in two seconds.
Night patrol sounds a quiet alarm for newly detected threats at most once per five seconds.

The Ender magnet attracts up to sixteen eligible item entities every five ticks
within six blocks. Eligible loot comes from blocks broken by the owner or mobs
credited to the owner or their tame fairy. Manually dropped items, player-death
drops, unmarked loot, and other owners' loot are excluded. Mixed item entities lose
eligibility conservatively; ordinary pickup permissions remain unchanged. The
magnet respects pickup delay, inventory capacity, block collisions, and loaded chunks.
Existing unmarked drops do not gain provenance after upgrading.

Species settings live in the `jungle`, `cherry`, `pale`, and `ender` sections of
`dairatz.json`: `health`, `damage`, `fireRate`, `flySpeed`, `spawnWeight`, `minGroup`,
and `maxGroup`. Partial sections use validated defaults; weight zero disables that
species' biome spawns, independently of residents generated with natural hollows.
Legacy flat configuration keys remain supported.

Server game tests cover the hollow lifecycle, ownership, drops, generation, crop
growth, serialization, and a complete day/night cycle:

```bash
./gradlew :fabric:runGameTest -Ptarget=1.21.11-fabric
xvfb-run -a ./gradlew :fabric:runClientGameTest -Ptarget=1.21.11-fabric
```

The client tests use explicit state waits and Fabric's supported mode without the
packet synchronizer, which times out during repeated integrated-server restarts.
They still verify block state updates, entity identity, saved world data, recovery,
and rendering in the running client.

## Configuration

On first launch, a config file is created at `config/dairatz.json`.
Settings are validated as a complete snapshot. Invalid JSON or unsupported values
leave the last valid settings active (defaults on initial startup), log the reason,
and leave the file unchanged. Omitted fields use defaults; unknown fields are ignored.
Spawn weights accept 0–1000000; zero disables that species' biome spawning. The freeze radius
accepts 0–8, with zero disabling freezing. Spawn groups must be 1–64 with minimum
not greater than maximum; tame chance denominators and attack intervals must be positive.
Health must be 1–1024 and flight speed 0–1024; damage and healing must be finite and
nonnegative, with damage representable as a finite float after applicable multipliers.
Slowness levels are 1–256 and durations are nonnegative. Restart to apply settings.

### General

| Parameter | Description | Default |
|---|---|---:|
| `healAmount` | HP restored when feeding the tame item | 4.0 |
| `tameChance` | Tame chance denominator (1 in N per attempt) | 3 |

### Fairy

| Parameter | Description | Default |
|---|---|---:|
| `fairyHealth` | Fairy max HP | 16.0 |
| `furballDamage` | Furball projectile damage | 2.0 |
| `fireRate` | Ticks between shots | 60 |
| `flySpeed` | Flying speed | 0.4 |
| `dairatzSpawnWeight` | Biome spawn weight | 10 |
| `dairatzMinGroup` | Minimum group size | 2 |
| `dairatzMaxGroup` | Maximum group size | 4 |

### Winter Fairy

| Parameter | Description | Default |
|---|---|---:|
| `winterFairyHealth` | Winter Fairy max HP | 16.0 |
| `winterSnowballDamage` | Ice Furball base damage (3× against fire mobs) | 1.0 |
| `winterFireRate` | Ticks between shots | 60 |
| `winterFlySpeed` | Flying speed | 0.4 |
| `winterSlownessDuration` | Slowness duration in ticks | 60 |
| `winterSlownessLevel` | Slowness amplifier (1 = Slowness I) | 2 |
| `winterFreezeRadius` | Water freeze radius in blocks | 2 |
| `winterSpawnWeight` | Biome spawn weight | 8 |
| `winterMinGroup` | Minimum group size | 2 |
| `winterMaxGroup` | Maximum group size | 3 |

## License

MIT
