package me.techchrism.nettytest;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class PipelineInjector extends ChannelInboundHandlerAdapter
{
    private final String name;
    private final ChannelHandler handler;
    
    public PipelineInjector(String name, ChannelHandler handler)
    {
        this.name = name;
        this.handler = handler;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        Channel channel = (Channel) msg;
        channel.pipeline().addFirst(name, handler);
        ctx.fireChannelRead(msg);
    }
}
