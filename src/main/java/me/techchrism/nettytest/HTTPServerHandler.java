package me.techchrism.nettytest;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HTTPServerHandler extends SimpleChannelInboundHandler<Object>
{
    private HttpRequest request;
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
    {
        ctx.flush();
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg)
    {
        if(msg instanceof HttpRequest)
        {
            HttpRequest request = this.request = (HttpRequest) msg;
            if(HttpUtil.is100ContinueExpected(request))
            {
                writeContinueResponse(ctx);
            }
        }
        
        if(msg instanceof HttpContent)
        {
            if(msg instanceof LastHttpContent)
            {
                LastHttpContent trailer = (LastHttpContent) msg;
                writeContinueResponse(ctx, trailer, "Hello World!");
            }
        }
    }
    
    private void writeContinueResponse(ChannelHandlerContext ctx)
    {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE, Unpooled.EMPTY_BUFFER);
        ctx.write(response);
    }
    
    private void writeContinueResponse(ChannelHandlerContext ctx, LastHttpContent trailer, String responseData)
    {
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, trailer.decoderResult()
                .isSuccess() ? OK : BAD_REQUEST, Unpooled.copiedBuffer(responseData, CharsetUtil.UTF_8));
        
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        
        if(keepAlive)
        {
            httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        
        ctx.write(httpResponse);
        if(!keepAlive)
        {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        cause.printStackTrace();
        ctx.close();
    }
}
