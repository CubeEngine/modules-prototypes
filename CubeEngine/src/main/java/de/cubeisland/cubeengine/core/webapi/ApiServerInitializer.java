package de.cubeisland.cubeengine.core.webapi;

import de.cubeisland.cubeengine.core.Core;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 *
 * @author Phillip Schichtel
 */
public class ApiServerInitializer extends ChannelInitializer<SocketChannel>
{
    private final Core core;
    private final ApiServer server;

    ApiServerInitializer(Core core, ApiServer server)
    {
        this.core = core;
        this.server = server;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception
    {
        ch.pipeline()
            .addLast("decoder", new HttpRequestDecoder())
            .addLast("aggregator", new HttpObjectAggregator(this.server.getMaxContentLength()))
            .addLast("encoder", new HttpResponseEncoder())
            .addLast("handler", new ApiRequestHandler(this.server, this.core.getJsonObjectMapper()));

        if (this.server.isCompressionEnabled())
        {
            ch.pipeline().addLast("deflater", new HttpContentCompressor(this.server.getCompressionLevel(), this.server.getCompressionWindowBits(), this.server.getCompressionMemoryLevel()));
        }
    }
}