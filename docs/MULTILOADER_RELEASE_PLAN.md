# Dairatz: план multi-loader и multi-version разработки

- Статус документа: проектное решение.
- Базовая версия мода: `1.1.0` (существующий тег `v1.1`).
- Первый multi-loader релиз: `1.1.1` для Minecraft `1.21.11`, без новых мобов и рецептов.
- Целевой функциональный релиз: `1.2.0`.
- Целевые загрузчики: Fabric, Forge и NeoForge.
- Целевые версии Minecraft: `1.21.11`, семейство `26.1.x` и `26.2`.

## Текущий прогресс

- [x] Создана релизная ветка `release/1.1.1`.
- [x] `1.1.1-alpha.1`: common/Fabric и базовая переработка CI.
- [x] `1.1.1-alpha.2`: Forge 1.21.11.
- [x] `1.1.1-beta.1`: NeoForge 1.21.11.
- [x] Dedicated server smoke-test на Fabric, Forge и NeoForge 1.21.11.
- [ ] `1.1.1-rc.1`: регрессионная проверка трех loader.
- [ ] `1.1.1`: публикация трех CI-артефактов без новых мобов и рецептов.

## 1. Главные решения

1. Проект развивается в одном Git-репозитории и одной основной ветке `main`.
2. Отдельные постоянные Git-ветки для версий Minecraft и загрузчиков не создаются.
3. Игровая логика, сущности, модели, ресурсы и конфигурационная модель находятся в общем коде.
4. Fabric, Forge и NeoForge получают отдельные entrypoint и platform adapter.
5. Stonecutter управляет различиями между версиями Minecraft и создаёт отдельный build target для каждой совместимой комбинации.
6. На каждую комбинацию Minecraft × loader выпускается отдельный JAR, кроме явно протестированных совместимых patch-релизов.
7. Версия мода не зависит от версии Minecraft: один релиз имеет несколько платформенных артефактов.
8. CI расширяется постепенно: новый обязательный job добавляется только вместе с реализацией соответствующего target.
9. Первый этап переносит существующий функционал `1.1.0` на Forge и NeoForge 1.21.11; новые мобы и рецепты в этот этап не входят.

## 2. Forge и NeoForge

NeoForge не считается средой автоматической совместимости для Forge-модов.

Начиная с NeoForge 20.5 используется отдельный файл метаданных
`META-INF/neoforge.mods.toml`. Forge использует `META-INF/mods.toml`. Согласно
[официальному объявлению NeoForge](https://neoforged.net/news/20.5release/), JAR без
`neoforge.mods.toml` может быть пропущен загрузчиком. Помимо метаданных, между проектами
различаются Gradle toolchain, события, регистрации и API.

Следствия:

- Forge JAR нельзя публиковать как гарантированно совместимый с NeoForge;
- NeoForge должен иметь собственный target, metadata и smoke-тесты;
- общий исходный код между Forge и NeoForge допустим, но итоговые JAR должны собираться отдельно;
- случайный запуск Forge JAR на конкретной версии NeoForge не является поддерживаемой совместимостью.

## 3. Целевая архитектура

Логические области исходников:

```text
common/
  src/main/
    java/ru/nyansus/mc/
      entity/                 # сущности, AI и игровое поведение
      gameplay/               # общие механики
      registry/               # ID и независимые определения контента
      config/                 # модель и сериализация конфигурации
      compat/                 # небольшие адаптеры vanilla API разных MC
    resources/
      assets/dairatz/         # модели, текстуры и переводы
      data/dairatz/           # рецепты, loot tables, tags

fabric/
  src/main/
    java/                     # Fabric entrypoint и adapter
    resources/fabric.mod.json

forge/
  src/main/
    java/                     # Forge entrypoint и adapter
    resources/META-INF/mods.toml

neoforge/
  src/main/
    java/                     # NeoForge entrypoint и adapter
    resources/META-INF/neoforge.mods.toml

build/ci/<minecraft>-<loader>/ # production JAR, подготовленный для CI
```

Эта структура реализована для `1.1.1`. При подключении Stonecutter границы `common`,
`fabric`, `forge`, `neoforge` должны сохраняться.

### 3.1. Что должно находиться в common

- `AbstractFairyEntity` и конкретные классы фей;
- общие AI goals, приручение, лечение, телепортация и посадка на голову;
- логика снарядов и уникальных способностей;
- модели, render state и анимационные определения, если они используют только vanilla API;
- registry ID без вызова loader-specific регистрации;
- значения и сериализация конфигурации;
- текстуры, локализация, рецепты, loot tables и tags.

В `common` запрещены импорты `net.fabricmc.*`, `net.minecraftforge.*` и
`net.neoforged.*`.

### 3.2. Что должно находиться в platform adapter

- entrypoint мода и client entrypoint;
- регистрация сущностей, предметов и атрибутов;
- добавление spawn rules и biome spawns;
- creative tabs;
- регистрация renderer и model layer;
- получение config directory;
- loader-specific events и lifecycle;
- metadata, access widener или access transformer.

Реализованный минимальный контракт:

```java
public interface ContentRegistrar {
    <T extends Entity> RegistryEntry<EntityType<T>> registerEntityType(
            String name,
            Function<ResourceKey<EntityType<?>>, EntityType<T>> factory);

    RegistryEntry<Item> registerItem(
            String name,
            Function<ResourceKey<Item>, Item> factory);
}
```

Не следует превращать этот интерфейс в копию API загрузчика. В него добавляется только
то, что действительно используется Dairatz.

### 3.3. Архитектура новых мобов

Общие параметры варианта феи следует отделить от уникального поведения:

```text
FairyDefinition
  ├─ registry ID
  ├─ tame/heal item
  ├─ health and speed
  ├─ projectile definition
  ├─ spawn biome tag
  └─ spawn weight/group

AbstractFairyEntity
  ├─ common AI
  ├─ taming/healing
  ├─ follow/teleport
  └─ head mounting

ConcreteFairyEntity
  └─ unique ability only
```

Новые рецепты должны быть data-driven JSON-файлами. Java-код нужен только для
нестандартного recipe type/serializer.

## 4. Матрица итогового релиза `1.2.0`

Обозначения:

- **отдельный** — отдельный собираемый и тестируемый JAR;
- **семейство** — один JAR допустим для нескольких patch-релизов только после тестов на каждом из них;
- **будущий** — добавляется отдельным PR после выхода стабильного Minecraft и loader toolchain.

| Minecraft | Java | Fabric | Forge | NeoForge | Примечание |
|---|---:|---|---|---|---|
| 1.21.11 | 21 | отдельный | отдельный | отдельный | Текущая исходная версия проекта |
| 26.1 | 25 | семейство 26.1.x | семейство 26.1.x | семейство 26.1.x | Новый unobfuscated toolchain |
| 26.1.1 | 25 | семейство 26.1.x | семейство 26.1.x | семейство 26.1.x | Обязательный smoke-test |
| 26.1.2 | 25 | семейство 26.1.x | семейство 26.1.x | семейство 26.1.x | Build baseline для линии 26.1.x |
| 26.2 | 25 | отдельный | отдельный | отдельный | Текущий latest target |
| будущая 26.3+ | 25+ | будущий | будущий | будущий | В текущий scope не входит |

Все выбранные версии представлены в официальном списке
[Minecraft Forge downloads](https://files.minecraftforge.net/net/minecraftforge/forge/).

Для `26.1.x` сначала пробуется единый JAR с корректным Minecraft version range. Если
хотя бы один loader не проходит build или smoke-test на всех `26.1`, `26.1.1` и
`26.1.2`, семейство разбивается на три отдельных target и JAR.

При обязательной поддержке всех трёх loader стабильный `1.2.0` будет содержать:

- 3 артефакта для `1.21.11`;
- 3 артефакта для `26.1.x`, если линия бинарно совместима;
- 3 артефакта для `26.2`;
- итого 9 артефактов в минимальной полной матрице;
- до 15 артефактов, если `26.1`, `26.1.1` и `26.1.2` потребуют отдельных JAR.

Поэтому NeoForge имеет смысл включать в обязательную матрицу только как осознанное
продуктовое решение: он не достаётся автоматически вместе с Forge.

### 4.1. Имена артефактов

Формат:

```text
dairatz-<mod-version>+mc<minecraft>-<loader>.jar
```

Примеры:

```text
dairatz-1.2.0+mc1.21.11-fabric.jar
dairatz-1.2.0+mc1.21.11-forge.jar
dairatz-1.2.0+mc1.21.11-neoforge.jar
dairatz-1.2.0+mc26.1.x-fabric.jar
dairatz-1.2.0+mc26.1.x-forge.jar
dairatz-1.2.0+mc26.1.x-neoforge.jar
dairatz-1.2.0+mc26.2-fabric.jar
dairatz-1.2.0+mc26.2-forge.jar
dairatz-1.2.0+mc26.2-neoforge.jar
```

В metadata версия мода остаётся `1.2.0`; Minecraft и loader задаются отдельными
dependency constraints. Полное имя используется только для файла и публикации.

## 5. План релизов

### 5.1. Перенос существующего `1.1.0`

| Версия | Назначение | Публикация |
|---|---|---|
| `1.1.1-alpha.1` | Разделение common/Fabric без изменения поведения | GitHub prerelease, 1.21.11 Fabric |
| `1.1.1-alpha.2` | Forge adapter и первый Forge JAR | GitHub prerelease, 1.21.11 Fabric/Forge |
| `1.1.1-beta.1` | NeoForge adapter и первый NeoForge JAR | GitHub prerelease, 1.21.11 × три loader |
| `1.1.1-rc.1` | Регрессионные тесты текущего контента `1.1.0` | GitHub/Modrinth beta |
| `1.1.1` | Стабильный multi-loader 1.21.11 без новых мобов | GitHub, Modrinth, CurseForge |

Тег `v1.1` не переписывается. `1.1.1` является преимущественно техническим релизом
совместимости. Registry ID и конфигурация должны соответствовать `1.1.0`; согласованное
исключение — новая модель Ice Furball и тройной урон этого снаряда мобам, особенно
уязвимым к заморозке.

### 5.2. Расширение версий Minecraft без нового контента

| Версия | Назначение | Обязательная матрица |
|---|---|---|
| `1.1.2-alpha.1` | Первый порт на 26.2 | 1.21.11 и 26.2 × три loader |
| `1.1.2` | Стабильная поддержка 26.2 | 6 build jobs |
| `1.1.3-alpha.1` | Первый порт на 26.1.2 | 1.21.11, 26.1.x и 26.2 × три loader |
| `1.1.3-rc.1` | Проверка одного 26.1.x JAR на 26.1/26.1.1/26.1.2 | Полная build/compatibility матрица |
| `1.1.3` | Полный version scope со старым набором контента | 9 JAR либо 15 при разделении 26.1.x |

### 5.3. Функциональный релиз с новыми мобами

| Версия | Назначение | Публикация |
|---|---|---|
| `1.2.0-alpha.1` | Три новых моба, предметы и рецепты | GitHub prerelease, сначала 1.21.11 × три loader |
| `1.2.0-beta.1` | Новый контент перенесён на 26.1.x и 26.2 | GitHub prerelease, полная матрица |
| `1.2.0-rc.1` | Заморозка контента, тест миров и dedicated server | GitHub/Modrinth beta |
| `1.2.0` | Первый стабильный функциональный multi-version релиз | GitHub, Modrinth, CurseForge |

Новые мобы не разрабатываются до стабильного `1.1.1`. Желательно также завершить
version scope `1.1.3`, чтобы новый контент сразу создавался внутри проверенной матрицы.

### 5.4. Следующие релизы

- `1.2.1`, `1.2.2` — исправления без изменения сохранений и registry ID;
- `1.3.0` — новые предметы, мобы, рецепты и совместимые механики;
- `2.0.0` — только несовместимые изменения конфигурации, API или сохранённых данных;
- новая версия Minecraft сама по себе не повышает major/minor мода;
- исправление одного target повышает patch глобально, после чего release workflow
  пересобирает всю поддерживаемую матрицу.

## 6. Этапы реализации

### Этап 0. Зафиксировать контракт совместимости

- [x] Сохранить существующие registry ID и NBT-поля.
- [x] Описать поддерживаемые Minecraft/loader в README.
- [x] Решить, остаётся ли NeoForge обязательным target. В этом плане он считается обязательным.
- [ ] Зафиксировать список новых мобов, предметов и рецептов отдельной спецификацией.

Критерий готовности: добавление loader не меняет существующие миры и игровой баланс.

### Этап 1. Очистить текущую Fabric-базу

- [x] Удалить неиспользуемые `ExampleMixin` и `ExampleClientMixin`, если они действительно пустые.
- [x] Исправить placeholder metadata в `fabric.mod.json`.
- [ ] Добавить минимальные automated tests для конфигурации и общих вычислений.
- [ ] Проверить запуск клиента на Fabric 1.21.11.
- [x] Проверить запуск dedicated server на Fabric 1.21.11.

Критерий готовности: поведение версии `1.1.0` воспроизводится без регрессий.

### Этап 2. Выделить common и Fabric adapter

- [x] Перенести Fabric entrypoint в `fabric` source set.
- [x] Вынести получение config directory из общей конфигурации.
- [x] Вынести biome modifications, attributes, creative tabs и client registrations.
- [x] Оставить entity/gameplay code в common.
- [x] Запретить loader imports в common проверкой CI или ArchUnit-подобным тестом.

На этом этапе существующий CI разделяется на два логических job:

- `quality` — Checkstyle и общие unit tests, запускается один раз;
- `build-1.21.11-fabric` — сборка Fabric JAR и загрузка artifact.

Критерий готовности: Fabric 1.21.11 работает идентично исходному моду, а CI сохраняет
production JAR отдельно от `sources` и development JAR.

### Этап 3. Добавить Forge 1.21.11

- [x] Реализовать Forge adapter и Forge metadata.
- [x] Добавить Forge-регистрацию сущностей, предметов, атрибутов, спавнов и client renderer.
- [x] Преобразовать необходимую часть access widener в Forge access transformer либо удалить необходимость доступа.
- [ ] Проверить client и integrated server.
- [x] Проверить dedicated server.
- [x] Добавить CI matrix row `build-1.21.11-forge`.
- [x] Сохранять artifact `dairatz-1.21.11-forge`.

Критерий готовности: `1.1.1-alpha.2` содержит Fabric и Forge JAR с одинаковым
функционалом `1.1.0`.

### Этап 4. Добавить NeoForge 1.21.11

- [x] Реализовать NeoForge adapter и `neoforge.mods.toml`.
- [x] Добавить NeoForge-регистрацию сущностей, предметов, атрибутов, спавнов и client renderer.
- [x] Настроить NeoForge access transformer или удалить необходимость доступа.
- [ ] Проверить client и integrated server.
- [x] Проверить dedicated server.
- [x] Добавить CI matrix row `build-1.21.11-neoforge`.
- [x] Сохранять artifact `dairatz-1.21.11-neoforge`.

Критерий готовности: `1.1.1-beta.1` содержит три loader-specific JAR с одинаковыми
registry ID, конфигурацией и игровым поведением.

### Этап 5. Выпустить стабильный `1.1.1`

- [x] Не добавлять новые мобы и рецепты; Ice Furball является согласованным исключением.
- [ ] Провести регрессионную проверку существующих Dairatz и Winter Fairy.
- [ ] Проверить загрузку мира, созданного на Fabric `1.1.0`.
- [ ] Проверить отдельный клиент на трёх loader.
- [x] Проверить dedicated server на трёх loader.
- [x] Добавить tag-triggered job `release`, который собирает artifacts в GitHub Release.
- [ ] Опубликовать три JAR для Minecraft 1.21.11.

Критерий готовности: стабильный `1.1.1` переносит реализацию `1.1.0` на Fabric,
Forge и NeoForge, не добавляя новых мобов и рецептов.

### Этап 6. Подключить Stonecutter и портировать версии

- [ ] Добавить target metadata: Minecraft, loader, Java и dependency versions.
- [ ] Настроить constants `fabric`, `forge`, `neoforge` и version predicates.
- [ ] Сосредоточить version-specific условия в `compat`, build scripts и metadata.
- [ ] Не размазывать Stonecutter-условия по игровой логике без необходимости.
- [ ] Добавить задачи сборки одного target и всей матрицы.
- [ ] Портировать `26.2` × Fabric/Forge/NeoForge и выпустить `1.1.2`.
- [ ] Портировать `26.1.2` как build baseline линии `26.1.x`.
- [ ] Проверить тот же JAR на `26.1.1` и `26.1`.
- [ ] Разделить `26.1.x` на точные target только при доказанной бинарной несовместимости.
- [ ] Выпустить полную техническую матрицу как `1.1.3`.

Для перехода `1.21.11` → `26.1` учитывать Java 25 и отсутствие обфускации:
[Fabric porting guide](https://docs.fabricmc.net/develop/porting/index) и
[NeoForged migration primer](https://docs.neoforged.net/primer/docs/26.1/).

Критерий готовности: активный target переключается одной Gradle-задачей, каждый target
компилируется и проходит loader smoke-test, а полный scope `1.1.3` не содержит новых
мобов.

### Этап 7. Добавить новый контент `1.2.0`

- [ ] Ввести `FairyDefinition` или эквивалентную декларативную модель.
- [ ] Добавить три моба с общей базовой логикой.
- [ ] Добавить spawn eggs, модели, renderer, текстуры и переводы.
- [ ] Добавить рецепты через JSON.
- [ ] Добавить spawn biome tags и loot tables.
- [ ] Добавить GameTests для приручения, атаки, уникальных способностей и рецептов.
- [ ] Перенести новый контент на полную существующую CI-матрицу.

Критерий готовности: новый контент работает на всех обязательных Minecraft/loader target.

### Этап 8. Пошаговая переработка CI и публикации

CI не создаёт jobs для ещё не реализованных target. Матрица растёт вместе с релизами:

| Milestone | Добавляемые jobs | Build jobs после этапа |
|---|---|---:|
| Текущий `1.1.0` | существующий Fabric build | 1 |
| `1.1.1-alpha.1` | `quality`, нормализованный `build-1.21.11-fabric` | 1 + quality |
| `1.1.1-alpha.2` | `build-1.21.11-forge` | 2 + quality |
| `1.1.1-beta.1` | `build-1.21.11-neoforge` | 3 + quality |
| `1.1.1` | `release` для сборки трёх artifacts в GitHub Release | 3 + quality + release |
| `1.1.2` | три build jobs для `26.2` | 6 + quality + release |
| `1.1.3` | три build jobs и compatibility tests для `26.1.x` | 9 + quality + compatibility + release |
| `1.2.0` | новые GameTests, матрица target не расширяется | 9 + quality + compatibility + release |

#### 8.1. Контракт Gradle для CI

CI вызывает один стабильный интерфейс, скрывающий внутренние задачи loader toolchain
(и в будущем Stonecutter):

```text
./gradlew buildTarget -Ptarget=<minecraft>-<loader>
```

Примеры:

```text
./gradlew buildTarget -Ptarget=1.21.11-forge
./gradlew buildTarget -Ptarget=26.1.x-neoforge
./gradlew buildTarget -Ptarget=26.2-fabric
```

Задача обязана выполнить проверки target и скопировать ровно один production JAR в:

```text
build/ci/<target>/
```

`sources`, development и shadow/intermediary JAR не должны попадать в release artifact.

#### 8.2. Build matrix

После реализации всего scope PR-матрица содержит:

- `1.21.11` × Fabric/Forge/NeoForge;
- `26.1.2` × Fabric/Forge/NeoForge;
- `26.2` × Fabric/Forge/NeoForge.

Java выбирается на уровне matrix row: Java 21 для `1.21.11`, Java 25 для `26.1.x`
и `26.2`. `fail-fast` должен быть выключен, чтобы падение одного target не скрывало
результаты остальных.

#### 8.3. Compatibility jobs для `26.1.x`

Один JAR сначала собирается против `26.1.2`, затем отдельные compatibility jobs
запускают его на `26.1`, `26.1.1` и `26.1.2`. При несовместимости build matrix
разделяется на точные patch-target.

#### 8.4. Сохранение artifacts

Каждый build job загружает уникальный GitHub Actions artifact:

```text
dairatz-1.21.11-fabric
dairatz-1.21.11-forge
dairatz-1.21.11-neoforge
...
```

Artifact содержит production JAR и `SHA256SUMS`. Для PR/push artifacts хранятся
ограниченное время. При теге `v*` job `release`:

1. ждёт успешного завершения обязательных build/compatibility jobs;
2. скачивает все artifacts текущего workflow;
3. проверяет отсутствие дубликатов имён и наличие ожидаемой матрицы;
4. создаёт GitHub Release или дополняет существующий;
5. прикладывает JAR и checksum как постоянные release assets.

#### 8.5. Общие проверки

- проверка отсутствия loader imports в common;
- Checkstyle и unit tests;
- GameTests там, где loader поддерживает стабильный runner;
- запуск dedicated server;
- проверка содержимого JAR и правильного metadata;
- публикация только при успехе всей обязательной матрицы.

## 7. Git-flow

Постоянные ветки:

- `main` — единственная основная ветка;
- `support/<major.minor>` — создаётся только при необходимости долгого сопровождения старой функциональной линии.

Временные ветки:

```text
refactor/multiloader-layout
feature/fire-fairy
feature/stonecutter-matrix
port/26.2
fix/forge-spawn-registration
```

Правила:

- feature/port/fix ветка живёт только до merge PR;
- ветки по Minecraft-версии не используются как постоянное хранилище портов;
- feature добавляется сначала в common и в том же PR доводится до текущей обязательной PR-матрицы;
- теги `v1.1.1`, `v1.1.2`, `v1.1.3` и `v1.2.0` создаются только из `main` после соответствующего release CI;
- один тег создаёт все JAR релиза.

## 8. Политика поддержки

Рекомендуемые уровни:

| Уровень | Версии | Обязательства |
|---|---|---|
| Active | 1.21.11, 26.1.x, 26.2 | Полные исправления и регрессионные тесты |
| Final | снятые с поддержки линии | Последний JAR остаётся доступным без новых исправлений |

`26.3+` не входит в текущие обязательства. Новая линия добавляется отдельным решением
после стабильного релиза Minecraft, появления toolchain всех обязательных loader и
прохождения CI.

## 9. Definition of Done

### 9.1. Стабильный `1.1.1`

- [x] Реализация `1.1.0` разделена на common и loader adapters.
- [x] Не добавлены новые мобы и рецепты; реализовано согласованное изменение Ice Furball.
- [x] Существующие registry ID, NBT и конфигурация сохранены; совместимость мира ожидает runtime-теста.
- [x] Собраны отдельные Fabric, Forge и NeoForge JAR для Minecraft 1.21.11.
- [x] Каждый JAR имеет правильное loader-specific metadata.
- [ ] Клиент запускается на каждом loader.
- [x] Dedicated server запускается на каждом loader.
- [x] CI настроен сохранять три уникальных artifact с JAR и checksum.
- [x] Tag workflow настроен публиковать все три JAR в GitHub Release.

### 9.2. Стабильный `1.2.0`

- [ ] Общий код не зависит от Fabric/Forge/NeoForge API.
- [ ] Существующие registry ID и миры версии `1.1.0` совместимы.
- [ ] Три новых моба и рецепты доступны на всех обязательных target.
- [ ] Все артефакты имеют правильные loader-specific metadata.
- [ ] Forge JAR не публикуется как NeoForge JAR и наоборот.
- [ ] Полная release-матрица собирается в чистом CI.
- [ ] Клиент и dedicated server запускаются для каждого target.
- [ ] README содержит таблицу файлов и уровни поддержки.
- [ ] Git tag, changelog и опубликованные файлы используют одну mod version.
