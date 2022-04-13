package org.harryng.demo.natives.vertx.mutiny;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.OpenOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.file.AsyncFile;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FileSystemVertx {
    static System.Logger logger = System.getLogger(FileSystemVertx.class.getCanonicalName());
    Vertx vertx = null;

    private void initVertx() {
        var vertxOpts = new VertxOptions();
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
        var srcPath = "files/BadgeChainDemo.mp4";
//        var srcPath = "files/test2.txt";
        int buffSize = 1024 * 1024;
        getVertx().fileSystem().open(srcPath, new OpenOptions().setRead(true))
                .map(asyncFile -> {
                    logger.log(System.Logger.Level.INFO, "file read");
                    return asyncFile.setReadBufferSize(buffSize)
                            .handler(buffer -> {
                                logger.log(System.Logger.Level.INFO, "Buffsize: " + buffer.length());
                            })
//                            .endHandler(() -> logger.log(System.Logger.Level.INFO, "end file!"))
                            .endHandler(() -> {
                                logger.log(System.Logger.Level.INFO, "end file!");
                                asyncFile.closeAndForget();
                            });
//                    return asyncFile;
                })
//                .flatMap(asyncFile -> {
//                    logger.log(System.Logger.Level.INFO, "file closed");
//                    return asyncFile.close();
//                })
                .onFailure().transform(err -> {
                    logger.log(System.Logger.Level.ERROR, "", err);
                    return err;
                })
                .subscribe().with(
                        item -> {
                        },//logger.log(System.Logger.Level.INFO, "subscribe: " + item),
                        ex -> logger.log(System.Logger.Level.ERROR, "error: ", ex)
                );
//                .await().indefinitely();
    }

    public void writeFile() {
        final var path = "files/test2.txt";
        final var data = "this is string data big";
        getVertx().fileSystem()
                .exists(path)
                .flatMap(existed -> {
                    if (!existed) {
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

    public void copyFileMulti() {
        var srcPath = "files/BadgeChainDemo.mp4";
        var destPath = "files/BadgeChainDemo_1.mp4";
        int buffSize = 256 * 1024;
        getVertx().fileSystem()
                .exists(srcPath)
                .flatMap(Unchecked.function(srcPathExisting -> {
                    if (srcPathExisting) {
                        return getVertx().fileSystem()
                                .open(srcPath, new OpenOptions()
                                        .setCreate(false)
                                        .setRead(true));
                    } else {
                        throw new FileNotFoundException(srcPath);
                    }
                }))
                .flatMap(Unchecked.function(srcAsyncFile -> getVertx().fileSystem().exists(destPath)))
                .flatMap(Unchecked.function(destPathExisting -> !destPathExisting ?
                        getVertx().fileSystem().createFile(destPath)
                        : getVertx().fileSystem().delete(destPath)))
                .flatMap(Unchecked.function(v -> getVertx().fileSystem().open(srcPath, new OpenOptions().setRead(true))))
                .onItem().transformToMulti(srcFile -> Multi.createFrom().<Buffer>emitter(multiEmitter ->
                        srcFile.setReadBufferSize(buffSize)
                                .handler(buffer -> {
                                    logger.log(System.Logger.Level.INFO, "read buffer size: " + buffer.length());
                                    multiEmitter.emit(buffer);
                                })
                                .endHandler(() -> {
                                    logger.log(System.Logger.Level.INFO, "Read file finished!");
                                    srcFile.closeAndForget();
                                })))
                .onItem().transformToMultiAndConcatenate(
                        buffer -> getVertx().fileSystem().open(destPath, new OpenOptions().setAppend(true))
                                .toMulti()
                                .flatMap(destFile -> {
                                    logger.log(System.Logger.Level.INFO, "write buffer size: " + buffer.length());
                                    return destFile.write(buffer)
//                                                .map(v -> destFile.flushAndForget())
                                            .toMulti()
                                            .map(v -> destFile.flushAndForget())
                                            .map(v -> destFile);
                                }))
//                .merge()
                .toUni()
//                .flatMap(itm -> itm)
                .map(destFile -> {
                    logger.log(System.Logger.Level.INFO, "Dest file size: " + destFile.sizeBlocking());
                    destFile.closeAndForget();
                    return destFile;
                })
                .onFailure().invoke(ex -> logger.log(System.Logger.Level.ERROR, "Exception: ", ex))
                .subscribe().with(
                        item -> logger.log(System.Logger.Level.INFO, "read size: " + item.sizeBlocking()),
                        failure -> System.out.println("Failed with " + failure)//,
//                        () -> System.out.println("Completed")
                );
    }

    public void copyFileMulti2() {
//        var srcPath = "/mnt/working/downloads/film/Rat Disaster 2021 ViE 1080p WEB-DL DD2.0 H.264 (Thuyet Minh - Sub Viet).mkv";
        var srcPath = "/mnt/working/downloads/film/Stay.Alive.2006.KSTE.avi";
        var destPath = "files/testbig.avi";
        int buffSize = 1024 * 1024;

        var destIndex = new AtomicInteger();
        getVertx().fileSystem()
                .exists(srcPath)
                .flatMap(Unchecked.function(srcPathExisting -> {
                    if (srcPathExisting) {
                        return getVertx().fileSystem()
                                .open(srcPath, new OpenOptions()
                                        .setCreate(false)
                                        .setRead(true));
                    } else {
                        throw new FileNotFoundException(srcPath);
                    }
                }))
                .flatMap(Unchecked.function(srcAsyncFile -> getVertx().fileSystem().exists(destPath)))
                .flatMap(Unchecked.function(destPathExisting -> !destPathExisting ?
                        getVertx().fileSystem().createFile(destPath)
                        : getVertx().fileSystem().delete(destPath)))
                .flatMap(Unchecked.function(v -> getVertx().fileSystem().open(srcPath, new OpenOptions().setRead(true))))
                .onItem().transformToMulti(srcFile -> Multi.createFrom().<Buffer>emitter(multiEmitter -> {
                    var srcIndex = new AtomicInteger();
                    srcFile.setReadBufferSize(buffSize).handler(buffer -> {
//                                logger.log(System.Logger.Level.INFO, "read:" + new String(buffer.getBytes()) + "|");
                                srcIndex.getAndIncrement();
                                multiEmitter.emit(buffer);
                            })
                            .endHandler(() -> {
                                logger.log(System.Logger.Level.INFO, "Read file finished! " + srcIndex.intValue());
                                srcFile.closeAndForget();
                            });
                }, buffSize * 16))
                .concatMap(buffer -> getVertx().fileSystem().open(destPath, new OpenOptions().setAppend(true))
                        .toMulti()
                        .flatMap(destFile -> {
                            destIndex.getAndIncrement();
//                            logger.log(System.Logger.Level.INFO, "write:" + new String(buffer.getBytes()) + "|");
                            return destFile.write(buffer)
                                    .map(v -> destFile.flushAndForget()).toMulti();
//                                            .map(v -> destFile).toMulti();
//                                            .toMulti().map(v -> destFile);
                        }))
                .onItem().transformToUniAndConcatenate(destFile -> {
//                    logger.log(System.Logger.Level.INFO, "Write count: " + destIndex.intValue());
                    return Uni.createFrom().item(destFile);
                })
                .map(asyncFile -> {
                    logger.log(System.Logger.Level.INFO, "Dest file is closing!");
                    asyncFile.closeAndForget();
                    return asyncFile;
                })
                .onFailure().invoke(ex -> logger.log(System.Logger.Level.ERROR, "Exception: ", ex))
                .subscribe().with(
                        item -> {
                        },//logger.log(System.Logger.Level.INFO, "Read size: " + item.sizeBlocking()),
                        failure -> logger.log(System.Logger.Level.ERROR, "Failed with " + failure, failure)//,
//                        () -> System.out.println("Completed")
                );
    }

    public void copyFile() {
        var srcPath = "/mnt/working/downloads/film/Stay.Alive.2006.KSTE.avi";
        var destPath = "files/testbig.avi";
        int buffSize = 1024 * 1024;

        var destIndex = new AtomicInteger();
        getVertx().fileSystem()
                .exists(srcPath)
                .flatMap(Unchecked.function(srcPathExisting -> {
                    if (srcPathExisting) {
                        return getVertx().fileSystem()
                                .open(srcPath, new OpenOptions()
                                        .setCreate(false)
                                        .setRead(true));
                    } else {
                        throw new FileNotFoundException(srcPath);
                    }
                }))
                .flatMap(Unchecked.function(srcAsyncFile -> getVertx().fileSystem().exists(destPath)))
                .flatMap(Unchecked.function(destPathExisting -> !destPathExisting ?
                        getVertx().fileSystem().createFile(destPath)
                        : getVertx().fileSystem().delete(destPath)))
                .flatMap(Unchecked.function(v -> getVertx().fileSystem().open(srcPath, new OpenOptions().setRead(true))))
                .map(srcFile -> srcFile.setReadBufferSize(buffSize)
                        .handler(buffer -> {
                            logger.log(System.Logger.Level.INFO, "File read: " + buffer.length());
                            getVertx().fileSystem().open(destPath, new OpenOptions().setAppend(true))
                                    .flatMap(asyncFile -> asyncFile.write(buffer))
//                                    .map(v -> srcFile.flushAndForget())
                                    .onFailure().retry().indefinitely()
                                    .await().indefinitely();
                        })
                        .endHandler(() -> {
                            logger.log(System.Logger.Level.INFO, "Read file finished!");
                            srcFile.closeAndForget();
                        })
                ).map(asyncFile -> {
                    logger.log(System.Logger.Level.INFO, "Dest file is closing!");
                    return asyncFile;
                })
                .onFailure().invoke(ex -> logger.log(System.Logger.Level.ERROR, "Exception: ", ex))
                .subscribe().with(item -> {
                        },
                        failure -> logger.log(System.Logger.Level.ERROR, "Failed with " + failure, failure));
    }
}
