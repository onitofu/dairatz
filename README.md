# Dairatz

A Minecraft mod that adds tameable flying fairy companions.

## Supported platforms

Release `1.2.1` targets Minecraft `1.21.11` and Java `21`:

| Loader | Minimum version | Artifact |
|---|---:|---|
| Fabric | Fabric Loader `0.18.4`, Fabric API `0.141.3+1.21.11` | `dairatz-1.2.1+mc1.21.11-fabric.jar` |
| Forge | Forge `61.1.7` | `dairatz-1.2.1+mc1.21.11-forge.jar` |
| NeoForge | NeoForge `21.11.42` | `dairatz-1.2.1+mc1.21.11-neoforge.jar` |

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

A resident tends plants within eight blocks horizontally and three vertically.
Flowers are required for pollination. She stays by the garden while you travel,
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
loaded and within sixteen blocks. Winter fairies cannot occupy oak hollows.

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
