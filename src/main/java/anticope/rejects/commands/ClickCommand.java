package anticope.rejects.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ClickCommand extends Command {

    public ClickCommand() {
        super("click", "Click a block", "click");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {

        builder.then(argument("x", IntegerArgumentType.integer()).then(argument("y", IntegerArgumentType.integer()).then(argument("z", IntegerArgumentType.integer()).executes(ctx -> {
            int x = IntegerArgumentType.getInteger(ctx, "x");
            int y = IntegerArgumentType.getInteger(ctx, "y");
            int z = IntegerArgumentType.getInteger(ctx, "z");
            execute(x, y, z);
            return SINGLE_SUCCESS;
        }))));
    }

    private void execute(int x, int y, int z) {
        ClientPlayNetworkHandler conn = mc.getNetworkHandler();
        if (conn == null)
            return;
        PlayerActionC2SPacket packet = new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                new BlockPos(x, y, z), Direction.UP);
        conn.sendPacket(packet);

    }
}
