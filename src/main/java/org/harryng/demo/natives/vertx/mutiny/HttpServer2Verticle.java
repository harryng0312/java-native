package org.harryng.demo.natives.vertx.mutiny;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
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

public class HttpServer2Verticle extends AbstractVerticle {
    static Logger logger = LoggerFactory.getLogger(HttpServer2Verticle.class);
    private HttpServer server = null;

    private EventBus eventBus = null;

    final String host = "localhost";
    final int port = 5432;
    final String db = "test_db";
    final String user = "test_db";
    final String passwd = "test_db";
    final int poolsize = 5;

    public void onWsQueryUser(RoutingContext context) {
        var dbConnector = DbConnector.createDbConnector(vertx, host, port, db, user, passwd, poolsize);
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
                                resultJson.put("total", rowset.rowCount())
                                        .put("results", recordsJson);
                                serverWebSocket.writeTextMessage(resultJson.toString())
                                        .subscribe().with(v -> logger.info("Send result to client!"));
                            }, ex -> serverWebSocket.writeTextMessage(ex.getMessage()).subscribe().with(
                                    v -> logger.info("Send error to client!"), ex1 -> logger.error("", ex1)));
                }).drainHandler(() -> {
                }).closeHandler(dbConnector::closeSqlClient).endHandler(() -> {
                }).exceptionHandler(ex -> serverWebSocket.writeTextMessage(ex.getMessage()).subscribe().with(v -> {
                }, ex1 -> {
                }))
        ).subscribe().with(serverWebSocket -> logger.info("Client connected!"), ex -> logger.error("", ex));
    }

    public void onWsUpdateUser(RoutingContext context) {
        var dbConnector = DbConnector.createDbConnector(vertx, host, port, db, user, passwd, poolsize);
        var sqlConn = dbConnector.getSqlConnection();
        context.request().toWebSocket().map(serverWebSocket ->
                serverWebSocket.handler(buffer -> {
                    logger.info("Client call:");
                    var obj = new JsonObject(new String(buffer.getBytes(), StandardCharsets.UTF_8));
                    var params = obj.getJsonArray("params", new JsonArray());
                    // start trans scope
                    sqlConn.flatMap(sqlConnection -> sqlConnection.begin().flatMap(transaction ->
                                    sqlConnection.preparedQuery(obj.getString("sql"))
                                            .execute(Tuple.from(params.stream().toList()))
                                            .invoke(rows -> {
                                                logger.info("trans is commited!");
                                                transaction.commitAndForget();
                                            })
                                            .onFailure().invoke(ex -> {
                                                logger.info("trans is rolled back!");
                                                transaction.rollbackAndForget();
                                            })
                                            .onItemOrFailure().invoke((rows, ex) -> {
                                                logger.info("trans is completed!");
                                                transaction.completionAndForget();
                                            })
                            ).onItemOrFailure().invoke((rows, ex) -> {
                                logger.info("Sql Connection is closing!");
                                dbConnector.releaseSqlConnection(sqlConnection);
                            }).map(rows -> {
                                logger.info(rows.rowCount() + " row(s) effected");
                                var result = new JsonObject().put("total", rows.rowCount());
                                serverWebSocket.writeTextMessage(result.toString())
                                        .subscribe().with(v -> logger.info("Send result to client!"),
                                                ex -> logger.error("Send ex to client!", ex));
                                return rows;
                            })).onFailure().invoke(ex -> serverWebSocket.writeTextMessage(ex.getMessage())
                                    .subscribe().with(v -> logger.info("Send ex to client!"),
                                            ex1 -> logger.error("Send ex to client!", ex1)))
                            .subscribe().with(rows -> logger.info("Trans is complete!"),
                                    ex -> logger.error("Trans ex:", ex));
                    // end trans scope
                }).drainHandler(() -> {
                }).closeHandler(() -> {
                }).endHandler(() -> {
                    logger.info("Client disconnected!");
                }).exceptionHandler(ex -> serverWebSocket.writeTextMessage(ex.getMessage())
                        .subscribe().with(v -> {
                        }, ex1 -> {
                        })
                )
        ).subscribe().with(serverWebSocket -> logger.info("Client connected!"), ex -> logger.error("", ex));
    }

    public void onWsUpdate2User(RoutingContext context) {
        var dbConnector = DbConnector.createDbConnector(vertx, host, port, db, user, passwd, poolsize);
        var sqlConn = dbConnector.getSqlConnection();
        context.request().toWebSocket().map(serverWebSocket ->
                        serverWebSocket.handler(buffer -> {
                            logger.info("Client call:");
                            var obj = new JsonObject(new String(buffer.getBytes(), StandardCharsets.UTF_8));
                            var params = obj.getJsonArray("params", new JsonArray());
                            // start trans scope
                            dbConnector.withTransaction(sqlConnection -> {
                                                var result = sqlConnection.preparedQuery(obj.getString("sql"))
                                                        .execute(Tuple.from(params.stream().toList()));
                                                return result.invoke(rows -> {
                                                    logger.info("Sql Connection is closing!");
//                                                    dbConnector.releaseSqlConnection(sqlConnection);
                                                });
                                            }
                                    )
                                    .map(rows -> {
                                        logger.info("trans is commited!");
                                        logger.info(rows.rowCount() + " row(s) effected");
                                        var result = new JsonObject().put("total", rows.rowCount());
                                        serverWebSocket.writeTextMessage(result.toString())
                                                .subscribe().with(v -> logger.info("Send result to client!"),
                                                        ex -> logger.error("Send ex to client!", ex));
                                        return rows;
                                    })
                                    .onFailure().invoke(throwable -> {
                                        logger.info("trans is rolled back!");
                                        logger.error("", throwable);
                                    })
                                    .onItemOrFailure().invoke((rows, ex) -> {
//                                logger.info("trans is completed!");
//                                logger.info("Sql Connection is closing!");
//                                dbConnector.releaseSqlConnection(sqlConnection);
                                    }).onFailure().invoke(ex -> serverWebSocket.writeTextMessage(ex.getMessage())
                                            .subscribe().with(v -> logger.info("Send ex to client!"),
                                                    ex1 -> logger.error("Send ex to client!", ex1)))
                                    .subscribe().with(rows -> logger.info("Trans is complete!"),
                                            ex -> logger.error("Trans ex:", ex));

                            // end trans scope
                        }).drainHandler(() -> {
                        }).closeHandler(() -> {
                        }).endHandler(() -> {
                            logger.info("Client disconnected!");
                        }).exceptionHandler(ex -> serverWebSocket.writeTextMessage(ex.getMessage())
                                .subscribe().with(v -> {
                                }, ex1 -> {
                                })
                        )
        ).subscribe().with(serverWebSocket -> logger.info("Client connected!"), ex -> logger.error("", ex));
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
        var wsQueryUser = Router.router(vertx);
        wsQueryUser.route("/").handler(this::onWsQueryUser);
        wsRouter.mountSubRouter("/query-user", wsQueryUser);
        var wsUpdateUser = Router.router(vertx);
        wsUpdateUser.route("/").handler(this::onWsUpdateUser);
        var wsUpdate2User = Router.router(vertx);
        wsUpdate2User.route("/").handler(this::onWsUpdate2User);

        wsRouter.mountSubRouter("/update-user", wsUpdateUser);
        wsRouter.mountSubRouter("/update2-user", wsUpdate2User);
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
