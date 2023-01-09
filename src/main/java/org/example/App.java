package org.example;

/**
 * Hello world!
 *
 */
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.io.IOException;

public class App extends AbstractVerticle
{
    public static void main(String[] args)throws IOException
    {
        //firstVerticle ff=new firstVerticle();
        //ff.start();
        Vertx vertx =Vertx.vertx();
        vertx.deployVerticle(new Register());
        //vertx.deployVerticle(new Req());
    }

}
