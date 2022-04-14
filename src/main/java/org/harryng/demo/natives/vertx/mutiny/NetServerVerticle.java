package org.harryng.demo.natives.vertx.mutiny;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.net.NetServerOptions;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.net.NetServer;
import io.vertx.mutiny.core.net.NetSocket;

import java.nio.charset.StandardCharsets;

public class NetServerVerticle extends AbstractVerticle {
    static Logger logger = LoggerFactory.getLogger(NetServerVerticle.class);
    protected NetServer server = null;

    protected Uni<Void> init() {
        var options = new NetServerOptions().setReuseAddress(true)
                .setPort(4321).setHost("0.0.0.0");
        server = vertx.createNetServer(options);
        return server.connectHandler(this::handleNetSocket)
                .listen()
                .invoke(v -> {
                    logger.info("Server is listening at port " + server.actualPort() + " ...");
                }).replaceWithVoid();
    }

    private void handleNetSocket(NetSocket netSocket) {
        netSocket.handler(this::onMessage).closeHandler(netSocket::closeAndForget);
    }

    protected void onMessage(Buffer buffer) {
        logger.info("Server receive:" + new String(buffer.getBytes(), StandardCharsets.UTF_8));
    }

    @Override
    public Uni<Void> asyncStart() {
        super.asyncStart();
        return Uni.createFrom().voidItem().flatMap(v -> this.init());
    }

    @Override
    public Uni<Void> asyncStop() {
        return super.asyncStop().flatMap(v -> server.close()).flatMap(v -> vertx.close());
    }
}
