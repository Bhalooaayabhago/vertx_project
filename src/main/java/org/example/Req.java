package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import static io.vertx.ext.web.client.predicate.ResponsePredicate.JSON;


public class Req extends AbstractVerticle
{
    private double lat,lon;
    Req(double lat,double lon)
    {
        super();
        this.lat=lat;
        this.lon=lon;
    }
    Req()
    {
        super();
        this.lat=0;
        this.lon=0;
    }

    private WebClient r;
    JsonObject job;
    HttpRequest<JsonObject> tom;
     public void start(Promise<Void> p)
     {
         r=WebClient.create(vertx);
         vertx.eventBus().consumer("coordinates",this::msg);
         tom=r.get(443, "api.openweathermap.org", "/data/2.5/weather") // (2)
                 .ssl(true)  // (3)
                 .putHeader("Accept", "application/json")  // (4)
                 .as(BodyCodec.jsonObject()) // (5)
                 .expect(ResponsePredicate.SC_OK);
         vertx.setTimer(10000,this::lo);
         p.complete();
     }
    private <T> void msg(Message<T> tMessage)
    {
        JsonObject message = (JsonObject) tMessage.body();
        System.out.println("Message Received " + message);
       String s1=message.getString("lat");
       String s2=message.getString("lon");
       lat=(s1.equals(""))?0:(Double.parseDouble(s1));
       lon=(s2.equals(""))?0:(Double.parseDouble(s2));
        tom.addQueryParam("lat",lat+"");
        tom.addQueryParam("lon",lon+"");
        tom.addQueryParam("appid","");
        //vertx.executeBlocking(this::lo);

        tMessage.reply(message);
    }
    void lo(long ll)
    {
        tom.send(ok->
        {
            if(ok.succeeded()) {
                System.out.println("Sucess");
                //tMessage.reply(ok.result());
            }
            else
                ok.cause().printStackTrace();
        });
        //p.complete();
    }


}
