# Dairatz

A Minecraft mod that adds tameable flying fairy companions.

## Supported platforms

Release `1.1.1` targets Minecraft `1.21.11` and Java `21`:

| Loader | Minimum version | Artifact |
|---|---:|---|
| Fabric | Fabric Loader `0.18.4`, Fabric API `0.141.3+1.21.11` | `dairatz-1.1.1+mc1.21.11-fabric.jar` |
| Forge | Forge `61.1.7` | `dairatz-1.1.1+mc1.21.11-forge.jar` |
| NeoForge | NeoForge `21.11.42` | `dairatz-1.1.1+mc1.21.11-neoforge.jar` |

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
`forge/`, or `neoforge/`. Checkstyle uses an adapted Google Java Style configuration.

```bash
./gradlew verifyCommonIsolation :fabric:checkstyleMain :fabric:checkstyleClient \
  -Ptarget=1.21.11-fabric
```

## CI artifacts

The GitHub Actions workflow runs one matrix job per loader. Each job calls
`buildTarget`, creates `SHA256SUMS`, and uploads a loader-specific artifact retained
for 30 days. A `v*` tag attaches all JARs and checksums to a GitHub Release.

See the [multi-loader and release plan](docs/MULTILOADER_RELEASE_PLAN.md) for the
Minecraft version roadmap and release policy.

## Configuration

On first launch, a config file is created at `config/dairatz.json`:

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
| `winterSnowballDamage` | Frostball projectile damage | 1.0 |
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
