/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.core.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.logger.CubeLogger;
import de.cubeisland.cubeengine.core.webapi.exception.ApiRequestException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.logger.LogLevel.*;
import static de.cubeisland.cubeengine.core.webapi.RequestError.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;

/**
 * This class handles all requests
 *
 * @author Phillip Schichtel
 */
public class ApiRequestHandler extends ChannelInboundMessageHandlerAdapter<Object>
{
    private final Charset UTF8 = Charset.forName("UTF-8");
    private final String WEBSOCKET_ROUTE = "websocket";
    private final Logger logger;
    private final ApiServer server;
    private WebSocketServerHandshaker handshaker = null;
    private ObjectMapper objectMapper;

    ApiRequestHandler(ApiServer server, ObjectMapper mapper)
    {
        this.server = server;
        this.objectMapper = mapper;
        this.logger = new CubeLogger("webapi");
        try
        {
            this.logger.addHandler(new FileHandler("webapi.log"));
        }
        catch (IOException e)
        {
            CubeEngine.getLog().log(ERROR, "Failed to initialize the file handler for the web api log!", e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable t)
    {
        this.error(context, UNKNOWN_ERROR);
        this.logger.log(ERROR, "An error occurred while processing an API request: " + t.getMessage(), t);
    }

    @Override
    public void messageReceived(ChannelHandlerContext context, Object message) throws Exception
    {
        this.logger.log(INFO, "{0} connected...", ((InetSocketAddress)context.channel().remoteAddress()).getAddress().getHostAddress());
        if (!this.server.isAddressAccepted((InetSocketAddress)context.channel().remoteAddress()))
        {
            this.logger.log(INFO, "Access denied!");
            context.channel().close();
        }

        if (message instanceof FullHttpRequest)
        {
            this.logger.log(INFO, "this is a HTTP request...");
            this.handleHttpRequest(context, (FullHttpRequest)message);
        }
        else if (message instanceof WebSocketFrame)
        {
            this.logger.log(INFO, "oh a websocket frame!");
            this.handleWebSocketFrame(context, (WebSocketFrame)message);
        }
        else
        {
            this.logger.log(INFO, "dafuq!?");
            context.close();
        }
    }

    private void handleHttpRequest(ChannelHandlerContext context, FullHttpRequest request)
    {
        if (request.getDecoderResult().isFailure())
        {
            this.error(context, UNKNOWN_ERROR);
            this.logger.log(INFO, "the decoder failed on this request...", request.getDecoderResult().cause());
            return;
        }

        QueryStringDecoder qsDecoder = new QueryStringDecoder(request.getUri(), this.UTF8, true, 100);

        String path = qsDecoder.path().trim();

        if (path.length() == 0 || "/".equals(path))
        {
            this.error(context, ROUTE_NOT_FOUND);
            return;
        }

        if (path.charAt(0) == '/')
        {
            path = path.substring(1);
        }
        if (path.charAt(path.length() - 1) == '/')
        {
            path = path.substring(0, path.length() - 1);
        }

        // is this request intended to initialize a websockets connection?
        if ("websocket".equals(path))
        {
            this.logger.log(INFO, "received a websocket request...");
            WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory("ws://" + request.headers().get(HOST) + "/" + this.WEBSOCKET_ROUTE, null, false);
            this.handshaker = handshakerFactory.newHandshaker(request);
            if (this.handshaker == null)
            {
                this.logger.log(INFO, "client is incompatible!");
                handshakerFactory.sendUnsupportedWebSocketVersionResponse(context.channel());
            }
            else
            {
                this.logger.log(INFO, "handshaking now...");
                this.handshaker.handshake(context.channel(), request).addListener(new ChannelFutureListener()
                {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception
                    {
                        if (future.isSuccess())
                        {
                            logger.log(INFO, "Success!");
                        }
                        else
                        {
                            logger.log(INFO, "Failed!");
                        }
                    }
                });
            }
            return;
        }

        ApiHandler handler = this.server.getApiHandler(path);
        if (handler == null)
        {
            this.error(context, ROUTE_NOT_FOUND);
            return;
        }

        JsonNode data = null;
        ByteBuf requestContent = request.data();
        if (requestContent != Unpooled.EMPTY_BUFFER)
        {
            try
            {
                data = this.objectMapper.readTree(requestContent.array());
            }
            catch (Exception e)
            {
                this.logger.log(DEBUG, "Failed to parse the request body!", e);
                this.error(context, MALFORMED_DATA);
                return;
            }
        }
        final RequestMethod method = RequestMethod.getByName(request.getMethod().name());
        final Parameters params = new Parameters(qsDecoder.parameters());

        ApiRequest apiRequest = new ApiRequest((InetSocketAddress)context.channel().remoteAddress(), method, params, request.headers(), data);
        ApiResponse apiResponse = new ApiResponse();

        try
        {
            handler.execute(apiRequest, apiResponse);
            this.success(context, apiResponse);
        }
        catch (ApiRequestException e)
        {
            this.error(context, REQUEST_EXCEPTION, e);
        }
        catch (Throwable t)
        {
            this.error(context, UNKNOWN_ERROR);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext context, WebSocketFrame frame)
    {
        if (frame instanceof CloseWebSocketFrame)
        {
            this.logger.log(INFO, "recevied close frame");
            this.server.unsubscribe(this);
            this.handshaker.close(context.channel(), (CloseWebSocketFrame)frame);
        }
        else if (frame instanceof PingWebSocketFrame)
        {
            this.logger.log(INFO, "recevied ping frame");
            context.write(new PongWebSocketFrame(frame.data()));
        }
        else if (frame instanceof TextWebSocketFrame)
        {
            this.logger.log(INFO, "recevied text frame");
            this.handleTextWebSocketFrame(context, (TextWebSocketFrame)frame);
        }
        else
        {
            this.logger.log(INFO, "recevied unknown incompatible frame");
            context.close();
        }
    }

    private void handleTextWebSocketFrame(ChannelHandlerContext context, TextWebSocketFrame frame)
    {
        String content = frame.text();

        int newLinePos = content.indexOf('\n');
        if (newLinePos == -1)
        {
            this.logger.log(INFO, "the frame data didn't contain a newline !");
            // TODO error response
            return;
        }
        String command = content.substring(0, newLinePos).trim();
        content = content.substring(newLinePos).trim();

        if ("request".equals(command))
        {
            String route;
            newLinePos = content.indexOf('\n');
            RequestMethod method = null;
            if (newLinePos == -1)
            {
                route = content;
            }
            else
            {
                route = normalizeRoute(content.substring(0, newLinePos));
                content = content.substring(newLinePos).trim();

                final int spacePos = route.indexOf(' ');
                if (spacePos != -1)
                {
                    method = RequestMethod.getByName(route.substring(0, spacePos));
                    route = route.substring(spacePos + 1);
                }
            }

            if (method == null)
            {
                method = RequestMethod.GET;
            }

            JsonNode data = null;
            if (!content.isEmpty())
            {
                try
                {
                    data = this.objectMapper.readTree(content);
                }
                catch (Exception e)
                {
                    // TODO ERROR
                }
            }

            ApiHandler handler = this.server.getApiHandler(route);
            Parameters params = null;
            ApiRequest request = new ApiRequest((InetSocketAddress)context.channel().remoteAddress(), method, params, HttpHeaders.EMPTY_HEADERS, data);
            ApiResponse response = new ApiResponse();
            try
            {
                handler.execute(request, response);
            }
            catch (ApiRequestException e)
            {}
            catch (Throwable t)
            {}
        }
        else if ("subscribe".equals(command))
        {
            this.server.subscribe(content.trim(), this);
        }
        else if ("unsubscribe".equals(command))
        {
            this.server.unsubscribe(content.trim(), this);
        }

        context.write(new TextWebSocketFrame(command + " -- " + content));
    }

    private void success(ChannelHandlerContext context, ApiResponse apiResponse)
    {
        context.write(apiResponse.getContent()).addListener(ChannelFutureListener.CLOSE);
    }

    private void error(ChannelHandlerContext context, RequestError error)
    {
        this.error(context, error, null);
    }

    private void error(ChannelHandlerContext context, RequestError error, ApiRequestException e)
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("id", error.getCode());
        data.put("desc", error.getDescription());

        if (e != null)
        {
            Map<String, Object> reason = new HashMap<String, Object>();
            reason.put("id", e.getCode());
            reason.put("desc", e.getMessage());
            data.put("reason", reason);
        }

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, error.getRepsonseStatus(), Unpooled.copiedBuffer(this.serialize(data), this.UTF8));
        response.headers().set(CONTENT_TYPE, MimeType.JSON.toString());

        context.write(response).addListener(ChannelFutureListener.CLOSE).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    public static String normalizeRoute(String route)
    {
        route = route.trim().replace('\\', '/');
        if (route.charAt(0) == '/')
        {
            route = route.substring(1);
        }
        if (route.charAt(route.length() - 1) == '/')
        {
            route = route.substring(0, route.length() - 1);
        }
        return route;
    }

    public String serialize(Object object)
    {
        if (object == null)
        {
            return "null";
        }
        if (object instanceof Map)
        {
            try
            {
                return this.objectMapper.writer().writeValueAsString(object);
            }
            catch (JsonProcessingException e)
            {
                this.logger.log(ERROR, "Failed to generate the JSON code for a response!", e);
                return "null";
            }
        }
        else
        {
            return String.valueOf(object);
        }
    }

    public void handleEvent(String event, Map<String, Object> data)
    {}
}