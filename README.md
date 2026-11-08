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
