# Slayer Stats

RuneLite plugin that tracks slayer session stats during tasks.

## POC features

- Tracks slayer point gains via the `SLAYER_POINTS` varbit (positive deltas only, so spending points is ignored)
- Starts a session on slayer XP gain
- Ends the session after a configurable idle period with no slayer XP
- Shows session stats in infoboxes (points, tasks, superiors — each with count and per-hour toggles)

## Install (sideload)

Clone on the machine you play on, build, then sideload the jar:

```bash
git clone https://github.com/mevans/slayer-stats.git
cd slayer-stats
./gradlew shadowJar
```

Copy `build/libs/slayer-stats--all.jar` into your RuneLite sideload folder:

- **Windows:** `%USERPROFILE%\.runelite\sideloaded-plugins\`
- **macOS:** `~/.runelite/sideloaded-plugins/`

Enable **Developer mode** in RuneLite settings, restart the client, then enable **Slayer Stats** in the plugin list.

## Development

Requires JDK 11+.

```bash
./gradlew run
```

Build a distributable jar:

```bash
./gradlew shadowJar
```

The jar will be in `build/libs/slayer-stats--all.jar`.

## Configuration

| Setting | Description | Default |
| --- | --- | --- |
| Show points per hour | Toggle the points/hour infobox | On |
| Show session points | Toggle the session points infobox | On |
| Show superiors spawned | Toggle the superiors spawned infobox | On |
| Show superiors per hour | Toggle the superiors/hour infobox | On |
| Show tasks completed | Toggle the tasks completed infobox | On |
| Show tasks per hour | Toggle the tasks/hour infobox | On |
| Session idle timeout | Minutes without slayer XP before ending session | 5 |

## License

BSD 2-Clause (same as RuneLite example plugin)
