package org.vivecraft.utils;

import org.bukkit.Location;
import org.vivecraft.Reflector;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;

// Logic adapted from the official Vivecraft client source
public class AimFixHandler extends ChannelInboundHandlerAdapter {
    private final Connection netManager;

    public AimFixHandler(Connection netManager) {
        this.netManager = netManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ServerGamePacketListenerImpl listener = ((ServerGamePacketListenerImpl) this.netManager.getPacketListener());
        net.minecraft.server.level.ServerPlayer serverPlayer = listener.player;
        boolean isCapturedPacket = msg instanceof ServerboundUseItemPacket ||
            msg instanceof ServerboundUseItemOnPacket ||
            msg instanceof ServerboundPlayerActionPacket;

        VivePlayer vivePlayer = VSE.vivePlayers.get(serverPlayer.getUUID());

        if (vivePlayer == null || !vivePlayer.isVR() || !isCapturedPacket || serverPlayer.getServer() == null) {
            // we don't need to handle this packet, just defer to the next handler in the pipeline
            ctx.fireChannelRead(msg);
            return;
        }

        serverPlayer.getServer().submit(() -> {
            // Save all the current orientation data
            Vec3 pos = serverPlayer.position();
            Vec3 prevPos = new Vec3(serverPlayer.xo, serverPlayer.yo, serverPlayer.zo);
            float xRot = serverPlayer.getXRot();
            float yRot = serverPlayer.getYRot();
            float yHeadRot = serverPlayer.yHeadRot;
            float prevXRot = serverPlayer.xRotO;
            float prevYRot = serverPlayer.yRotO;
            float prevYHeadRot = serverPlayer.yHeadRotO;
            float eyeHeight = serverPlayer.getEyeHeight();

            Vec3 aimPos = null;
            // Check again in case of race condition
            if (vivePlayer != null && vivePlayer.isVR()) {
                // use the aim the client sent
                Location controllerPos = vivePlayer.getControllerPos(0);
                aimPos = new Vec3(controllerPos.getX(), controllerPos.getY(), controllerPos.getZ());
                Vec3 dir = vivePlayer.getControllerDir(0);

                // Inject our custom orientation data
                serverPlayer.setPosRaw(aimPos.x, aimPos.y, aimPos.z);
                serverPlayer.xo = aimPos.x;
                serverPlayer.yo = aimPos.y;
                serverPlayer.zo = aimPos.z;
                serverPlayer.setXRot((float) Math.toDegrees(Math.asin(-dir.y)));
                serverPlayer.setYRot((float) Math.toDegrees(Math.atan2(-dir.x, dir.z)));
                serverPlayer.xRotO = serverPlayer.getXRot();
                serverPlayer.yRotO = serverPlayer.yHeadRotO = serverPlayer.yHeadRot = serverPlayer.getYRot();
                
                Reflector.setFieldValue(Reflector.Entity_eyeHeight, serverPlayer, 0.0001F);

                // Set up offset to fix relative positions
                vivePlayer.offset = pos.subtract(aimPos);
            }

            // Call the packet handler directly
            try {
                if (this.netManager.isConnected()) {
                    try {
                        if (msg instanceof ServerboundUseItemPacket p) {
                            // need to alter the rotation for this one, for older clients that don't send the right rotation
                            new ServerboundUseItemPacket(p.getHand(), p.getSequence(), serverPlayer.getYRot(),
                                serverPlayer.getXRot()).handle(listener);
                        } else {
                            @SuppressWarnings("unchecked")
                            Packet<ServerGamePacketListenerImpl> packet = (Packet<ServerGamePacketListenerImpl>) msg;
                            packet.handle(listener);
                        }
                    } catch (RunningOnDifferentThreadException ignored) {
                        // Apparently might get thrown and can be ignored
                    }
                }
            } finally {
                // Vanilla uses SimpleChannelInboundHandler, which automatically releases
                // by default, so we're expected to release the packet once we're done.
                ReferenceCountUtil.release(msg);
            }

            // if the packet changed the player position, use that
            if ((aimPos != null && !serverPlayer.position().equals(aimPos)) ||
                (aimPos == null && !serverPlayer.position().equals(pos)))
            {
                pos = serverPlayer.position();
            }

            // Restore the original orientation data
            serverPlayer.setPosRaw(pos.x, pos.y, pos.z);
            serverPlayer.xo = prevPos.x;
            serverPlayer.yo = prevPos.y;
            serverPlayer.zo = prevPos.z;
            serverPlayer.setXRot(xRot);
            serverPlayer.setYRot(yRot);
            serverPlayer.yHeadRot = yHeadRot;
            serverPlayer.xRotO = prevXRot;
            serverPlayer.yRotO = prevYRot;
            serverPlayer.yHeadRotO = prevYHeadRot;
            Reflector.setFieldValue(Reflector.Entity_eyeHeight, serverPlayer, eyeHeight);

            // Reset offset
            if (vivePlayer != null) {
                vivePlayer.offset = Vec3.ZERO;
            }
        });
    }
}
