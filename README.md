# FMS — Elytra Flight Management System

**Status: initial architecture / unintegrated Java core.** This is the first rough draft, not yet an installable Minecraft mod.

FMS is a planned client-side flight director for Minecraft Elytra travel, with Xaero's World Map terrain survey, destination coordinates, terrain-aware cruise targets, ETA, rocket reserves, wing condition, aviation-style voice warnings, and modular draggable HUD widgets.

## Initial behavior

- Input destination X/Z; plan a horizontal route and a conservative cruise target.
- Fully mapped terrain: highest sampled ground elevation plus configurable clearance (default **40 blocks**).
- Unmapped / partially mapped terrain: never assume it is clear; recommend **at least Y=250**, configurable.
- If mapped terrain calls for more than Y=250, use the higher recommendation, subject to a configured ceiling. A capped target carries a **ceiling-constrained** flag and is **not guaranteed safe**.
- ETA is undefined at zero horizontal speed; ultimately uses smoothed observed speed and climb/descent estimates.
- Give terrain and impact alerts priority over noncritical warnings.

> Y=250 is a fallback recommendation, not a promise of safety. Tall mountains, structures, unloaded chunks, insufficient rockets and the player's current trajectory can make it unsafe.

## Repository layout

- `src/main/java/cc/fentware/fms/core`: Minecraft-independent planning, telemetry, warning policy.
- `src/test`: small dependency-free smoke tests.
- `docs/ARCHITECTURE.md`: integrations, risk boundaries, staged roadmap.

Run the core smoke tests with a JDK 17+ installed:

```bash
bash scripts/test-core.sh
```

## Planned implementation order

1. Confirm exact Minecraft/Fabric loader/API versions and initialize a real Fabric client mod.
2. Connect client tick telemetry; implement destination command and persistent config.
3. Render movable HUD instruments (altitude, speed, heading, ETA, rockets, Elytra health).
4. Add Xaero terrain adapter with explicit unknown coverage and safe on-thread snapshots.
5. Route sampling, flight profile, forward-impact prediction, warnings, original audio assets.

**Not an autopilot:** FMS gives flight guidance; it does not steer or automate flight.


## HUD alpha (Fabric 26.3)

The first on-screen HUD implementation lives in `cc.fentware.fms.client`.

- **H** toggles the complete flight HUD.
- **J** opens the HUD layout editor; drag the flight and navigation panels, then press Escape to save.
- **/fms go <x> <z>** sets a temporary waypoint (client-side only).
- **/fms clear** removes that waypoint.
- Layout is persisted to `config/fms-hud.properties`; waypoints are session-only.
- HUD shows live airspeed (blocks/s), Y altitude, vertical speed, heading, Elytra remaining durability, inventory firework rockets, X/Z position, distance and simple speed-based ETA.
- The displayed unknown-terrain Y=250 is a **configuration recommendation**, not a scanned terrain clearance. Xaero integration and predicted collision detection are not active yet.

Build requirements: **JDK 25**, **Gradle 9.7.1**, Minecraft **26.3**, Fabric Loader **0.19.5**, Fabric API **0.161.0+26.3**. From the repository root use `gradle build` (a Gradle wrapper JAR is not yet checked in). Build locally; this repository intentionally has no GitHub Actions workflow.

**Verification:** Java core smoke tests have passed previously. The Fabric client and HUD have not yet been compiled or launched in a Minecraft client here. The Fabric HUD has not yet been compiled or launched here; resolve any API incompatibilities with a local Gradle build before installing.

## Development phases

See [docs/ROADMAP.md](docs/ROADMAP.md). Phase 1: HUD and basic telemetry warnings. Phase 2: Xaero terrain calculations, safe-altitude planning, and real terrain lookahead. Phase 3: opt-in, player-overridable assisted flight and experimental emergency recovery. All changes currently go directly to `main`.

## Phase 1.5 — aviation HUD (prototype)

- Default mode is **AVIATION**: transparent center reticle, pitch ladder, horizon, velocity-derived flight-path marker, heading tape, speed/altitude tapes, vertical speed, wing durability, rocket reserves, destination cue and warning text.
- **K** cycles `BOXED -> AVIATION -> HYBRID -> BOXED`, with the selection saved in `config/fms-hud.properties`.
- **H** toggles the entire HUD. **J** still opens the original draggable **boxed-panel** layout editor; per-instrument dragging for cockpit symbology is not implemented yet.
- **HYBRID** currently uses the same cockpit overlay plus the explicit unknown-terrain Y=250 advisory; it does not yet place configurable mini-panels.
- No bank/roll simulation and no terrain awareness yet. Flight-path symbology is based on sampled movement, not a physics simulator. Pitch ladder and tapes are an initial stylized pass for testing.

Build and test locally with `gradle clean build` then `gradle runClient`; no CI or GitHub Actions workflow.

## Phase 1 warning audio

Six stock `.ogg` files are registered through `assets/fms/sounds.json`: `bingo`, `warning`, `caution`, `pullup`, `altitude`, `mastercaution`.

Current warning-to-clip mapping:
- `BINGO FUEL` → `bingo.ogg`
- `SINK RATE` → `caution.ogg`
- `HIGH SPEED DESCENT` → `warning.ogg`
- `CHECK WINGS` and `CHECK WINGS - CRITICAL` → `mastercaution.ogg`
- `pullup.ogg` and `altitude.ogg` are installed but **not automatically triggered** yet; they need phase-two terrain/altitude validation.

The warning system plays one selected alert at a time, on changes (minimum 1.2 seconds separation), then repeats the same alert at most every 12 seconds. It clears warning state when Elytra flight ends.

- **M** toggles FMS warning sounds (persisted).
- **N** previews the master caution sound.
- Change `audioEnabled=true/false` and `audioVolume=0..100` in `config/fms-hud.properties` (edit while Minecraft is closed); these are interim settings until the full settings screen is built.
- Playback is client-side, independent of the HUD visibility toggle. Test with your Minecraft master/UI audio levels enabled.

Audio integration is source-committed but **not yet compiled or playtested** after this change. Run `gradle clean build` followed by `gradle runClient`. Redistribution rights for uploaded audio remain the responsibility of the project.

## FMS in-game settings (phase-one implementation)

Press **O** while in a world to open the new FMS settings screen:

- **HUD MODE** cycles `ALWAYS`, `ELYTRA_EQUIPPED` (default), and `ONLY_FLYING`. Equip an Elytra in the chest slot to reveal FMS when in the default mode; removing it hides the HUD. **H** remains the global HUD enable/disable toggle.
- **STYLE** cycles boxed, aviation, and hybrid.
- **AUDIO** enables/disables warning audio, with **VOL -** / **VOL +** stepping the FMS audio volume by 10%.
- Each of the five active warnings has a selectable stock sound (`bingo`, `warning`, `caution`, `pullup`, `altitude`, `mastercaution`, or `off`), plus a **TEST** button.
- Settings save to `config/fms-hud.properties`; **Done** closes the screen. Existing shortcuts remain available: H HUD, J boxed-layout editor, K HUD style, M audio toggle, N master-caution preview.

### Replacing sounds with your own clips

The menu selects from the six bundled sound event IDs; it does **not** import arbitrary MP3/OGG files or browse your file system yet. To substitute a clip, use a Minecraft resource pack that overrides the matching resource at `assets/fms/sounds/warnings/<name>.ogg` while preserving the existing filenames and event names. Convert to OGG Vorbis before adding the replacement. Reload resources after changing the pack.

The first pass has **no Xaero terrain detection**, no actual ground-proximity or pull-up activation, and no autopilot. `pullup` and `altitude` can be previewed or assigned to an existing alert as a substitute tone, but are not separate detected events yet.

Changes are pushed directly to `main`. They have not been compiled or in-game verified in this environment: run `gradle clean build` and `gradle runClient` locally.


## Mod Menu integration (optional)

FMS implements the Mod Menu `modmenu` entrypoint and provides a config screen factory. With [Mod Menu](https://modrinth.com/mod/modmenu) installed on Fabric 26.3, open **Mods → FMS → Configure** to launch the same sound/HUD settings screen available via **O** in-game. Closing returns to the previous Mod Menu screen.

The Gradle development runtime includes Mod Menu `21.0.0` from Terraformers Maven for testing. End users are not required to install Mod Menu: FMS still works independently with its **O** shortcut. If you package a JAR, do not distribute Mod Menu inside it.

Test: `gradle clean build` and `gradle runClient`. Check the Mods list and configure action on the title screen; verify that the in-game shortcut still works without Mod Menu. The new integration has not been locally compiled or launch-tested here.

## Settings layout fix

The settings screen now uses **HUD** and **AUDIO** tabs instead of one fixed-height panel. The AUDIO page contains five sound-assignment buttons with neighboring TEST buttons; click the assigned sound name to cycle through the six registered OGG clips or `off`. The smaller layout does not require scrolling at common GUI scales. Switch between pages using the tabs at the top. This code is committed to `main` but awaits a local Minecraft build/playtest.

## Xaero's Minimap HUD coexistence

FMS detects Xaero's Minimap when installed and offers **Xaero Space** in **FMS Settings → HUD** (enabled by default). Set **Minimap Corner** to match the position chosen in Xaero's own `Change Position` screen (defaults to top right). The boxed FMS flight/navigation panels will temporarily shift vertically if they overlap a reserved corner region; saved drag positions are left untouched. In AVIATION/HYBRID modes the main flight instruments stay in the screen center and do not move.

This is a conservative **corner safe-area**, **not** direct access to Xaero's live minimap position or shape, and it is not a universal overlap detector. Players using custom non-corner positions or unusually large minimaps may need to move widgets manually using J, or disable Xaero Space. The reserve uses approximate scaled GUI dimensions. It requires no Xaero dependency and does not affect normal FMS use when Xaero is absent.

This code is committed to `main` but needs a local Minecraft compile and visual test with Xaero's Minimap installed.

## User custom warning audio (OGG, phase one)

FMS now creates `config/fms/sounds/` in the active Minecraft game directory. Add up to **16** mono OGG Vorbis clips here, with simple filenames such as `my_warning.ogg` (letters, numbers, underscores and hyphens only; up to 48 characters before `.ogg`, case-sensitive lowercase extension). Clips larger than 8 MiB and files without an Ogg container header are ignored; the header check does not guarantee valid Vorbis encoding.

Go to **Mod Menu → FMS → Configure → Audio** and use **OPEN FOLDER** to access this location, then **REFRESH** to rescan it. FMS creates/updates an unpacked resource pack at `resourcepacks/FMS Custom Sounds/`. Under **Options → Resource Packs**, enable **FMS Custom Sounds** once; after a Refresh, reload resources using **F3+T**, or reopen Resource Packs to reload. This extra step is necessary for Minecraft's sound manager to see newly generated OGG resources. Assign sounds using the warning row selector (the `custom:<filename>` choices appear after Refresh), then TEST. Existing bundled sounds still work without the generated pack.

FMS deliberately does **not** execute or decode arbitrary files itself: it packages user-provided OGG files into the normal Minecraft resource pipeline. Settings persist in `config/fms-hud.properties`; assignments made to a missing custom file will fall back to `bingo` the next time settings are loaded. Currently, replacing or changing a custom clip needs REFRESH + Minecraft resource reload. Import picker and automatic resource-pack activation are future enhancements, not present functionality.

Development client folder: `FMS/run/config/fms/sounds/`. Normal launcher: `.minecraft/config/fms/sounds/`, or the game directory of the user's modpack/profile.

The custom-sounds implementation has been committed but not compiled or verified in-game in this environment.

## Terrain / altitude callouts (local first pass)

FMS now registers the three supplied recordings `lowterrain.ogg`, `terrain.ogg`, and `terrainahead.ogg` and uses existing `pullup.ogg` and `altitude.ogg`. Terrain awareness runs **only during Elytra flight**, sampling Minecraft's **already loaded** chunk heightmaps (MOTION_BLOCKING) at the current X/Z and up to ~48 blocks ahead along the player’s measured velocity. It never loads unseen chunks, and ignores unobserved samples. It predicts a short, straight-line path using current velocity; it is **not** guaranteed collision detection.

Current initial thresholds (tune after in-game flight testing):
- **ALTITUDE**: <25 blocks above local surface and descending faster than 3 blocks/sec.
- **LOW TERRAIN**: <7 blocks above local surface.
- **TERRAIN AHEAD**: projected path comes within 14 blocks of surface in the sampled horizon.
- **TERRAIN**: projected margin <7 blocks within 1.8 seconds.
- **PULL UP**: projected margin <3.5 blocks within 1.15 seconds (critical; immediately overrides audio transition delay).

There is a new **AGL** (above ground level) value in the cockpit HUD and a **TERRAIN** settings tab in Mod Menu → FMS → Configure for enabling warnings and changing/testing individual recordings.

**Not yet Xaero terrain-cache integration:** Xaero's Minimap has optional FMS screen-placement compatibility, but FMS does not read Xaero's historical/remote terrain cache. Local Minecraft chunk heightmaps are available without a dependency on Xaero. No guarantees for unloaded terrain, caves, roofs, overhead or lateral obstacles, or sudden velocity changes. The critical warning is advisory and does not control the player. The implementation has not been compiled or validated in-game here; please test on a backup world and report the first Gradle compiler error if any.
