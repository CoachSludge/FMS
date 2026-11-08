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
