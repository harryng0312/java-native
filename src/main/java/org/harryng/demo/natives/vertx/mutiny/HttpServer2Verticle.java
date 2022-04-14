package org.harryng.demo.natives.vertx.mutiny;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.AllowForwardHeaders;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.http.HttpServer;
import io.vertx.mutiny.core.http.HttpServerRequest;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.RoutingContext;
import io.vertx.mutiny.ext.web.handler.ErrorHandler;
import io.vertx.mutiny.ext.web.handler.StaticHandler;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class HttpServer2Verticle extends AbstractVerticle {
    static System.Logger logger = System.getLogger(HttpServer2Verticle.class.getCanonicalName());
    private HttpServer server = null;

    private EventBus eventBus = null;

    public void onWsHello(RoutingContext context) {
        String address = context.request().connection().remoteAddress().hostAddress()
                + " " + context.request().connection().remoteAddress().port();
        var pingId = new AtomicLong();
        var queryParams = context.queryParams();
        var id = context.pathParam("id");
        var name = queryParams.contains("name") ? queryParams.get("name") : "unknown";
        context.request().toWebSocket().invoke(serverWebSocket -> serverWebSocket.handler(buffer -> {
            logger.log(System.Logger.Level.INFO, "Handler:"
                    + new String(buffer.getBytes(), StandardCharsets.UTF_8));
            var reqData = new JsonObject(buffer.getDelegate());
            var resData = new JsonObject()
                    .put("id", id)
                    .put("name", name)
                    .put("address", address)
                    .put("message", "Hello " + name + " connected from " + address)
                    .put("requestData", reqData);
            serverWebSocket.write(Buffer.buffer(resData.toString())).flatMap(v -> serverWebSocket.writeTextMessage("Server send data finished"))
                    .subscribe().with(itm -> logger.log(System.Logger.Level.INFO, "send msg finished"));
        }).textMessageHandler(str -> {
            logger.log(System.Logger.Level.INFO, "Text Msg Handler:" + str);
        }).binaryMessageHandler(buffer -> {
            logger.log(System.Logger.Level.INFO, "Binary Msg Handler:"
                    + new String(buffer.getBytes(), StandardCharsets.UTF_8));
        }).frameHandler(webSocketFrame -> {
            logger.log(System.Logger.Level.INFO, "Frame Handler:"
                    + new String(webSocketFrame.binaryData().getBytes()));
        }).drainHandler(() -> {
            // pause if needed
        }).closeHandler(() -> {
            logger.log(System.Logger.Level.INFO, "Client websocket closed!");
        }).endHandler(() -> {
            logger.log(System.Logger.Level.INFO, "WebSocket is end!");
            context.vertx().cancelTimer(pingId.get());
        }).exceptionHandler(ex -> {
            logger.log(System.Logger.Level.ERROR, "", ex);
        })).subscribe().with(
                serverWebSocket -> {
                    logger.log(System.Logger.Level.INFO, "Client fired onConnected!");
                    var preodicId = context.vertx().setPeriodic(5_000, timerId -> {
                        serverWebSocket.writePing(Buffer.buffer("Ping[" + LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "]")).subscribe().with(v ->
                                logger.log(System.Logger.Level.INFO, "Ping client"));
                    });
                    pingId.set(preodicId);
                },
                ex -> logger.log(System.Logger.Level.ERROR, "", ex));
    }

    public void onFailure(RoutingContext context) {
        context.response()
                .setStatusCode(404)
                .end()
                .subscribe().with(itm -> {logger.log(System.Logger.Level.INFO, "OnFailure");},
                        ex -> logger.log(System.Logger.Level.ERROR, "Route err:", ex));
//        .reroute("/static/error.html");
    }
    @Override
    public Uni<Void> asyncStart() {
        super.asyncStart();
        server = vertx.createHttpServer(new HttpServerOptions());
        eventBus = vertx.eventBus();
        final var rootRouter = Router.router(vertx);
        rootRouter.route("/");//.failureHandler(this::onFailure);
        rootRouter.allowForward(AllowForwardHeaders.ALL);
        rootRouter.errorHandler(404, this::onFailure);

        var wsRouter = Router.router(vertx);
        var wsHelloRouter = Router.router(vertx);
        wsHelloRouter.route("/:id")
                .handler(this::onWsHello);
//                .failureHandler(this::onFailure);
        wsRouter.mountSubRouter("/hello", wsHelloRouter);
        rootRouter.mountSubRouter("/ws", wsRouter);

        var staticHandler = StaticHandler.create("static-resources");
        staticHandler.setIndexPage("index.html");
        var staticRouter = Router.router(vertx);
        staticRouter.route("/static/*").handler(staticHandler)
                .failureHandler(this::onFailure);
        rootRouter.mountSubRouter("/", staticRouter);

        return server
                .requestHandler(rootRouter)
                .exceptionHandler(ex -> logger.log(System.Logger.Level.ERROR, "", ex))
                .invalidRequestHandler(HttpServerRequest::bodyAndForget)
                .listen(8080)
                .onItem().invoke(() -> logger.log(System.Logger.Level.INFO, "HTTP server started on port " + server.actualPort()))
                .onFailure().invoke(ex -> logger.log(System.Logger.Level.ERROR, "", ex))
                .replaceWithVoid();
    }

    @Override
    public Uni<Void> asyncStop() {
        super.asyncStop();
        logger.log(System.Logger.Level.INFO, "Vertx is shutting down!");
        return super.asyncStop().flatMap(v -> server.close()).flatMap(v -> vertx.close());
    }
}
