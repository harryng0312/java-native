package org.harryng.demo.natives.vertx.mutiny;

import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.mutiny.core.http.HttpClientRequest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class HttpServerVerticleTest extends AbstractVertxTest {

    @Test
    public void testSendHttpServerFromClient() {
        var options = new HttpClientOptions();
        var client = vertx.createHttpClient(options);
        var reqOptions = new RequestOptions()
                .setHost("localhost").setPort(8080)
                .addHeader("content-type", "application/json");
        var req = client
                .request(reqOptions.setURI("/hello/1?name=test01").setMethod(HttpMethod.GET))
                .map(httpClientRequest -> {
                    logger.log(System.Logger.Level.INFO, "Connecting...");
                    return httpClientRequest;
                }).flatMap(HttpClientRequest::connect)
                .map(httpClientResponse -> httpClientResponse.bodyHandler(buffer -> {
                    logger.log(System.Logger.Level.INFO, "Receive:" + new String(buffer.getBytes(), StandardCharsets.UTF_8));
                }))
                .onFailure().invoke(ex -> logger.log(System.Logger.Level.ERROR, "Ex:", ex))
                .onItemOrFailure().invoke((itm, ex) -> client.closeAndForget());
        vertx.executeBlockingAndAwait(req);
    }
}
