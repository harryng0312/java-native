package org.harryng.demo.natives.vertx.mutiny;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.AllowForwardHeaders;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.http.HttpServer;
import io.vertx.mutiny.core.http.HttpServerRequest;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.RoutingContext;
import io.vertx.mutiny.ext.web.handler.StaticHandler;
import io.vertx.mutiny.sqlclient.Tuple;
import org.harryng.demo.natives.ResourcesUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpServer2Verticle extends AbstractVerticle {
    static Logger logger = LoggerFactory.getLogger(HttpServer2Verticle.class);
    private HttpServer server = null;

    private EventBus eventBus = null;

    public void onWsSql(RoutingContext context) {
        var dbConnector = DbConnector.createDbConnector(vertx, "localhost", 5432,
                "test_db", "test_db", "test_db", 3);
        var sqlClient = dbConnector.getSqlClient();
        context.request().toWebSocket().invoke(
                serverWebSocket -> serverWebSocket.handler(buffer -> {
                    logger.info("Client call:");
                    var obj = new JsonObject(new String(buffer.getBytes(), StandardCharsets.UTF_8));
                    var params = obj.getJsonArray("params");
                    if (params == null) {
                        params = new JsonArray();
                    }
                    sqlClient.preparedQuery(obj.getString("sql"))
                            .mapping(row -> new JsonObject()
                                    .put("id", row.getLong("id_"))
                                    .put("createdDate", ResourcesUtil.getDateTimeFormatter()
                                            .format(row.getLocalDateTime("created_date")))
                                    .put("modifiedDate", ResourcesUtil.getDateTimeFormatter()
                                            .format(row.getLocalDateTime("modified_date")))
                                    .put("status", row.getString("status"))
                                    .put("screenname", row.getString("screenname"))
                                    .put("username", row.getString("username"))
                                    .put("password", row.getString("password_"))
                                    .put("dob", ResourcesUtil.getDateFormatter()
                                            .format(row.getLocalDate("dob")))
                                    .put("passwdEncryptedMethod", row.getString("passwd_encrypted_method"))
                            ).execute(Tuple.tuple(params.stream().toList()))
                            .subscribe().with(rowset -> {
                                var resultJson = new JsonObject();
                                var recordsJson = new JsonArray();
                                rowset.forEach(recordsJson::add);
                                resultJson.put("total", rowset.size())
                                        .put("results", recordsJson);
                                serverWebSocket.writeTextMessage(resultJson.toString())
                                        .subscribe().with(v -> logger.info("Send result to client!"));
                            });
                }).drainHandler(() -> {
                }).closeHandler(dbConnector::releaseSqlClient).endHandler(() -> {
                }).exceptionHandler(ex -> logger.error("", ex))
        ).subscribe().with(serverWebSocket -> logger.info("Client connected!"));
    }

    public void onFailure(RoutingContext context) {
        context.response()
                .setStatusCode(404)
                .end()
                .subscribe().with(itm -> {
                            logger.info("OnFailure");
                        },
                        ex -> logger.error("Route err:", ex));
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
        wsHelloRouter.route("/")
                .handler(this::onWsSql);
//                .failureHandler(this::onFailure);
        wsRouter.mountSubRouter("/sql", wsHelloRouter);
        rootRouter.mountSubRouter("/ws", wsRouter);

        var staticHandler = StaticHandler.create("static-resources");
        staticHandler.setIndexPage("index.html");
        var staticRouter = Router.router(vertx);
        staticRouter.route("/static/*").handler(staticHandler)
                .failureHandler(this::onFailure);
        rootRouter.mountSubRouter("/", staticRouter);

        return server
                .requestHandler(rootRouter)
                .exceptionHandler(ex -> logger.error("", ex))
                .invalidRequestHandler(HttpServerRequest::bodyAndForget)
                .listen(8080)
                .onItem().invoke(() -> logger.info("HTTP server started on port " + server.actualPort()))
                .onFailure().invoke(ex -> logger.error("", ex))
                .replaceWithVoid();
    }

    @Override
    public Uni<Void> asyncStop() {
        super.asyncStop();
        logger.info("Vertx is shutting down!");
        return super.asyncStop().flatMap(v -> server.close()).flatMap(v -> vertx.close());
    }
}
