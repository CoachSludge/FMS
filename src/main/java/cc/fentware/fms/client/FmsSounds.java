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

    private static final SoundEvent[] CUSTOM = new SoundEvent[CustomSounds.MAX_SOUNDS];
    static {
        for (int i = 0; i < CUSTOM.length; i++) CUSTOM[i] = registerCustom(CustomSounds.slotId(i));
    }

    public static final String[] NAMES = {"bingo", "warning", "caution", "pullup", "altitude", "mastercaution", "off"};

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

    public static void preview(SoundEvent sound) {
        play(sound);
    }

    public static void play(SoundEvent sound) {
        if (sound == null || !HudSettings.audioEnabled || HudSettings.audioVolume <= 0) return;
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(sound, 1.0F, HudSettings.audioVolume / 100.0F));
    }
}
