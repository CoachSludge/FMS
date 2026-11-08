package cc.fentware.fms.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Phase-one warnings: these use only live player telemetry.
 * No terrain avoidance, impact prediction, or automatic controls until later phases.
 */
public final class FlightWarnings {
    public enum Severity { NONE, CAUTION, CRITICAL }
    public record Alert(String message, Severity severity) {
        public static Alert none() { return new Alert("", Severity.NONE); }
    }

    private FlightWarnings() {}

    public static Alert current() {
        var player = Minecraft.getInstance().player;
        if (player == null || !player.isFallFlying()) return Alert.none();

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        int durability = chest.is(Items.ELYTRA) ? chest.getMaxDamage() - chest.getDamageValue() : 0;
        double descent = -player.getDeltaMovement().y * 20.0;
        double horizontalSpeed = Math.hypot(player.getDeltaMovement().x, player.getDeltaMovement().z) * 20.0;
        int rockets = player.getInventory().countItem(Items.FIREWORK_ROCKET);

        // Phase one is deliberately conservative: no false claim of imminent terrain collision.
        if (durability <= 5) return new Alert("CHECK WINGS - CRITICAL", Severity.CRITICAL);
        if (descent > 32 && horizontalSpeed > 15) return new Alert("HIGH SPEED DESCENT", Severity.CRITICAL);
        if (durability <= 20) return new Alert("CHECK WINGS", Severity.CAUTION);
        if (descent > 16) return new Alert("SINK RATE", Severity.CAUTION);
        if (rockets <= 3) return new Alert("BINGO FUEL", Severity.CAUTION);
        return Alert.none();
    }
}
