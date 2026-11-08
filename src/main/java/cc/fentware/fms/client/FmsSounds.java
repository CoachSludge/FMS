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

    private FmsSounds() {}

    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("fms", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void initialize() { /* Trigger static registration during client startup. */ }

    public static void preview(SoundEvent sound) {
        play(sound);
    }

    public static void play(SoundEvent sound) {
        if (!HudSettings.audioEnabled || HudSettings.audioVolume <= 0) return;
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(sound, 1.0F, HudSettings.audioVolume / 100.0F));
    }
}
