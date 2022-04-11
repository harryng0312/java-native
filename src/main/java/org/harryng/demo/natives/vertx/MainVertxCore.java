package org.harryng.demo.natives.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.OpenOptions;
import org.harryng.demo.natives.Application;

import java.nio.charset.StandardCharsets;

public class MainVertxCore {
    static System.Logger logger = System.getLogger(Application.class.getCanonicalName());
    Vertx vertx = null;

    private void initVertx(){
        var vertxOpts = new VertxOptions().setWorkerPoolSize(40);
        vertx = Vertx.vertx(vertxOpts);
    }

    public Vertx getVertx(){
        if(vertx == null) {
            initVertx();
        }
        return vertx;
    }

    public void readFile(){
        getVertx().fileSystem()
                .readFile("./files/test.txt")
                .onSuccess(buffer -> {
                    logger.log(System.Logger.Level.INFO, "file content:"
                            + new String(buffer.getBytes(), StandardCharsets.UTF_8));
                })
                .onFailure(err -> {
                    logger.log(System.Logger.Level.INFO, "", err);
                });
    }

    public void readBigFile(){
        getVertx().fileSystem().open("./files/test.txt", new OpenOptions()
                .setRead(true).setWrite(false).setAppend(false).setCreate(false))
                .onSuccess(asyncFile -> {
                    asyncFile.setReadBufferSize(256 * 1024)
                        .handler(buffer -> {
                            logger.log(System.Logger.Level.INFO, new String(buffer.getBytes(), StandardCharsets.UTF_8));
                        })
                        .endHandler((v) -> {logger.log(System.Logger.Level.INFO, "end file!");});
                })
                .onFailure(err -> logger.log(System.Logger.Level.ERROR, "", err));
    }

    public static void main(String[] args){
        var app = new MainVertxCore();
        app.readBigFile();

        app.getVertx().close().result();
    }
}
