package org.adde0109.ambassador;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.network.ConnectionManager;
import com.velocitypowered.proxy.protocol.packet.brigadier.ArgumentIdentifier;
import com.velocitypowered.proxy.protocol.packet.brigadier.ArgumentPropertyRegistry;
import com.velocitypowered.proxy.protocol.packet.brigadier.ArgumentPropertySerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInitializer;
import org.adde0109.ambassador.velocity.VelocityBackendChannelInitializer;
import org.adde0109.ambassador.velocity.VelocityServerChannelInitializer;
import org.adde0109.ambassador.velocity.VelocityEventHandler;
import org.adde0109.ambassador.velocity.commands.EnumArgumentProperty;
import org.adde0109.ambassador.velocity.commands.EnumArgumentPropertySerializer;
import org.adde0109.ambassador.velocity.commands.ModIdArgumentProperty;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "ambassador", name = "Ambassador", version = "1.1.7-alpha", authors = {"adde0109"})
public class Ambassador {

  public ProxyServer server;
  public final Logger logger;
  private final Metrics.Factory metricsFactory;
  private final Path dataDirectory;

  private static Ambassador instance;
  public static Ambassador getInstance() {
    return instance;
  }


  @Inject
  public Ambassador(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
    this.server = server;
    this.logger = logger;
    this.dataDirectory = dataDirectory;
    this.metricsFactory = metricsFactory;
    Ambassador.instance = this;
  }

  @Subscribe(order = PostOrder.LAST)
  public void onProxyInitialization(ProxyInitializeEvent event) throws ReflectiveOperationException {
    initMetrics();

    server.getEventManager().register(this, new VelocityEventHandler(this));

    inject();
  }

  private void inject() throws ReflectiveOperationException {
    Field cmField = VelocityServer.class.getDeclaredField("cm");
    cmField.setAccessible(true);

    ChannelInitializer<?> original = ((ConnectionManager) cmField.get(server)).serverChannelInitializer.get();
    ((ConnectionManager) cmField.get(server)).serverChannelInitializer.set(new VelocityServerChannelInitializer(original,(VelocityServer) server));

    ChannelInitializer<?> originalBackend = ((ConnectionManager) cmField.get(server)).backendChannelInitializer.get();
    ((ConnectionManager) cmField.get(server)).backendChannelInitializer.set(new VelocityBackendChannelInitializer(originalBackend,(VelocityServer) server));

    Method argumentRegistry = ArgumentPropertyRegistry.class.getDeclaredMethod("register", ArgumentIdentifier.class, Class.class, ArgumentPropertySerializer.class);
    argumentRegistry.setAccessible(true);
    argumentRegistry.invoke(null,ArgumentIdentifier.id("forge:enum"), EnumArgumentProperty.class, EnumArgumentPropertySerializer.ENUM);
    argumentRegistry.invoke(null,ArgumentIdentifier.id("forge:modid"), ModIdArgumentProperty.class,
            new ArgumentPropertySerializer<>() {
              @Override
              public ModIdArgumentProperty deserialize(ByteBuf buf, ProtocolVersion protocolVersion) {
                return new ModIdArgumentProperty();
              }

              @Override
              public void serialize(Object object, ByteBuf buf, ProtocolVersion protocolVersion) {

              }
            });

  }

  private void initMetrics() {
    Metrics metrics = metricsFactory.make(this, 15655);
  }
}
