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
