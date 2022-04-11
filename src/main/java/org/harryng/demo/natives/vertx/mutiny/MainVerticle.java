package org.harryng.demo.natives.vertx.mutiny;

import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.Router;

public class MainVerticle extends AbstractVerticle {
    static System.Logger logger = System.getLogger(MainVerticle.class.getCanonicalName());
    @Override
    public void start() throws Exception {
        // Create a Router
        Router router = Router.router(vertx);

        // Mount the handler for all incoming requests at every path and HTTP method
        router.route().handler(context -> {
            // Get the address of the request
            String address = context.request().connection().remoteAddress().toString();
            // Get the query parameter "name"
            var queryParams = context.queryParams();
            String name = queryParams.contains("name") ? queryParams.get("name") : "unknown";
            // Write a json response
            context.json(new JsonObject().put("name", name).put("address", address).put("message", "Hello " + name + " connected from " + address));
        });

        // Create the HTTP server
        vertx.createHttpServer()
                // Handle every request using the router
                .requestHandler(router)
                // Start listening
                .listen(8888)
                // Print the port
                .map(server -> {
                    logger.log(System.Logger.Level.INFO, "HTTP server started on port " + server.actualPort());
                    return server;
                });
    }
}
