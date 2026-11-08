package cc.fentware.fms.client;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import cc.fentware.fms.core.CruiseMath;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

/** Local-only waypoint commands; no server command or permissions needed. */
public final class FmsCommands {
    private FmsCommands() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(literal("fms")
                .then(literal("go")
                    .then(argument("x", DoubleArgumentType.doubleArg())
                        .then(argument("z", DoubleArgumentType.doubleArg())
                            .executes(context -> {
                                double x = DoubleArgumentType.getDouble(context, "x");
                                double z = DoubleArgumentType.getDouble(context, "z");
                                FmsClient.setDestination(x, z);
                                if (Minecraft.getInstance().player != null) {
                                    Minecraft.getInstance().player.sendSystemMessage(
                                        Component.literal("[FMS] Destination set: X " + Math.round(x) + ", Z " + Math.round(z)));
                                }
                                return 1;
                            }))))
                .then(literal("xaero")
                    .then(literal("list").executes(context -> {
                        List<XaeroWaypoints.Waypoint> points = XaeroWaypoints.list();
                        tell("[FMS] Xaero waypoints: " + points.size() + " found. Use /fms xaero select <number>.");
                        int shown = Math.min(20, points.size());
                        for (int i = 0; i < shown; i++) {
                            XaeroWaypoints.Waypoint p = points.get(i);
                            tell("[FMS] " + (i + 1) + ". " + p.name() + " [" + p.world()
                                    + " / " + p.dimension() + "] X " + p.x() + " Z " + p.z());
                        }
                        if (points.size() > shown) tell("[FMS] Showing first 20 waypoints.");
                        return shown;
                    }))
                    .then(literal("select")
                        .then(argument("number", IntegerArgumentType.integer(1))
                            .executes(context -> {
                                int selected = IntegerArgumentType.getInteger(context, "number");
                                List<XaeroWaypoints.Waypoint> points = XaeroWaypoints.list();
                                if (selected > points.size()) {
                                    tell("[FMS] Invalid Xaero waypoint number. Run /fms xaero list.");
                                    return 0;
                                }
                                XaeroWaypoints.Waypoint p = points.get(selected - 1);
                                // User explicitly selects a displayed world/dimension entry.
                                // Xaero files can contain points from other saves; be sure to
                                // select from the current world and matching dimension.
                                FmsClient.setDestination(p.x(), p.z(), p.name());
                                tell("[FMS] Flight destination: " + p.name() + " at X " + p.x()
                                        + " Z " + p.z() + ". Verify world/dimension: "
                                        + p.world() + " / " + p.dimension());
                                return 1;
                            }))))
                .then(literal("cruise")
                    .then(literal("on").executes(context -> {
                        HudSettings.cruiseGuidance = true;
                        HudSettings.save();
                        tell("[FMS] Cruise flight director ON (advisory only).");
                        return 1;
                    }))
                    .then(literal("off").executes(context -> {
                        HudSettings.cruiseGuidance = false;
                        HudSettings.save();
                        tell("[FMS] Cruise flight director OFF.");
                        return 1;
                    }))
                    .then(literal("auto").executes(context -> {
                        HudSettings.cruiseOverrideY = -1;
                        HudSettings.save();
                        tell("[FMS] Cruise Y set to terrain-advisory estimate.");
                        return 1;
                    }))
                    .then(literal("y")
                        .then(argument("altitude", IntegerArgumentType.integer(64, 300))
                            .executes(context -> {
                                HudSettings.cruiseOverrideY = IntegerArgumentType.getInteger(context, "altitude");
                                HudSettings.save();
                                tell("[FMS] Cruise Y override: " + HudSettings.cruiseOverrideY);
                                return 1;
                            })))
                    .then(literal("reach")
                        .then(argument("blocksPerRocket", IntegerArgumentType.integer(100, 2000))
                            .executes(context -> {
                                HudSettings.blocksPerRocket = IntegerArgumentType.getInteger(context, "blocksPerRocket");
                                HudSettings.save();
                                tell("[FMS] Estimated range: " + HudSettings.blocksPerRocket + " blocks/rocket (assumption).");
                                return 1;
                            })))
                    .then(literal("reserve")
                        .then(argument("rockets", IntegerArgumentType.integer(0, 16))
                            .executes(context -> {
                                HudSettings.rocketReserve = IntegerArgumentType.getInteger(context, "rockets");
                                HudSettings.save();
                                tell("[FMS] Rocket reserve set: " + HudSettings.rocketReserve);
                                return 1;
                            })))
                    .then(literal("status").executes(context -> {
                        CruiseGuidance.Guidance g = CruiseGuidance.current();
                        if (g == null) {
                            tell("[FMS] Set a destination with /fms go X Z first.");
                        } else {
                            tell("[FMS] CRZ Y " + g.recommendedY() + " (" + g.altitudeAdvice()
                                    + "), heading " + Math.round(g.bearing())
                                    + ", turn " + Math.round(g.headingError())
                                    + ", rockets approx " + g.fuel().totalRockets() + " ("
                                    + g.fuel().travelRockets() + " + " + g.fuel().reserveRockets()
                                    + " reserve), carried " + g.fuel().availableRockets()
                                    + ", route survey " + (g.completeSurvey() ? "sampled" : "INCOMPLETE"));
                        }
                        return 1;
                    })))
                .then(literal("map")
                    .then(literal("on").executes(context -> {
                        HudSettings.mapFlightPanel = true;
                        HudSettings.save();
                        tell("[FMS] Xaero World Map flight panel ON.");
                        return 1;
                    }))
                    .then(literal("off").executes(context -> {
                        HudSettings.mapFlightPanel = false;
                        HudSettings.save();
                        tell("[FMS] Xaero World Map flight panel OFF.");
                        return 1;
                    })))
                .then(literal("clear").executes(context -> {
                    FmsClient.clearDestination();
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.sendSystemMessage(Component.literal("[FMS] Destination cleared."));
                    }
                    return 1;
                }))
            )
        );
    }

    private static void tell(String message) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal(message));
        }
    }
}
