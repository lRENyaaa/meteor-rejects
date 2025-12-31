package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.concurrent.CompletableFuture;

public class AutoAntiAntiXray extends Module {

    private final int radius = 4;
    private BlockPos pos = null;

    public AutoAntiAntiXray() {
        super(MeteorRejectsAddon.CATEGORY, "auto-anti-anti-xray", "Auto remove ghost blocks & bypass AntiXray");
    }
    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        CompletableFuture.runAsync(this::updateBlockPos);
    }

    private void updateBlockPos(){
        BlockPos tempPos = MeteorClient.mc.player.getBlockPos();
        if (pos != null && pos.getX() == tempPos.getX() && pos.getY() == tempPos.getY() && pos.getZ() == tempPos.getZ()) return;
        pos = tempPos;
        execute();
    }


    private synchronized void execute(){
        final BlockPos pos = this.pos;
        ClientPlayNetworkHandler conn = MeteorClient.mc.getNetworkHandler();
        if (conn == null || pos == null)
            return;
        for (int dx = -radius; dx <= radius; dx++)
            for (int dy = -radius; dy <= radius; dy++)
                for (int dz = -radius; dz <= radius; dz++) {
                    PlayerActionC2SPacket packet = new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
                            new BlockPos(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz), Direction.UP);
                    conn.sendPacket(packet);
                    System.out.println("check: " + pos.getX() + dx + "," + pos.getY() + dy + ", " + pos.getZ() + dz);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
    }
}
