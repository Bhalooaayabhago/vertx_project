package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static io.vertx.ext.web.codec.BodyCodec.json;


public class Register extends AbstractVerticle
{
    private static final Logger log=  LoggerFactory.getLogger(Register.class);
    private WebClient wb;
    HttpRequest<JsonObject> tom;
    //Random ran=new Random();
   // double temp=9.0;
    public void start(Promise<Void> p)
    {
       //vertx.setPeriodic(1000,this::upd);
        Router r=Router.router(vertx);
        wb=WebClient.create(vertx);
        r.get("/get*").handler(this::fun);
        vertx.createHttpServer().requestHandler(r).listen(8080).onSuccess(ok->{log.info("Success");p.complete();}).onFailure(p::fail);
    }
    void fun(RoutingContext r)
    {
        log.info("Processing");
        String s1=r.queryParams().get("lat");
        String s2=r.queryParams().get("lon");
        String s3=r.queryParams().get("city");
        String s4=r.queryParams().get("zip");
        tom=wb.get(443, "api.openweathermap.org", "/data/2.5/weather") // (2)
                .ssl(true)  // (3)
                .putHeader("Accept", "application/json")  // (4)
                .as(BodyCodec.jsonObject()) // (5)
                .expect(ResponsePredicate.SC_OK);
        int flag=0;
        if(s3==null!=true)
            flag=1;
        else if(s4==null!=true)
            flag=2;
        double lat=-1000,lon=-1000;
        log.info(flag+"");
        if(s1!=null)
            lat=(s1.equals(""))?-1000:Double.parseDouble(s1);
        if(s2!=null)
            lon=(s2.equals(""))?-1000:Double.parseDouble(s2);
        if(flag==0) {
            tom.addQueryParam("lat", lat + "");
            tom.addQueryParam("lon", lon + "");
        }
        else if(flag==1)
            tom.addQueryParam("q", s3 + "");
        else
            tom.addQueryParam("zip", s4 + "");
        log.info(System.getenv().containsKey("api_key"));
        String mainkey=System.getenv("api_key");
        if(mainkey==null)
            mainkey="1";
        tom.addQueryParam("appid",mainkey);
        tom.send(reply->{
            if(reply.succeeded())
                r.response().putHeader("Content-Type", "appliaction/json").end(reply.result().body().toString());
            else {
                r.response().putHeader("Content-Type", "appliaction/json").end("Error");
                reply.cause().printStackTrace();
            }
        });
        //final EventBus eb=vertx.eventBus();
        //JsonObject obj=new JsonObject();
        //obj.put("temprature",temp);
        //obj.put("lat",lat);
        //obj.put("lon",lon);
        /*System.out.println(lat+" "+lon);
        vertx.deployVerticle(new Req());
        eb.request("coordinates",obj,reply->
        {
            if (reply.failed()) {
               reply.cause().printStackTrace();
            } else {
                r.response().putHeader("Content-Type", "appliaction/json").end(reply.result().body().toString());
            }
        }   );*/
    }

    /*private void upd(long fat)
    {
        temp=temp+ran.nextGaussian()/2.0d;
        System.out.println(temp);
    }*/



}
