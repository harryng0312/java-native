package org.harryng.demo.natives.vertx.mutiny;

import io.vertx.core.net.NetClientOptions;
import io.vertx.mutiny.core.Vertx;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;

public class NetServerVerticleTest {
    static System.Logger logger = System.getLogger(NetServerVerticleTest.class.getCanonicalName());
    protected Vertx vertx = null;

    @BeforeEach
    public void init() {
        vertx = Vertx.vertx();
    }

    @AfterEach
    public void destroy() {
        Runtime.getRuntime().addShutdownHook(new Thread(vertx::close));
    }

    @Test
    public void testSendNetServerFromClient() {
        logger.log(System.Logger.Level.INFO, "=====");
        var options = new NetClientOptions();
        var netClient = vertx.createNetClient(options);
        var run = netClient.connect(4321, "localhost").map(netSocket -> netSocket.handler(buffer -> {
            logger.log(System.Logger.Level.INFO, "Client receive:" + new String(buffer.getBytes(), StandardCharsets.UTF_8));
        })).flatMap(netSocket -> {
            logger.log(System.Logger.Level.INFO, "Client sent!");
            return netSocket.write("From client: hello server")
                    .flatMap(v -> netSocket.end())
                    .flatMap(v -> netSocket.close());
//        }).subscribe().with(itm -> {});
        }).onFailure().transform(ex -> {
            logger.log(System.Logger.Level.ERROR, "Ex:", ex);
            return ex;
        });
        vertx.executeBlockingAndAwait(run);
    }
}