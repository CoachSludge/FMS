# FMS technical outline (draft 0)

## Modules

1. `core`: pure Java records and calculations. Keep testable without loading Minecraft.
2. `client-minecraft`: future Fabric event handlers; read player position, movement, Elytra durability, rockets, heading, dimension and world height.
3. `terrain-xaero`: optional/version-pinned Xaero's World Map integration; surveyed *coverage* and surface height. Do not copy or redistribute Xaero source.
4. `terrain-live`: collision/height data from currently loaded client chunks to catch cliffs, structures, and obstacles; unknown where missing.
5. `navigation`: waypoint UX, line sampling, terrain corridor, altitude profile, glide/rocket estimation and ETA.
6. `warnings`: predictive forward path check, precedence, cooldowns and configurable audio.
7. `hud`: multiple independent widgets with draggable positions, resize, scale, presets and screen-size-aware anchoring.

## Key rules

- Default unknown-terrain target Y=250 (Overworld), configurable; do not call it guaranteed safe.
- Survey status is `KNOWN`, `PARTIAL`, or `UNKNOWN`. Absent samples count as unknown.
- Preserve unknown gaps even if the route contains mapped segments. When any unknown segment remains, maintain at least fallback cruise altitude, unless world ceiling prevents it.
- Use actual current dimension bounds (never assume 320 for every dimension). Consider separate Nether/End presets after explicit design review.
- Check whether target altitude is reachable *before* an unknown boundary using observed climb rate / rocket reserves.
- Distinguish surface altitude from collision geometry. Xaero map colors/height alone do not give full structure or cave safety.
- Never access Minecraft world or Xaero mutable data from a worker thread. Capture immutable client-thread snapshots, then calculate off-thread with throttling.
- Alert priority: imminent collision, terrain, damaged wings/low fuel, unknown terrain; debounce repeated audio and never reproduce proprietary audio recordings.
- ETA and rocket-range estimates are provisional and should expose uncertainty instead of false precision.
- All HUD components independently toggleable, draggable, scaleable, colorable; settings saved locally.
- Client-only by default; no server packets that would require an FMS server mod. No automated steering.

## First milestone acceptance criteria

- Client launches with an empty config and no crash.
- Command or keybind sets X/Z target and clears it.
- HUD shows live Y, vertical speed, horizontal speed, rockets, Elytra remaining durability, route distance and ETA.
- Unknown-terrain mode shows `TERRAIN UNKNOWN` and `CLIMB TO Y 250` when appropriate.
- Switching dimensions invalidates terrain plans and clears warnings from the previous dimension.
- Dragging or toggling widgets persists across game restarts.
- No memory leak or render-thread stalls from map scanning.

## Open dependencies

- Pin target game version and obtain verified matching Fabric Loom, loader, Fabric API and Java toolchain versions.
- Verify Xaero map API/licensing and supported per-version integration method. Use an adapter rather than coupling `core` directly to internal classes.
- Verify optional integration with roll/camera mods before claiming a physical bank gauge.
