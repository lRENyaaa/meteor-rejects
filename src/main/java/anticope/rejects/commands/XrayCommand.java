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

public class XrayCommand extends Command {


    public XrayCommand() {
        super("ghost-v2", "Remove ghost blocks & bypass AntiXray", "aax-v2", "anti-anti-xray-v2");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(ctx -> {
            execute(4);
            return SINGLE_SUCCESS;
        });
        builder.then(argument("radius", IntegerArgumentType.integer(1)).executes(ctx -> {
            int radius = IntegerArgumentType.getInteger(ctx, "radius");
            execute(radius);
            return SINGLE_SUCCESS;
        }));
    }
    private void execute(int radius) {
        ClientPlayNetworkHandler conn = mc.getNetworkHandler();
        if (conn == null)
            return;
        BlockPos pos = mc.player.getBlockPos();
        new XrayThread(conn, pos, radius).start();
    }


    static class XrayThread extends Thread {
        private final ClientPlayNetworkHandler conn;
        private final BlockPos pos;
        private final int radius;

        private XrayThread(ClientPlayNetworkHandler conn,BlockPos pos,int radius){
            this.conn = conn;
            this.pos = pos;
            this.radius = radius;
        }

        public void run(){
            int i = 0;
            for (int dx = -radius; dx <= radius; dx++)
                for (int dy = -radius; dy <= radius; dy++)
                    for (int dz = -radius; dz <= radius; dz++) {
                        PlayerActionC2SPacket packet = new PlayerActionC2SPacket(
                                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
                                new BlockPos(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz), Direction.UP);
                        conn.sendPacket(packet);
                        System.out.println("check: " + pos.getX() + dx + "," + pos.getY() + dy + ", " + pos.getZ() + dz);
                        if (i++ == 100) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            i=0;
                        }
                    }
        }
    }
}
