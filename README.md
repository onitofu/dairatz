# Dairatz

A Minecraft Fabric mod that adds tameable flying fairy companions.

## Requirements

- Minecraft **1.21.11**
- Fabric Loader **0.18.4+**
- Fabric API **0.141.3+1.21.11**
- Java **21**

## Building

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/dairatz-<version>.jar`.

## Development

### Running the client

```bash
./gradlew runClient
```

### Code style

The project uses [Checkstyle](https://checkstyle.org/) with an adapted Google Java Style configuration (4-space indent, 120 char line length, no Javadoc enforcement).

```bash
./gradlew checkstyleMain checkstyleClient
```

Checkstyle runs automatically as part of `./gradlew check` and the CI pipeline.

### CI

GitHub Actions workflow (`.github/workflows/build.yml`) runs on every push/PR to `main`/`master`:

1. Checkstyle validation
2. Full Gradle build
3. JAR artifact upload

## Configuration

On first launch, a config file is created at `config/dairatz.json`:

### Fairy

| Parameter | Description | Default |
|---|---|---|
| `fairyHealth` | Fairy max HP | 16.0 |
| `furballDamage` | Furball projectile damage | 2.0 |
| `fireRate` | Ticks between shots | 60 |
| `flySpeed` | Flying speed | 0.4 |

### Winter Fairy

| Parameter | Description | Default |
|---|---|---|
| `winterFairyHealth` | Winter Fairy max HP | 16.0 |
| `winterSnowballDamage` | Frostball projectile damage | 1.0 |
| `winterFireRate` | Ticks between shots | 60 |
| `winterFlySpeed` | Flying speed | 0.4 |
| `winterSlownessDuration` | Slowness duration in ticks | 60 |
| `winterSlownessLevel` | Slowness amplifier (1 = Slowness I) | 2 |
| `winterFreezeRadius` | Water freeze radius in blocks | 4 |

## Wiki

See the [Wiki](wiki/home.md) for gameplay documentation.

## License

MIT
