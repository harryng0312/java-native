package org.harryng.demo.natives.vertx.mutiny;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.http.HttpServer;
import io.vertx.mutiny.core.http.HttpServerRequest;
import io.vertx.mutiny.core.http.WebSocketFrame;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.RoutingContext;
import io.vertx.mutiny.ext.web.handler.StaticHandler;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class HttpServerVerticle extends AbstractVerticle {
    static System.Logger logger = System.getLogger(HttpServerVerticle.class.getCanonicalName());
    private HttpServer server = null;

    private EventBus eventBus = null;

    public void onGetHello(RoutingContext context) {
        String address = context.request().connection().remoteAddress().hostAddress()
                + " " + context.request().connection().remoteAddress().port();
        var queryParams = context.queryParams();
        String id = context.pathParam("id");
        String name = queryParams.contains("name") ? queryParams.get("name") : "unknown";
        context.jsonAndForget(new JsonObject()
                .put("id", id)
                .put("name", name)
                .put("address", address)
                .put("message", "Hello " + name + " connected from " + address));
    }

    public void onPostHello(RoutingContext context) {
        String address = context.request().connection().remoteAddress().hostAddress()
                + " " + context.request().connection().remoteAddress().port();
        var queryParams = context.queryParams();
        var id = context.pathParam("id");
        var name = queryParams.contains("name") ? queryParams.get("name") : "unknown";
        context.request().body().map(buffer -> {
            var reqData = new JsonObject(buffer.getDelegate());
            var resData = new JsonObject()
                    .put("id", id)
                    .put("name", name)
                    .put("address", address)
                    .put("message", "Hello " + name + " connected from " + address)
                    .put("requestData", reqData);
            return resData;
        }).flatMap(context::json
        ).subscribe().with(itm -> {
        });
    }

    public void onWsHello(RoutingContext context) {
        String address = context.request().connection().remoteAddress().hostAddress()
                + " " + context.request().connection().remoteAddress().port();
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
        }).exceptionHandler(ex -> {
            logger.log(System.Logger.Level.ERROR, "", ex);
        })).subscribe().with(
                itm -> logger.log(System.Logger.Level.INFO, "Client fired onConnected!"),
                ex -> logger.log(System.Logger.Level.ERROR, "", ex));
    }

    public void onFailure(RoutingContext context) {
        context.response().setStatusCode(context.statusCode()).end()
                .subscribe().with(itm -> {
                }, ex -> logger.log(System.Logger.Level.ERROR, "", ex));
    }

    @Override
    public Uni<Void> asyncStart() {
        super.asyncStart();
        server = vertx.createHttpServer(new HttpServerOptions());
        eventBus = vertx.eventBus();
        final var rootRouter = Router.router(vertx);
        rootRouter.route("/").failureHandler(this::onFailure);

        var getHelloRouter = Router.router(vertx);
        getHelloRouter.get("/:id")
                .produces("application/json")
                .consumes("application/json")
                .handler(this::onGetHello)
                .failureHandler(this::onFailure);

        var postHelloRouter = Router.router(vertx);
        postHelloRouter.post("/:id")
                .produces("application/json")
                .consumes("application/json")
                .handler(this::onPostHello)
                .failureHandler(this::onFailure);

        var wsRouter = Router.router(vertx);
        var wsHelloRouter = Router.router(vertx);
        wsHelloRouter.route("/:id")
                .handler(this::onWsHello)
                .failureHandler(this::onFailure);
        wsRouter.mountSubRouter("/hello", wsHelloRouter);

        rootRouter.mountSubRouter("/hello", getHelloRouter);
        rootRouter.mountSubRouter("/hello", postHelloRouter);
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
