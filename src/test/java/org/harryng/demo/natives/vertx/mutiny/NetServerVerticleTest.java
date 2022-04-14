package org.harryng.demo.natives.vertx.mutiny;

import io.vertx.core.net.NetClientOptions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class NetServerVerticleTest extends AbstractVertxTest{

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
                    .map(v -> netSocket);
//        }).subscribe().with(itm -> {});
        }).onFailure().transform(ex -> {
            logger.log(System.Logger.Level.ERROR, "Ex:", ex);
            return ex;
        }).onItemOrFailure().transformToUni((netSocket, ex) -> netSocket.close());//.subscribe().with(itm->{});
//        vertx.executeBlockingAndAwait(run);
        vertx.executeBlockingAndAwait(run);
    }
}