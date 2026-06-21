# Slayer Stats

RuneLite plugin that tracks slayer session stats during tasks.

## POC features

- Tracks slayer point gains via the `SLAYER_POINTS` varbit (positive deltas only, so spending points is ignored)
- Starts a session on slayer XP gain
- Ends the session after a configurable idle period with no slayer XP
- Shows session stats in a text overlay panel (points, tasks, superiors — each line toggled independently)

## Install (sideload)

Clone on the machine you play on, build, then sideload the jar:

```bash
git clone https://github.com/mevans/slayer-stats.git
cd slayer-stats
./gradlew shadowJar
```

Copy `build/libs/slayer-stats-unspecified-all.jar` into your RuneLite sideload folder:

- **Windows:** `%USERPROFILE%\.runelite\sideloaded-plugins\`
- **macOS:** `~/.runelite/sideloaded-plugins/`

Add `--developer-mode` as a client argument in the RuneLite launcher configure window, restart the client, then enable **Slayer Stats** in the plugin list.

## Development

Requires JDK 11+.

```bash
./gradlew run
```

Build a distributable jar:

```bash
./gradlew shadowJar
```

The jar will be in `build/libs/slayer-stats-unspecified-all.jar`.

## Configuration

| Setting | Description | Default |
| --- | --- | --- |
| Show overlay | Toggle the on-screen stats panel | On |
| Show points per hour | Show points/hr line | On |
| Show session points | Show session points line | On |
| Show superiors spawned | Show superiors spawned line | On |
| Show superiors per hour | Show superiors/hr line | On |
| Show tasks completed | Show tasks completed line | On |
| Show tasks per hour | Show tasks/hr line | On |
| Session idle timeout | Minutes without slayer XP before ending session | 5 |

## License

BSD 2-Clause (same as RuneLite example plugin)
