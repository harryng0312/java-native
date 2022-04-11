package org.harryng.demo.natives.vertx.mutiny;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.OpenOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.file.AsyncFile;
import io.vertx.mutiny.core.streams.Pump;

import java.nio.charset.StandardCharsets;

public class MainVertxCore {
    static System.Logger logger = System.getLogger(MainVertxCore.class.getCanonicalName());
    Vertx vertx = null;

    private void initVertx() {
        var vertxOpts = new VertxOptions().setWorkerPoolSize(40);
        vertx = Vertx.vertx(vertxOpts);
    }

    public Vertx getVertx() {
        if (vertx == null) {
            initVertx();
        }
        return vertx;
    }

    public void readFile() {
        getVertx().fileSystem()
                .readFile("./files/test.txt")
                .map(buffer -> {
                    logger.log(System.Logger.Level.INFO, "file content:"
                            + new String(buffer.getBytes(), StandardCharsets.UTF_8));
                    return buffer;
                })
                .onFailure().transform(err -> {
                    logger.log(System.Logger.Level.INFO, "", err);
                    return err;
                });
    }

    public void readBigFile() {
        getVertx().fileSystem().open("./files/test.txt", new OpenOptions()
                        .setRead(true).setWrite(false).setAppend(false).setCreate(false))
                .map(asyncFile -> {
                    asyncFile.setReadBufferSize(256 * 1024)
                            .handler(buffer -> {
                                logger.log(System.Logger.Level.INFO, new String(buffer.getBytes(), StandardCharsets.UTF_8));
                            })
                            .endHandler(() -> logger.log(System.Logger.Level.INFO, "end file!"));
                    return asyncFile;
                })
                .onFailure().transform(err -> {
                    logger.log(System.Logger.Level.ERROR, "", err);
                    return err;
                });
    }

    public void writeFile() {
        final var path = "files/test2.txt";
        final var data = "this is string data big";
        getVertx().fileSystem()
                .exists(path)
                .flatMap(existed -> {
                    if(!existed) {
                        getVertx().fileSystem().createFile(path);
                    }
                    return Uni.createFrom().voidItem();
                })
                .flatMap(v ->
                    getVertx().fileSystem()
                        .open(path, new OpenOptions()
                            .setWrite(true))
                )
                .flatMap(asyncFile -> {
                    var buffer = Buffer.buffer(data, "utf-8");
                    return asyncFile.write(buffer).map(v -> asyncFile);
//                    var pump = Pump.pump(, asyncFile);
//                    return asyncFile;
                })
//                .flatMap(asyncFile -> asyncFile.flush().map(v -> asyncFile))
//                .flatMap(AsyncFile::close)
                .map(AsyncFile::flushAndForget)
                .map(asyncFile -> {
                    asyncFile.closeAndForget();
                    return asyncFile;
                })
                .onFailure().transform(Unchecked.function(err -> err))
                .await().indefinitely();

    }

    public static void main(String[] args) {
        var app = new MainVertxCore();
        app.writeFile();

        app.getVertx().close().await().indefinitely();
    }
}
