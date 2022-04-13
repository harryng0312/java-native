package org.harryng.demo.natives.vertx.mutiny;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.http.HttpServer;
import io.vertx.mutiny.ext.web.Router;

public class MainVerticle extends AbstractVerticle {
    static System.Logger logger = System.getLogger(MainVerticle.class.getCanonicalName());
    private long counter = 0L;
    private HttpServer server = null;

    @Override
    public Uni<Void> asyncStart() {
        super.asyncStart();
        // create a server
        server = vertx.createHttpServer(new HttpServerOptions());
        // Create a Router
        Router router = Router.router(vertx);
        router.get("/hello")
                .produces("application/json")
                .consumes("application/json")
                .handler(context -> {
                    // Get the address of the request
                    String address = context.request().connection().remoteAddress().toString();
                    // Get the query parameter "name"
                    var queryParams = context.queryParams();
                    String name = queryParams.contains("name") ? queryParams.get("name") : "unknown";
                    // Write a json response
//                    context.json(new JsonObject().put("name", name)
//                                    .put("address", address)
//                                    .put("message", "Hello " + name + " connected from " + address))
//                            .subscribe().with(v -> {
//                            });
                    context.jsonAndForget(new JsonObject().put("name", name)
                            .put("address", address)
                            .put("message", "Hello " + name + " connected from " + address));
//                    context.response()
////                            .putHeader("content-type", "application/json")
//                            .end(new JsonObject()
//                                    .put("name", name)
//                                    .put("address", address)
//                                    .put("message", "Hello " + name + " connected from " + address)
//                                    .toString())
//                            .subscribe().with(v -> {
//                            });
                })
                .failureHandler(context -> {
                    context.response().setStatusCode(404).end()
                            .subscribe().with(ex -> logger.log(System.Logger.Level.ERROR, "", ex));
                });

        // Create the HTTP server
        return server
                // Handle every request using the router
                .requestHandler(router)
                // Start listening
                .listen(8080)
                // Print the port
                .onItem().invoke(() -> {
                    logger.log(System.Logger.Level.INFO, "HTTP server started on port " + server.actualPort());
//                    return server;
                })
                .onFailure().invoke(ex -> logger.log(System.Logger.Level.ERROR, "", ex))
                .replaceWithVoid();
    }

    @Override
    public Uni<Void> asyncStop() {
//        super.asyncStop()
        logger.log(System.Logger.Level.INFO, "Vertx is shutting down!");
//        vertx.close();
        return super.asyncStop().flatMap(v -> server.close()).flatMap(v -> vertx.close());
    }
}
