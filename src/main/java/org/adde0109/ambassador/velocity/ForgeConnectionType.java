package org.adde0109.ambassador.velocity;

import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.config.PlayerInfoForwarding;
import com.velocitypowered.proxy.connection.ConnectionType;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.backend.BackendConnectionPhase;
import com.velocitypowered.proxy.connection.client.ClientConnectionPhase;
import org.adde0109.ambassador.velocity.ForgeClientConnectionPhase;

public class ForgeConnectionType implements ConnectionType {

  private final MinecraftConnection connection;
  public ForgeConnectionType(MinecraftConnection connection) {
    this.connection = connection;
  }

  @Override
  public ClientConnectionPhase getInitialClientPhase() {
    return new ForgeClientConnectionPhase(connection);
  }

  @Override
  public BackendConnectionPhase getInitialBackendPhase() {
    return null;
  }

  @Override
  public GameProfile addGameProfileTokensIfRequired(GameProfile original, PlayerInfoForwarding forwardingType) {
    return original;
  }
}