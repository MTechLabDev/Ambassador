package org.adde0109.ambassador.velocity;

import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.network.BackendChannelInitializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.adde0109.ambassador.forge.ForgeConstants;
import org.adde0109.ambassador.velocity.backend.VelocityForgeBackendHandshakeHandler;


import java.lang.reflect.Method;

public class VelocityBackendChannelInitializer extends BackendChannelInitializer {

  private static final Method INIT_CHANNEL;

  private final ChannelInitializer<?> delegate;
  private final VelocityServer server;

  static {
    try {
      INIT_CHANNEL = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
      INIT_CHANNEL.setAccessible(true);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  public VelocityBackendChannelInitializer(ChannelInitializer<?> delegate, VelocityServer server) {
    super(server);
    this.delegate = delegate;
    this.server = server;
  }

  @Override
  protected void initChannel(Channel ch) {
    try {
      INIT_CHANNEL.invoke(delegate,ch);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
    ch.pipeline().addLast(ForgeConstants.HANDLER, new VelocityForgeBackendHandshakeHandler(server));
  }
}
