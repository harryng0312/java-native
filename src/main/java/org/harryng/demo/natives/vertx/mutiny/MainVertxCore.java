package org.harryng.demo.natives.vertx.mutiny;

public class MainVertxCore {
    static System.Logger logger = System.getLogger(MainVertxCore.class.getCanonicalName());

    public static void main(String[] args) {
        var app = new MainVertxFileSystem();
//        app.readBigFile();
//        app.writeFile();
        app.copyFileMulti2();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                app.getVertx().close();
            }
        });
//        app.getVertx().closeAndAwait();
//                .subscribe().with(
//                        item -> logger.log(System.Logger.Level.INFO, "vertx finished: " + item),
//                        ex -> logger.log(System.Logger.Level.ERROR, "vertx error: ", ex));
    }
}
