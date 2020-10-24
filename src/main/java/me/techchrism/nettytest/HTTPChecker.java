package me.techchrism.nettytest;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.AsciiString;

import java.util.Arrays;
import java.util.List;

@ChannelHandler.Sharable
public class HTTPChecker extends ChannelInboundHandlerAdapter
{
    // List of vanilla minecraft pipeline handlers
    private static final List<String> removeHandlers = Arrays.asList(
            "timeout", "legacy_query", "splitter", "decoder", "prepender", "encoder", "packet_handler");
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        ByteBuf byteBuf = (ByteBuf) msg;
        try
        {
            if(checkIfGet(byteBuf))
            {
                // if it's a GET request, remove the other pipeline elements and add the HTTP ones
                for(String name : ctx.channel().pipeline().names())
                {
                    if(removeHandlers.contains(name))
                    {
                        ctx.channel().pipeline().remove(name);
                    }
                }
                ChannelPipeline p = ctx.channel().pipeline();
                p.addLast(new HttpRequestDecoder());
                p.addLast(new HttpResponseEncoder());
                p.addLast(new HTTPServerHandler());
            }
            else
            {
                // If it's not a GET request, remove this checker so it doesn't check further packets on the same channel
                ctx.channel().pipeline().remove("http_checker");
            }
        }
        catch(Exception e)
        {
            System.out.println("Got error while checking...");
            e.printStackTrace();
        }
        ctx.fireChannelRead(msg);
    }
    
    private boolean checkIfGet(ByteBuf byteBuf)
    {
        byteBuf.markReaderIndex();
        if(byteBuf.readableBytes() < 3)
        {
            return false;
        }
    
        // Read first 3 bytes to check if it's ascii for "GET"
        byte[] firstBytes = new byte[3];
        byteBuf.readBytes(firstBytes);
        AsciiString method = new AsciiString(firstBytes);
        byteBuf.resetReaderIndex();
        return method.contentEquals("GET");
    }
}
