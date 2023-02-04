package org.adde0109.ambassador.velocity;

import com.velocitypowered.proxy.connection.ConnectionTypes;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.client.HandshakeSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.Handshake;
import io.netty.buffer.ByteBuf;
import org.adde0109.ambassador.forge.ForgeConstants;

public class VelocityHandshakeSessionHandler implements MinecraftSessionHandler {
  private final HandshakeSessionHandler original;
  private final MinecraftConnection connection;

  public VelocityHandshakeSessionHandler(HandshakeSessionHandler original, MinecraftConnection connection) {
    this.original = original;
    this.connection = connection;
  }

  @Override
  public boolean handle(Handshake handshake) {
    handshake.handle(original);
    if (connection.getType() == ConnectionTypes.VANILLA) {
      final String[] markerSplit = handshake.getServerAddress().split("\0");
      if (connection.getState() == StateRegistry.LOGIN && markerSplit.length > 1) {
        switch (markerSplit[1]) {
          case "FML2" -> connection.setType(ForgeConstants.ForgeFML2);
          case "FML3" -> connection.setType(ForgeConstants.ForgeFML3);
        }
      }
    }
    return true;
  }


  @Override
  public void handleGeneric(MinecraftPacket packet) {
    original.handleGeneric(packet);
  }

  @Override
  public void handleUnknown(ByteBuf buf) {
    original.handleUnknown(buf);
  }
}
