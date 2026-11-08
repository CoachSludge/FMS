package cc.fentware.fms.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/** Client-only, non-positional warning audio, backed by assets/fms/sounds.json. */
public final class FmsSounds {
    public static final SoundEvent BINGO = register("bingo");
    public static final SoundEvent WARNING = register("warning");
    public static final SoundEvent CAUTION = register("caution");
    public static final SoundEvent PULLUP = register("pullup");
    public static final SoundEvent ALTITUDE = register("altitude");
    public static final SoundEvent MASTER_CAUTION = register("mastercaution");
    public static final SoundEvent LOW_TERRAIN = register("lowterrain");
    public static final SoundEvent TERRAIN = register("terrain");
    public static final SoundEvent TERRAIN_AHEAD = register("terrainahead");
    public static final SoundEvent ELYTRA = register("elytra");
    public static final SoundEvent LOW_ELYTRA = register("lowelytra");
    public static final SoundEvent LAND = register("land");
    public static final SoundEvent LAND_NOW = register("landnow");

    private static final SoundEvent[] CUSTOM = new SoundEvent[CustomSounds.MAX_SOUNDS];
    static {
        for (int i = 0; i < CUSTOM.length; i++) CUSTOM[i] = registerCustom(CustomSounds.slotId(i));
    }

    public static final String[] NAMES = {"bingo", "warning", "caution", "pullup", "altitude", "mastercaution", "lowterrain", "terrain", "terrainahead", "elytra", "lowelytra", "land", "landnow", "off"};

    private FmsSounds() {}

    public static boolean validName(String name) {
        return CustomSounds.valid(name, NAMES);
    }

    public static String nextName(String name) {
        return CustomSounds.next(name, NAMES);
    }

    public static SoundEvent resolve(String name) {
        int index = CustomSounds.indexOf(name);
        if (index >= 0 && index < CUSTOM.length) return CUSTOM[index];
        return switch (name) {
            case "bingo" -> BINGO;
            case "warning" -> WARNING;
            case "caution" -> CAUTION;
            case "pullup" -> PULLUP;
            case "altitude" -> ALTITUDE;
            case "mastercaution" -> MASTER_CAUTION;
            case "lowterrain" -> LOW_TERRAIN;
            case "terrain" -> TERRAIN;
            case "terrainahead" -> TERRAIN_AHEAD;
            case "elytra" -> ELYTRA;
            case "lowelytra" -> LOW_ELYTRA;
            case "land" -> LAND;
            case "landnow" -> LAND_NOW;
            default -> null;
        };
    }

    public static void previewName(String name) {
        SoundEvent event = resolve(name);
        if (event != null) play(event);
    }

    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("fms", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    private static SoundEvent registerCustom(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("fms_custom", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void initialize() {
        CustomSounds.refresh();
    }

    /** Single FMS audio channel. Interrupts previous clips without touching other mods. */
    private static SoundInstance activeWarning;

    public static void preview(SoundEvent sound) { play(sound); }

    public static void stopWarningAudio() {
        if (activeWarning != null) {
            Minecraft.getInstance().getSoundManager().stop(activeWarning);
            activeWarning = null;
        }
    }

    public static boolean isWarningPlaying() {
        return activeWarning != null && Minecraft.getInstance().getSoundManager().isActive(activeWarning);
    }

    public static void play(SoundEvent sound) {
        // A new alert or a preview replaces the previous FMS sound in full.
        stopWarningAudio();
        if (sound == null || !HudSettings.audioEnabled || HudSettings.audioVolume <= 0) return;
        activeWarning = SimpleSoundInstance.forUI(sound, 1.0F, HudSettings.audioVolume / 100.0F);
        Minecraft.getInstance().getSoundManager().play(activeWarning);
    }
}
