package org.harryng.demo.natives.vertx.mutiny;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.mutiny.core.Vertx;

public class MainVertxCore {
    static Logger logger = LoggerFactory.getLogger(MainVertxCore.class);

    public void doFileSystem() {
        var app = new FileSystemVertx();
//        app.readBigFile();
//        app.writeFile();
//        app.copyFileMulti2();
        app.copyFile();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> app.getVertx().close()));
//        app.getVertx().closeAndAwait();
//                .subscribe().with(
//                        item -> logger.info("vertx finished: " + item),
//                        ex -> logger.log(System.Logger.Level.ERROR, "vertx error: ", ex));
    }

    public void startHttpServerVerticel() {
        var vertx = Vertx.vertx();
        var options = new DeploymentOptions()
                .setWorker(false)
                .setInstances(1);
        logger.info("Deplopment starting...");
        vertx.deployVerticleAndAwait(HttpServerVerticle::new, options);
        logger.info("Deplopment completed");
//        Runtime.getRuntime().addShutdownHook(new Thread(vertx::close));
    }

    public void startHttpServer2Verticel() {
        var vertx = Vertx.vertx();
        var options = new DeploymentOptions()
                .setWorker(false)
                .setInstances(1);
        logger.info("Deplopment starting...");
        vertx.deployVerticleAndAwait(HttpServer2Verticle::new, options);
        logger.info("Deplopment completed");
//        Runtime.getRuntime().addShutdownHook(new Thread(vertx::close));
    }

    public void startNetServerVerticle() {
        var vertx = Vertx.vertx();
        var options = new DeploymentOptions()
                .setWorker(false)
                .setInstances(1);
        logger.info("Deplopment starting...");
        vertx.deployVerticleAndAwait(NetServerVerticle::new, options);
        logger.info("Deplopment completed");
    }

    public static void main(String[] args) {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory");
        var main = new MainVertxCore();
        main.startHttpServer2Verticel();
//        main.startNetServerVerticle();
//        main.doFileSystem();
    }
}
