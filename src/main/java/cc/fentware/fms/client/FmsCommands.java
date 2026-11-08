package cc.fentware.fms.client;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

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
}
