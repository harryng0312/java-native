package org.harryng.demo.natives.vertx.mutiny;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.http.HttpServer;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.RoutingContext;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

public class HttpServerVerticle extends AbstractVerticle {
    static System.Logger logger = System.getLogger(HttpServerVerticle.class.getCanonicalName());
    private HttpServer server = null;

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

    public void onFailure(RoutingContext context) {
        context.response().setStatusCode(404).end()
                .subscribe().with(ex -> logger.log(System.Logger.Level.ERROR, "", ex));
    }

    @Override
    public Uni<Void> asyncStart() {
        super.asyncStart();
        server = vertx.createHttpServer(new HttpServerOptions());
        var rootRouter = Router.router(vertx);
        rootRouter.route("/");

        var getHelloRouter = Router.router(vertx);
        var postHelloRouter = Router.router(vertx);
        getHelloRouter.get("/:id")
                .produces("application/json")
                .consumes("application/json")
                .handler(this::onGetHello)
                .failureHandler(this::onFailure);
        postHelloRouter.post("/:id")
                .produces("application/json")
                .consumes("application/json")
                .handler(this::onPostHello)
                .failureHandler(this::onFailure);

        rootRouter.mountSubRouter("/hello", getHelloRouter);
        rootRouter.mountSubRouter("/hello", postHelloRouter);
        return server
                .requestHandler(rootRouter)
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
