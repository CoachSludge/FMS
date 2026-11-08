package cc.fentware.fms.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
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

    public static final String[] NAMES = {"bingo", "warning", "caution", "pullup", "altitude", "mastercaution", "off"};

    private FmsSounds() {}

    public static boolean validName(String name) {
        for (String value : NAMES) if (value.equals(name)) return true;
        return false;
    }

    public static String nextName(String name) {
        for (int i = 0; i < NAMES.length; i++) {
            if (NAMES[i].equals(name)) return NAMES[(i + 1) % NAMES.length];
        }
        return NAMES[0];
    }

    public static SoundEvent resolve(String name) {
        return switch (name) {
            case "bingo" -> BINGO;
            case "warning" -> WARNING;
            case "caution" -> CAUTION;
            case "pullup" -> PULLUP;
            case "altitude" -> ALTITUDE;
            case "mastercaution" -> MASTER_CAUTION;
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

    public static void initialize() { /* Trigger static registration during client startup. */ }

    public static void preview(SoundEvent sound) {
        play(sound);
    }

    public static void play(SoundEvent sound) {
        if (sound == null || !HudSettings.audioEnabled || HudSettings.audioVolume <= 0) return;
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(sound, 1.0F, HudSettings.audioVolume / 100.0F));
    }
}
