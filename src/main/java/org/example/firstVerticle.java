package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class firstVerticle extends AbstractVerticle
{

    public void start()
    {
        Vertx vertx =Vertx.vertx();
        vertx.createHttpServer().requestHandler(req->req.response().end("Hello World")).listen(8081);
    }

}