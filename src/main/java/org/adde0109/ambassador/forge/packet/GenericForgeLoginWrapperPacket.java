package org.adde0109.ambassador.forge.packet;

import com.velocitypowered.proxy.protocol.packet.LoginPluginResponsePacket;
import com.velocitypowered.proxy.protocol.util.DeferredByteBufHolder;
import io.netty.buffer.ByteBuf;

public class GenericForgeLoginWrapperPacket extends DeferredByteBufHolder implements IForgeLoginWrapperPacket {
  private final int id;
  private final boolean success;

  public GenericForgeLoginWrapperPacket(ByteBuf input, int id, boolean success) {
    super(input);
    this.id = id;
    this.success = success;
  }

  @Override
  public LoginPluginResponsePacket encode() {
    return new LoginPluginResponsePacket(id, true, content());
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public boolean getSuccess() {
    return success;
  }
}
