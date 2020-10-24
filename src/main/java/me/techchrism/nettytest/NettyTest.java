package me.techchrism.nettytest;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.server.v1_16_R2.MinecraftServer;
import net.minecraft.server.v1_16_R2.ServerConnection;

public class NettyTest extends JavaPlugin
{
    private Channel channel;
    
    @Override
    public void onEnable()
    {
        try
        {
            ServerConnection serverConnection = MinecraftServer.getServer().getServerConnection();
            
            Field listeningChannelsField = ServerConnection.class.getDeclaredField("listeningChannels");
            listeningChannelsField.setAccessible(true);
            List<ChannelFuture> listeningChannels = (List<ChannelFuture>) listeningChannelsField.get(serverConnection);

            // In testing, there has only been 1 listing channel
            channel = listeningChannels.get(0).channel();
            channel.pipeline().addFirst("pipeline_injector", new PipelineInjector("http_checker", new HTTPChecker()));
        }
        catch(NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDisable()
    {
        channel.pipeline().remove("pipeline_injector");
    }
}
