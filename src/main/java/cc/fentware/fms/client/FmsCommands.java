package cc.fentware.fms.client;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
                                FmsClient.setDestination(p.x(), p.z());
                                tell("[FMS] Flight destination: " + p.name() + " at X " + p.x()
                                        + " Z " + p.z() + ". Verify world/dimension: "
                                        + p.world() + " / " + p.dimension());
                                return 1;
                            }))))
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
