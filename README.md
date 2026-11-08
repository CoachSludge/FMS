# FMS — Elytra Flight Management System

**FMS** is a client-side Minecraft Fabric mod that turns Elytra travel into an aviation-style experience. It adds configurable flight instruments, terrain and maneuver warnings, warning audio, destination navigation, and an **advisory** cruise/rocket planner. Optional compatibility with **Xaero's Minimap** and **Xaero's World Map** lets you reuse saved waypoints and view a flight-plan summary in the map.

> **Development status:** \`0.1.0-alpha\` for **Minecraft Java Edition 26.3**. This is a prototype. Features described below exist in the source, but not every latest change has been verified with a successful local build or in-game test. Terrain warnings and flight estimates are **not guaranteed safe**. **FMS is not an autopilot.**

## Contents

- [Quick start](#quick-start)
- [Requirements and installation](#requirements-and-installation)
- [Controls](#controls)
- [HUD layouts and settings](#hud-layouts-and-settings)
- [Navigation and Xaero integration](#navigation-and-xaero-integration)
- [Cruise altitude and rocket estimates](#cruise-altitude-and-rocket-estimates)
- [Warning system](#warning-system)
- [Custom warning sounds](#custom-warning-sounds)
- [Configuration files](#configuration-files)
- [Development and building](#development-and-building)
- [Known limitations and troubleshooting](#known-limitations-and-troubleshooting)
- [Roadmap](#roadmap)

## Quick start

1. Install Fabric for **Minecraft 26.3**, Fabric API, and the FMS mod JAR in your Minecraft profile's \`mods/\` folder. [Mod Menu](https://modrinth.com/mod/modmenu) is optional.
2. Join a world, equip an Elytra, and press **O** to open FMS settings. The default HUD mode shows FMS when an Elytra is equipped.
3. Press **K** to switch HUD layouts: **Boxed → Aviation → Hybrid**. Use **H** to toggle the HUD.
4. Set a destination, for example:
   \`\`\`text
   /fms go 4300 800
   /fms cruise status
   \`\`\`
5. While flying, follow the displayed heading/altitude **suggestions**. FMS will not steer, change pitch, or use rockets for you.
6. To use Xaero waypoints, install compatible Xaero's Minimap and World Map versions, create/save a waypoint, then enter:
   \`\`\`text
   /fms xaero list
   /fms xaero select 1
   \`\`\`

All FMS commands are **client-side**; no server plugin or operator permission is required. Manual navigation and Xaero waypoint navigation share the same FMS destination display.

## Requirements and installation

| Item | Version / requirement |
| --- | --- |
| Minecraft | **Java Edition 26.3** |
| Mod loader | **Fabric Loader 0.19.5 or newer compatible version** |
| Required mod | **Fabric API** (project development version: \`0.161.0+26.3\`) |
| Java runtime for development | **Java 25** |
| Mod Menu | Optional; provides **Mods → FMS → Configure** |
| Xaero's Minimap | Optional; minimap HUD avoidance and waypoint files |
| Xaero's World Map | Optional; shared waypoints and an FMS flight-plan overlay |

**Player installation:** Put the built FMS JAR and matching Fabric API JAR in your active Minecraft profile's \`mods/\` directory. Add Mod Menu and/or Xaero mods if desired. Use versions that actually support Minecraft 26.3.

**Developers:** The project's Gradle configuration targets Fabric Loom \`1.18.2\`, Gradle \`9.7.1\`, and Java \`25\`. The repository may not include a complete Gradle wrapper JAR, so a system/local Gradle installation may be needed.

FMS is a client-only mod: the world or server does not need to install FMS to use its HUD or local commands.

## Controls

Default keybinds (rebind them in Minecraft's Controls menu if needed):

| Key | Action |
| --- | --- |
| **H** | Show/hide FMS HUD |
| **J** | Open the **Boxed HUD** drag-and-drop editor; Escape saves |
| **K** | Cycle **Boxed / Aviation / Hybrid** layouts |
| **M** | Mute/unmute FMS warning audio |
| **N** | Preview the stock master-caution beep |
| **O** | Open FMS settings while in a world |

### Commands

| Command | What it does |
| --- | --- |
| \`/fms go <x> <z>\` | Set a destination using manual X/Z coordinates |
| \`/fms clear\` | Clear the current destination |
| \`/fms xaero list\` | List saved Xaero waypoints (up to 20 displayed) |
| \`/fms xaero select <number>\` | Select a listed Xaero waypoint as your destination |
| \`/fms map on\` / \`/fms map off\` | Show/hide the flight-plan panel inside Xaero's World Map |
| \`/fms cruise on\` / \`/fms cruise off\` | Show/hide cruise guidance |
| \`/fms cruise status\` | Print current altitude, heading, and rocket estimates |
| \`/fms cruise auto\` | Return to calculated cruise-altitude guidance |
| \`/fms cruise y <64–300>\` | Manually choose a target cruise Y-level |
| \`/fms cruise reach <100–2000>\` | Set assumed horizontal **blocks per rocket** |
| \`/fms cruise reserve <0–16>\` | Set the number of extra reserve rockets |

**Example flight:**
\`\`\`text
/fms go 4300 800
/fms cruise on
/fms cruise reach 400
/fms cruise reserve 2
/fms cruise status
\`\`\`

The destination remains active until you clear it, leave the current world, or switch to another destination. Destinations are **session-only**, not saved permanently in FMS.

## HUD layouts and settings

FMS offers three display styles:

- **Aviation (default):** A transparent flight HUD with a heading tape, pitch ladder, speed and altitude tapes, velocity-based flight-path marker, vertical speed, ground clearance (AGL when measurable), Elytra durability, rockets, navigation, and active warnings.
- **Boxed:** Two configurable panels: **Flight Data** and **Navigation**. The J editor lets you drag these panels and save their positions.
- **Hybrid:** Aviation-style HUD plus additional terrain-status text where space permits.

### Visibility and layout settings

Open settings using **O**, or **Mod Menu → FMS → Configure**. Tabs currently include **HUD, Audio, Terrain, Wings**.

The **HUD** tab includes:

- **HUD Visibility:** \`ALWAYS\`, \`ELYTRA_EQUIPPED\` (default), or \`ONLY_FLYING\`.
- **Layout:** Boxed, Aviation, or Hybrid.
- **HUD Enabled:** Global display toggle.
- **Xaero Space:** Optional approximate minimap safe-area avoidance.
- **Minimap Corner:** Set to **top-right, top-left, bottom-right, or bottom-left** to match your Xaero configuration.

FMS reserves space near the bottom of the screen for Minecraft's hotbar, XP, hearts, armor, food, and other Survival indicators. It uses an approximate **78 GUI-pixel** bottom margin in Survival and **42 pixels** in Creative. On smaller scaled windows, it may hide secondary labels rather than covering vanilla UI.

In Aviation/Hybrid, destination and cruise readouts are combined into one compact top-corner block, opposite the configured Xaero Minimap corner when avoidance is enabled. **FMS does not detect Xaero's exact live minimap dimensions or arbitrary custom positions.** If something overlaps, verify **Xaero Space: ON** and set **Minimap Corner** correctly.

Only Boxed panels are draggable via J; individual aviation instruments are not independently movable yet.

## Navigation and Xaero integration

### Manual coordinates

Use:
\`\`\`text
/fms go 4300 800
\`\`\`

FMS shows the destination as **CUSTOM**, together with distance, heading, and an ETA based on current horizontal speed. When stationary, ETA may be unavailable. This is a **straight-line** estimate, not a terrain-aware path or a promise that you can reach it without detours.

### Xaero saved waypoints

With compatible **Xaero's Minimap** and **Xaero's World Map** installed:

1. Open Xaero's World Map (commonly **M**, depending on your Xaero keybind).
2. Create and **save** a waypoint.
3. Run \`/fms xaero list\`.
4. Check the waypoint's displayed **world and dimension**, then run \`/fms xaero select 1\` (or the number shown).
5. FMS uses that waypoint's **name and X/Z coordinates**.

This is a **read-only, file-based integration**. It looks for Xaero's waypoint text files in the active game directory, under \`xaero/minimap/\` or the older \`XaeroWaypoints/\` location. It does not modify Xaero waypoint data.

**Important:** FMS does not automatically validate the selected waypoint against the current world/dimension. Lists can include other worlds or servers. Select only a waypoint from the correct world and dimension. Xaero file formats can change, so importer compatibility needs in-game verification.

### FMS information inside Xaero's World Map

When the World Map is open, FMS has an optional upper-right **FMS / FLIGHT PLAN** panel showing:

- **YOU:** current X, Y, and Z.
- **TO / DEST:** destination label and X/Z.
- **DIST / HDG:** straight-line distance and destination bearing.
- **CRUISE Y:** recommended altitude plus \`CLIMB\`, \`LEVEL\`, or \`DESCEND\`.
- **ROCKETS:** estimated requirement (including reserve) versus rockets in inventory.
- **TERRAIN:** whether nearby sampled sections cover the proposed route.

Both **\`/fms go\` manual destinations** and **\`/fms xaero select\` destinations** feed this same panel. Toggle it with \`/fms map on\` or \`/fms map off\`. The panel is also hidden when cruise guidance is turned off.

**Currently not implemented:** Clicking an arbitrary point on Xaero's World Map to set the FMS destination directly. First create/select a saved waypoint or use manual coordinates. The overlay uses an optional Fabric screen hook and may need adaptation to Xaero GUI changes.

## Cruise altitude and rocket estimates

After you select a destination, the flight director can display:

| Display | Meaning |
| --- | --- |
| \`CRZ Y 250\` | Suggested **world Y-level** to cruise at |
| \`CLIMB\`, \`LEVEL\`, \`DESCEND\` | Whether your current Y is below, near, or above the target |
| \`TURN L\` / \`TURN R\` | Approximate heading correction to destination |
| \`RKT ~12 / 18\` | Estimated rockets needed / carried (example only) |
| \`ROUTE NOT SURVEYED\` | FMS could not confirm terrain along the entire proposed route |

### Rocket budgeting

FMS starts with a configurable assumption of **400 horizontal blocks traveled per rocket** and **2 reserve rockets**:

\`\`\`text
Travel rockets = ceil(remaining distance / assumed blocks per rocket)
Estimated total = travel rockets + reserve rockets
\`\`\`

For example, 3,900 blocks at 400 blocks/rocket with 2 reserve gives \`ceil(3900/400) + 2 = 12\` estimated rockets.

**This is a rough budget, not an accurate flight-physics simulation.** Actual range depends on how you glide, flight speed, firework use, pitch changes, and terrain. Tune \`/fms cruise reach\` after your own test flights.

### Cruise Y recommendation

FMS samples **Minecraft's loaded chunk heightmaps** along up to approximately the next **768 blocks** toward your destination. It uses the highest sampled terrain plus a default **40-block clearance margin**. If portions of the route are unknown, it also applies a configurable **Y=250** fallback. The current prototype clamps automatic recommendations to Y **64–300**.

**A route marked "sampled" is not certified safe.** Sparse samples can miss structures, caves, overhangs, or terrain between sample points; unloaded or distant terrain is unknown. The current code does **not** read Xaero's stored world-map terrain heights, does not produce obstacle-avoiding paths, and does not control the Elytra.

Use \`/fms cruise y 180\` to set a manual target or \`/fms cruise auto\` to restore automatic suggestions.

## Warning system

Flight warnings operate while you are **actively Elytra-gliding**. The HUD and sounds use live player movement, remaining Elytra durability, rocket inventory, and locally loaded terrain.

### One sound at a time

To prevent overlapping "Terrain / Pull Up / Warning" playback during stressful flights:

- FMS has **one active warning-audio channel**. Starting a new warning stops the previous FMS sound instance.
- A **higher-priority** warning can interrupt immediately.
- An equal-priority switch must stabilize for about **0.8 seconds**; downgrading requires about **1.8 seconds**.
- A cleared condition waits about **0.7 seconds** before stopping its sound.
- A persistent alert repeats no sooner than **12 seconds**, and only after its previous clip has ended.
- **Master caution does not automatically beep before every voice message.** Caution/warning clips can instead be assigned directly to particular warning types.

### Terrain and altitude

FMS looks at loaded Minecraft surface-height data at the player and along a short projected flight path (up to approximately 48 blocks forward). Thresholds below are initial test values:

| Alert | Initial trigger | Default clip |
| --- | --- | --- |
| **Altitude** | Under 25 blocks above local terrain, descending more than 3 blocks/sec | \`altitude.ogg\` |
| **Low Terrain** | Under 7 blocks above local terrain | \`lowterrain.ogg\` |
| **Terrain Ahead** | Projected clearance under 14 blocks | \`terrainahead.ogg\` |
| **Terrain** | Projected clearance under 7 blocks within 1.8 sec | \`terrain.ogg\` |
| **Pull Up** | Projected clearance under 3.5 blocks within 1.15 sec | \`pullup.ogg\` |

This is a **local, short-horizon advisory**, not collision avoidance. It does not know all blocks, caves, ceilings, lateral walls, unloaded terrain, or future turns.

### Elytra health, descent, fuel and maneuver alerts

| Alert | Initial trigger | Default clip |
| --- | --- | --- |
| **Elytra Health** | 35% durability remaining or less | \`elytra.ogg\` |
| **Low Elytra** | 15% or less | \`lowelytra.ogg\` |
| **Land Soon** | 8% or less | \`land.ogg\` |
| **Land Now** | 4% or less, 5 or fewer durability points, **or** 8% or less with 3 or fewer rockets | \`landnow.ogg\` |
| **High-Speed Descent** | More than 32 blocks/sec downward and more than 15 blocks/sec horizontally | \`warning.ogg\` |
| **Sink Rate** | More than 16 blocks/sec downward when no higher-priority condition applies | \`caution.ogg\` |
| **Bingo Fuel** | 3 or fewer fireworks in inventory | \`bingo.ogg\` |
| **Hard Turn** | Fast actual movement-direction change at 14+ blocks/sec, after a brief filtered threshold | \`mastercaution.ogg\` |
| **Steep Dive** | Nose down over 38°, horizontal speed at least 16 blocks/sec, and descent over 11 blocks/sec | \`mastercaution.ogg\` |

Actual flight-velocity calculations are **not** real-world airspeed or G-force measurements. Thresholds will be tuned through gameplay testing. Highest-priority terrain/collision alerts take precedence over ordinary cautions.

### Sound controls

In **Audio**, set audio ON/OFF, adjust volume, select warning clips, preview them, and toggle **Maneuvers**. The **Terrain** tab controls terrain warnings and their five sound assignments; **Wings** controls four Elytra-health/landing sounds. Most assignments can be switched to **off**, stock sounds, or imported custom clips.

The default sound names are identifiers; they do not imply that the game plays a master-caution beep before each spoken callout.

## Custom warning sounds

Players can import up to **16 OGG files** without modifying the mod JAR.

1. Open **FMS Settings → Audio → OPEN FOLDER**, or browse to \`config/fms/sounds/\` in the current Minecraft game directory.
2. Add clips such as \`my_alert.ogg\`. Names may use letters, digits, underscores, and hyphens, with at most 48 characters before \`.ogg\`. The extension must be lowercase.
3. Return to settings and click **REFRESH**.
4. FMS generates an unpacked resource pack at \`resourcepacks/FMS Custom Sounds/\`.
5. In **Minecraft Options → Resource Packs**, **enable FMS Custom Sounds**. After adding or changing clips, reload resources (typically **F3+T**) so Minecraft recognizes them.
6. Choose \`custom:my_alert\` in a sound-assignment selector and click **TEST**.

Per-file limit: **8 MiB**. FMS checks the Ogg container signature, but the recording must actually be encoded in a playable Minecraft-compatible format (normally **OGG Vorbis**). MP3 and WAV are not directly supported by this importer.

Development instance paths are normally \`run/config/fms/sounds/\` and \`run/resourcepacks/FMS Custom Sounds/\`; actual launcher paths depend on the selected Minecraft profile. Custom clips do not require Xaero mods.

## Configuration files

FMS user settings are stored in:

\`\`\`text
.minecraft/
├─ config/
│  ├─ fms-hud.properties         # HUD, audio, terrain, cruise and map toggles
│  └─ fms/
│     └─ sounds/                # User-provided .ogg recordings
└─ resourcepacks/
   └─ FMS Custom Sounds/        # Auto-generated resource pack
\`\`\`

The exact \`.minecraft\` root may be replaced by a separate launcher profile/game directory.

Notable persisted settings include \`hudMode\`, \`style\`, \`audioEnabled\`, \`audioVolume\`, \`terrainWarnings\`, \`maneuverWarnings\`, \`avoidXaeroMinimap\`, \`minimapCorner\`, \`cruiseGuidance\`, \`mapFlightPanel\`, \`cruiseClearance\`, \`cruiseOverrideY\`, \`blocksPerRocket\`, and \`rocketReserve\`.

For manual file edits, close Minecraft first so in-game saves do not overwrite your changes. FMS destination coordinates are not persisted in this file.

## Development and building

**Repository:** [CoachSludge/FMS](https://github.com/CoachSludge/FMS)

\`\`\`powershell
git pull origin main
gradle clean build
gradle runClient
\`\`\`

On a Windows setup with Gradle 9.7.1 installed at \`C:\Gradle\`:

\`\`\`powershell
& "C:\Gradle\gradle-9.7.1\bin\gradle.bat" clean build
& "C:\Gradle\gradle-9.7.1\bin\gradle.bat" runClient
\`\`\`

The build should produce a mod JAR under \`build/libs/\`. Check the actual JAR filename and build results before distributing. \`gradle check\` runs the dependency-free \`FlightPlannerSmokeTest\` alongside other available checks.

### Project structure

| Location | Purpose |
| --- | --- |
| \`src/main/java/cc/fentware/fms/client/\` | Minecraft client HUD, settings, audio, navigation, Xaero bridge |
| \`src/main/java/cc/fentware/fms/core/\` | Calculation helpers and planning logic independent of Minecraft |
| \`src/main/resources/assets/fms/\` | Bundled warning recordings and sound mappings |
| \`src/test/\` | Smoke-test code |
| \`docs/ARCHITECTURE.md\` | Technical architecture |
| \`docs/ROADMAP.md\` | Planned development phases |

## Known limitations and troubleshooting

| Problem | What to check |
| --- | --- |
| FMS HUD is missing | Equip an Elytra, press **H**, check **HUD Visibility**, and confirm Fabric/API versions |
| Sound warnings are silent | Check **M**, FMS Audio ON, FMS volume, Minecraft sound settings, and specific sound set to \`off\` |
| Custom clip will not play | File is valid OGG Vorbis; click **REFRESH**, enable generated resource pack, reload resources |
| Xaero list is empty | Both compatible Xaero mods are installed; waypoint was saved; its folder format matches the current reader |
| Selected Xaero waypoint sends you to the wrong place | Check **world/dimension** listed in \`/fms xaero list\`; FMS does not auto-match them |
| Xaero HUD overlaps FMS | In FMS HUD settings, turn **Xaero Space ON** and select the minimap's actual corner; inspect GUI scale |
| World Map flight panel does not appear | Try \`/fms map on\`, \`/fms cruise on\`; Xaero's map screen hooks may have changed |
| Rocket count seems inaccurate | It is a configurable heuristic, not a physics prediction. Adjust \`/fms cruise reach\` |
| Cruise altitude appears too low or too high | The route may be partly **unloaded/unsurveyed**. Adjust manual cruise Y, but never assume it is safe |
| Gradle build fails | Verify **Java 25**, **Gradle 9.7.1**, dependency versions, and read the first actual compiler error |

**Prototype boundaries:** FMS does not currently provide click-to-fly map picking, advanced flight-route optimization, long-range Xaero terrain-cache surveying, multi-stop flight plans, verified safe clearance, automatic rockets, steering, assisted turns, autopilot, or emergency recovery. Its alerts and cruise recommendations are informative only.

## Roadmap

- **Phase 1 — Instruments and warnings:** Core HUD layouts, telemetry, sounds, warning priorities, settings. Implemented as a prototype; visual validation and tuning continue.
- **Phase 2 — Terrain and navigation:** Local terrain awareness, destination commands, Xaero waypoints, map-side flight summary, cruise/rocket estimates. In progress. Planned: direct map selection, better terrain survey, multi-stop planning, arrival handling.
- **Phase 3 — Flight assistance:** Potential flight director improvements, optional assisted flight, waypoint-following autopilot and emergency recovery. **Not implemented.**

For technical milestones see [docs/ROADMAP.md](docs/ROADMAP.md). This README describes the **current code**, rather than keeping earlier, superseded change logs in the main guide.

---

**FMS is developed by FentWare.** It is a community/client-side mod project; it is not affiliated with Mojang, Microsoft, or the Xaero mod authors. Check licenses and redistribution rights before publishing bundled or custom audio.