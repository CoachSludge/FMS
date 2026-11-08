# FMS development phases

## Phase 1 — Crude HUD + warnings (in progress)

- Fabric 26.3 client-only HUD showing altitude, speed, heading, vertical speed, Elytra health, rocket count, destination range and provisional ETA
- Move the two initial panels with the layout editor; persist panel positions locally
- Local `/fms go X Z` and `/fms clear` commands
- First-pass non-terrain warnings: wing durability, rocket reserve, sink rate and high-speed descent
- **Important:** phase-one warnings are HUD-only indicators, *not* verified terrain collision predictions; no automatic control and no promise of safety
- Follow-up: sound/voice clips, user-settable thresholds, alert debounce, more modular HUD widgets, playtest/build fixes

## Phase 2 — Xaero map and flight calculations

- Create optional version-specific integration with Xaero's World Map
- Distinguish known terrain from unknown/unvisited map coverage
- Route survey including corridor width, height samples and terrain confidence
- Recommended climb/cruise/descent profile; fully unknown Overworld fallback **Y=250**
- Map-based lookahead plus live loaded-chunk collision queries, with different warnings for unknown versus known threats
- Estimate rocket needs and ETA from measured performance; recalculate while flying
- Do not treat Y=250 as guaranteed safe: terrain and structures may exceed it

## Phase 3 — Optional assisted flight and emergency recovery (design only)

- All automatic flight controls **off by default** and controlled by explicit keybinds
- Modes: flight director only, optional autopilot navigation, optional AFK/low-altitude recovery
- Player look/movement inputs take precedence immediately; manual override always available
- When armed, monitor persistent steep descent and excessive forward speed, plus terrain lookahead from phase two, before trying to correct course
- Recovery strategy: gentle pitch-up where feasible, then search for safe water or landing zones **only from reliable observed world data**
- Emergency water-bucket clutch is a **last-resort experimental feature** requiring a bucket, suitable placement conditions and explicit user opt-in; may fail due to latency, world rules, reach, water restrictions or missing terrain data
- Avoid repeated control oscillation, unintended item use, false AFK detection and unsafe assumptions about unl​oaded chunks
- In multiplayer, respect server rules; do not guarantee survival or fully automated safety

## Development workflow

Commit directly to `main` for now. No GitHub Actions workflow. Validate locally with Gradle and a real Minecraft instance before calling a release installable.
