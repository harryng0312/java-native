package org.harryng.demo.natives.vertx.mutiny;

import io.vertx.core.DeploymentOptions;
import io.vertx.mutiny.core.Vertx;

public class MainVertxCore {
    static System.Logger logger = System.getLogger(MainVertxCore.class.getCanonicalName());

    public void doFileSystem() {
        var app = new MainVertxFileSystem();
//        app.readBigFile();
//        app.writeFile();
        app.copyFileMulti2();
//        app.copyFile();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> app.getVertx().close()));
//        app.getVertx().closeAndAwait();
//                .subscribe().with(
//                        item -> logger.log(System.Logger.Level.INFO, "vertx finished: " + item),
//                        ex -> logger.log(System.Logger.Level.ERROR, "vertx error: ", ex));
    }

    public void startHttpServerVerticel() {
        var vertx = Vertx.vertx();
        var options = new DeploymentOptions()
                .setWorker(false)
                .setInstances(1);
        logger.log(System.Logger.Level.INFO, "Deplopment starting...");
        vertx.deployVerticleAndAwait(HttpServerVerticle::new, options);
        logger.log(System.Logger.Level.INFO, "Deplopment completed");
//        Runtime.getRuntime().addShutdownHook(new Thread(vertx::close));
    }

    public void startNetServerVerticle() {
        var vertx = Vertx.vertx();
        var options = new DeploymentOptions()
                .setWorker(false)
                .setInstances(1);
        logger.log(System.Logger.Level.INFO, "Deplopment starting...");
        vertx.deployVerticleAndAwait(NetServerVerticle::new, options);
        logger.log(System.Logger.Level.INFO, "Deplopment completed");
    }

    public static void main(String[] args) {
        var main = new MainVertxCore();
        main.startNetServerVerticle();
//        main.doFileSystem();
    }
}
