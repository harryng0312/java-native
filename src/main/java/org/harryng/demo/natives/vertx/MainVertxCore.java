package org.harryng.demo.natives.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.OpenOptions;

import java.nio.charset.StandardCharsets;

public class MainVertxCore {
    static System.Logger logger = System.getLogger(MainVertxCore.class.getCanonicalName());
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
        var srcPath = "files/BadgeChainDemo.mp4";
        var destPath = "files/BadgeChainDemo_1.mp4";
        int buffSize = 1024 * 1024;
//        getVertx().fileSystem().open(srcPath, new OpenOptions().setRead(true), res -> {
//            if(res.succeeded()){
//                res.result().setReadBufferSize(buffSize)
//                        .handler(buffer -> {
//                            logger.log(System.Logger.Level.INFO, "read buffer size: " + buffer.getBytes().length);
//                        })
//                        .endHandler(v -> {
//                            res.result().close().result();
//                        });
//            }
//        });
        getVertx().fileSystem().open(srcPath, new OpenOptions()
                .setRead(true).setWrite(false).setAppend(false).setCreate(false))
                .onSuccess(asyncFile -> {
                    asyncFile.setReadBufferSize(buffSize)
                        .handler(buffer -> {
//                            logger.log(System.Logger.Level.INFO, new String(buffer.getBytes(), StandardCharsets.UTF_8));
                            logger.log(System.Logger.Level.INFO, "read buffer size: " + buffer.getBytes().length);
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
