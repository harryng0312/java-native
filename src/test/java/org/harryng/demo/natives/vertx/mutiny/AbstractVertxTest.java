package org.harryng.demo.natives.vertx.mutiny;

import io.vertx.mutiny.core.Vertx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class AbstractVertxTest {
    static System.Logger logger = System.getLogger(AbstractVertxTest.class.getCanonicalName());
    protected Vertx vertx = null;

    @BeforeEach
    public void init() {
        vertx = Vertx.vertx();
    }

    @AfterEach
    public void destroy() {
        Runtime.getRuntime().addShutdownHook(new Thread(vertx::close));
    }
}
