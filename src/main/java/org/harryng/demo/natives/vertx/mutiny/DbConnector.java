package org.harryng.demo.natives.vertx.mutiny;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.SqlClient;
import io.vertx.mutiny.sqlclient.SqlConnection;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;

import java.util.function.Function;

public class DbConnector {
    private Vertx vertx = null;
    private PgConnectOptions connectOptions = null;
    private PoolOptions poolOptions = null;
    private SqlClient sqlClient = null;

    private DbConnector(Vertx vertx, String host, int port, String database, String user, String password, int poolsize) {
        this.vertx = vertx;
        init(host, port, database, user, password, poolsize);
    }

    public static DbConnector createDbConnector(Vertx vertx, String host, int port, String database, String user,
                                                String password, int poolsize) {
        return new DbConnector(vertx, host, port, database, user, password, poolsize);
    }

    private void init(String host, int port, String database, String user, String password, int poolsize) {
        connectOptions = new PgConnectOptions()
                .setPort(port)
                .setHost(host)
                .setDatabase(database)
                .setUser(user)
                .setPassword(password);
        poolOptions = new PoolOptions()
                .setMaxSize(poolsize);
    }

    public SqlClient getSqlClient() {
        sqlClient = PgPool.client(vertx, connectOptions, poolOptions);
        return sqlClient;
    }

    public Uni<Void> closeSqlClient() {
        return sqlClient.close();
    }

    public <T> Uni<T> withTransaction(Function<SqlConnection, Uni<T>> function){
        return PgPool.pool(vertx, connectOptions, poolOptions).withTransaction(function);
    }

    public Uni<SqlConnection> getSqlConnection() {
        return PgPool.pool(vertx, connectOptions, poolOptions).getConnection();
    }

    public void releaseSqlConnection(SqlConnection sqlConnection) {
        sqlConnection.closeAndForget();
    }

}
