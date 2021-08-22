package dev.simplix.protocolize.velocity;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.network.ConnectionManager;
import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.providers.PacketListenerProvider;
import dev.simplix.protocolize.api.providers.ProtocolRegistrationProvider;
import dev.simplix.protocolize.velocity.commands.ProtocolizeCommand;
import dev.simplix.protocolize.velocity.netty.ProtocolizeBackendChannelInitializer;
import dev.simplix.protocolize.velocity.netty.ProtocolizeServerChannelInitializer;
import dev.simplix.protocolize.velocity.providers.VelocityPacketListenerProvider;
import dev.simplix.protocolize.velocity.providers.VelocityProtocolRegistrationProvider;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/**
 * Date: 22.08.2021
 *
 * @author Exceptionflug
 */
@Plugin(name = "Protocolize", authors = "Exceptionflug", version = "v2", id = "protocolize")
public class ProtocolizePlugin {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private String version;
    private boolean supported;

    @Inject
    public ProtocolizePlugin(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        version = readVersion();
        initProviders();
    }

    private void initProviders() {
        Protocolize.registerService(PacketListenerProvider.class, new VelocityPacketListenerProvider());
        Protocolize.registerService(ProtocolRegistrationProvider.class, new VelocityProtocolRegistrationProvider());
    }

    private String readVersion() {
        try (InputStream inputStream = ProtocolizePlugin.class.getResourceAsStream("/version.txt")) {
            return new String(ByteStreams.toByteArray(inputStream), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.warn("Unable to read version", e);
        }
        return "2.?.?:unknown";
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) throws ReflectiveOperationException {
        logger.info("======= PROTOCOLIZE =======");
        logger.info("Version " + version + " by " + description().getAuthors().toString().replace("[", "").replace("]", ""));
        if (version.endsWith(":unknown")) {
            logger.warn("WARNING: YOU ARE RUNNING AN UNOFFICIAL BUILD OF PROTOCOLIZE. DON'T REPORT ANY BUGS REGARDING THIS VERSION.");
        }
        logger.info("Swap channel initializers (ignore the following two warnings)...");
        swapChannelInitializers();
        logger.info("Swapped channel initializers");

        proxyServer.getCommandManager().register("protocolize", new ProtocolizeCommand(this));
    }

    private void swapChannelInitializers() throws ReflectiveOperationException {
        Field cm = VelocityServer.class.getDeclaredField("cm");
        cm.setAccessible(true);
        ConnectionManager connectionManager = (ConnectionManager) cm.get(proxyServer);
        connectionManager.getBackendChannelInitializer().set(new ProtocolizeBackendChannelInitializer((VelocityServer) proxyServer));
        connectionManager.getServerChannelInitializer().set(new ProtocolizeServerChannelInitializer((VelocityServer) proxyServer));
    }

    public String version() {
        return version;
    }

    public PluginDescription description() {
        return proxyServer.getPluginManager().getPlugin("protocolize").get().getDescription();
    }

}
