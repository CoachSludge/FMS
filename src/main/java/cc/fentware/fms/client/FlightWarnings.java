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

        TerrainAwareness.Reading terrain = TerrainAwareness.current();
        // Only known, loaded terrain. Terrain threats supersede other flight advisories.
        switch (terrain.threat()) {
            case PULL_UP -> { return new Alert("PULL UP", Severity.CRITICAL); }
            case TERRAIN -> { return new Alert("TERRAIN", Severity.CRITICAL); }
            default -> { }
        }
        // Elytra durability in Minecraft is usually 432 points; percentage
        // thresholds provide meaningful landing warnings before the last few HP.
        int max = chest.is(Items.ELYTRA) ? chest.getMaxDamage() : 0;
        int percent = max > 0 ? (int) Math.ceil(durability * 100.0 / max) : 100;
        boolean lowFuel = rockets <= 3;
        if (percent <= 4 || durability <= 5)
            return new Alert("LAND NOW", Severity.CRITICAL);
        if (percent <= 8 && lowFuel)
            return new Alert("LAND NOW", Severity.CRITICAL);
        if (percent <= 8)
            return new Alert("LAND SOON", Severity.CRITICAL);
        if (descent > 32 && horizontalSpeed > 15) return new Alert("HIGH SPEED DESCENT", Severity.CRITICAL);
        switch (terrain.threat()) {
            case TERRAIN_AHEAD -> { return new Alert("TERRAIN AHEAD", Severity.CAUTION); }
            case LOW_TERRAIN -> { return new Alert("LOW TERRAIN", Severity.CAUTION); }
            case ALTITUDE -> { return new Alert("ALTITUDE", Severity.CAUTION); }
            default -> { }
        }
        if (percent <= 15)
            return new Alert("LOW ELYTRA", Severity.CAUTION);
        if (percent <= 35)
            return new Alert("ELYTRA HEALTH", Severity.CAUTION);
        if (descent > 16) return new Alert("SINK RATE", Severity.CAUTION);
        if (rockets <= 3) return new Alert("BINGO FUEL", Severity.CAUTION);
        return Alert.none();
    }
}
