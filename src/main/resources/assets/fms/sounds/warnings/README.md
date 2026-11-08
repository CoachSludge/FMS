# FMS stock warning audio (F-16 set)

Drop your audio clips into this directory. Use **.ogg (Vorbis)** for Minecraft's built-in sound engine; MP3 source files may be kept temporarily for conversion, but cannot be directly registered as ordinary Minecraft sound assets.

Expected filenames (lowercase, no spaces):

| Your clip | Rename to |
|---|---|
| bingo | `bingo.ogg` |
| warning | `warning.ogg` |
| caution | `caution.ogg` |
| pullup | `pullup.ogg` |
| alt | `alt.ogg` |
| mastercaution | `mastercaution.ogg` |

The name **F-16** describes the sound pack as a whole; if you have a separate seventh audio clip named `f16`, save that as `f16.ogg` and we'll decide which alert it should play for.

**Status:** folder only. No events are hooked up to these files yet. We'll add `assets/fms/sounds.json`, warning priority/playback logic, and a sound settings window after the clips are available.

For distributed releases, confirm that each clip is original or that redistribution is permitted.
