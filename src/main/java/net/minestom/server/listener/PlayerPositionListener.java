package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.network.packet.client.play.ClientPlayerPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionAndRotationPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.chunk.ChunkUtils;

public class PlayerPositionListener {

    public static void playerPacketListener(ClientPlayerPacket packet, Player player) {
        player.refreshOnGround(packet.onGround);
    }

    public static void playerLookListener(ClientPlayerRotationPacket packet, Player player) {
        final Position playerPosition = player.getPosition();
        final float x = playerPosition.getX();
        final float y = playerPosition.getY();
        final float z = playerPosition.getZ();
        final float yaw = packet.yaw;
        final float pitch = packet.pitch;
        final boolean onGround = packet.onGround;
        processMovement(player, x, y, z, yaw, pitch, onGround);
    }

    public static void playerPositionListener(ClientPlayerPositionPacket packet, Player player) {
        final Position playerPosition = player.getPosition();
        final float x = (float) packet.x;
        final float y = (float) packet.y;
        final float z = (float) packet.z;
        final float yaw = playerPosition.getYaw();
        final float pitch = playerPosition.getPitch();
        final boolean onGround = packet.onGround;
        processMovement(player, x, y, z, yaw, pitch, onGround);
    }

    public static void playerPositionAndLookListener(ClientPlayerPositionAndRotationPacket packet, Player player) {
        final float x = (float) packet.x;
        final float y = (float) packet.y;
        final float z = (float) packet.z;
        final float yaw = packet.yaw;
        final float pitch = packet.pitch;
        final boolean onGround = packet.onGround;
        processMovement(player, x, y, z, yaw, pitch, onGround);
    }

    private static void processMovement(Player player, float x, float y, float z,
                                        float yaw, float pitch, boolean onGround) {

        // Try to move in an unloaded chunk, prevent it
        if (ChunkUtils.isChunkUnloaded(player.getInstance(), x, z)) {
            player.teleport(player.getPosition());
            return;
        }

        Position newPosition = new Position(x, y, z, yaw, pitch);
        PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(player, newPosition);
        player.callEvent(PlayerMoveEvent.class, playerMoveEvent);
        if (!playerMoveEvent.isCancelled()) {
            // Move the player
            newPosition = playerMoveEvent.getNewPosition();
            player.refreshPosition(newPosition.getX(), newPosition.getY(), newPosition.getZ());
            player.refreshView(newPosition.getYaw(), newPosition.getPitch());
            player.refreshOnGround(onGround);
        } else {
            player.teleport(player.getPosition());
        }
    }

}
