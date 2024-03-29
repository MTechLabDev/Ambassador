package org.adde0109.ambassador.forge;

import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.backend.BackendConnectionPhase;
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.network.Connections;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.AvailableCommandsPacket;
import com.velocitypowered.proxy.protocol.packet.LoginPluginMessagePacket;
import com.velocitypowered.proxy.protocol.packet.PluginMessagePacket;
import io.netty.buffer.ByteBuf;
import org.adde0109.ambassador.forge.pipeline.CommandDecoderErrorCatcher;
import org.adde0109.ambassador.forge.pipeline.ForgeLoginWrapperDecoder;

public enum VelocityForgeBackendConnectionPhase implements BackendConnectionPhase {
  NOT_STARTED() {
    @Override
    VelocityForgeBackendConnectionPhase nextPhase() {
      return WAITING_FOR_ACK;
    }

    @Override
    public boolean consideredComplete() {
      //Safe if the server hasn't initiated the handshake yet.
      return true;
    }
  },
  WAITING_FOR_ACK() {
    @Override
    public void onLoginSuccess(VelocityServerConnection serverCon, ConnectedPlayer player) {
      serverCon.setConnectionPhase(VelocityForgeBackendConnectionPhase.COMPLETE);

      serverCon.getConnection().getChannel().pipeline().addBefore(Connections.MINECRAFT_DECODER,
              ForgeConstants.COMMAND_ERROR_CATCHER,
              new CommandDecoderErrorCatcher(serverCon.getConnection().getProtocolVersion(),player));
    }

    @Override
    void onTransitionToNewPhase(VelocityServerConnection connection) {
      MinecraftConnection mc = connection.getConnection();
      if (mc != null) {
        //This looks ugly. But unless the player didn't have a FML marker, we're fine.
        mc.setType(connection.getPlayer().getConnection().getType());
      }
    }
  },

  COMPLETE() {
    @Override
    public boolean consideredComplete() {
      return true;
    }
  };



  VelocityForgeBackendConnectionPhase() {
  }

  public void handle(VelocityServerConnection server, ConnectedPlayer player, LoginPluginMessagePacket message) {

    VelocityForgeBackendConnectionPhase newPhase = getNewPhase(server,message);

    server.setConnectionPhase(newPhase);

    //Reset client if not ready to receive new handshake
    VelocityForgeClientConnectionPhase clientPhase = (VelocityForgeClientConnectionPhase) player.getPhase();
    if (clientPhase != VelocityForgeClientConnectionPhase.NOT_STARTED) {
      clientPhase.resetConnectionPhase(player);
      if (clientPhase == VelocityForgeClientConnectionPhase.COMPLETE)
      {
        //Do not continue if the client can't reset.
        return;
      }
    }
    message.retain();
    player.getConnection().write(message);
    //Forge server
    //To avoid unnecessary resets, we wait until we get the handshake even if we know that we should
    //reset because that the previous server was Forge.

    ForgeLoginWrapperDecoder decoder = (ForgeLoginWrapperDecoder) player.getConnection()
            .getChannel().pipeline().get(ForgeConstants.FORGE_HANDSHAKE_DECODER);
    decoder.registerLoginWrapperID(message.getId());
  }

  public void onLoginSuccess(VelocityServerConnection serverCon, ConnectedPlayer player) {
  }

  void onTransitionToNewPhase(VelocityServerConnection connection) {
  }

  VelocityForgeBackendConnectionPhase nextPhase() {
    return this;
  }

  private VelocityForgeBackendConnectionPhase getNewPhase(VelocityServerConnection serverConnection,
                                                       LoginPluginMessagePacket packet) {
    VelocityForgeBackendConnectionPhase phaseToTransitionTo = nextPhase();
    if (phaseToTransitionTo != this) {
      phaseToTransitionTo.onTransitionToNewPhase(serverConnection);
    }
    return phaseToTransitionTo;
  }

  @Override
  public boolean handle(VelocityServerConnection server, ConnectedPlayer player, PluginMessagePacket message) {
    if (message.getChannel().equals("ambassador:commands")) {
      AvailableCommandsPacket packet = new AvailableCommandsPacket();
      packet.decode(message.content(), ProtocolUtils.Direction.CLIENTBOUND,server.getConnection().getProtocolVersion());
      server.getConnection().getActiveSessionHandler().handle(packet);
      return true;
    }
    return false;
  }

  public boolean consideredComplete() {
    return false;
  }



}
