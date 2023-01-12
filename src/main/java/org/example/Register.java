package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;

import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.ext.web.codec.BodyCodec.json;


public class Register extends AbstractVerticle
{
    private static final Logger log=  LoggerFactory.getLogger(Register.class);
    private WebClient wb;
    HttpRequest<JsonObject> tom;
    MySQLConnectOptions connectOptions;
    PoolOptions poolOptions;
    SqlClient client;
    double val[]=new double[2];
    //Random ran=new Random();
   // double temp=9.0;
    public void start(Promise<Void> p)
    {
       //vertx.setPeriodic(1000,this::upd);
        val[0]=-1000;
        val[1]=-1000;
         connectOptions = new MySQLConnectOptions()
                .setPort(3306).setHost("127.0.0.1")
                .setDatabase("weather_data")
                .setUser("root").setPassword(System.getenv("dbpass"));
         log.info(System.getenv("dbpass"));
        poolOptions = new PoolOptions().setMaxSize(10);
        client = MySQLPool.client(vertx,connectOptions, poolOptions);
        /*client.query("select * from Data").execute(ar->{
           if(ar.succeeded())
               log.info("DB works");
           else
               ar.cause().printStackTrace();
        });*/
        Router r=Router.router(vertx);
        wb=WebClient.create(vertx);
        r.get("/get").handler(this::fun);
        r.get("/search*").handler(this::hoho);
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
        AtomicInteger check=new AtomicInteger(flag);
        AtomicReference<String> ci=new AtomicReference<>(s3);
        AtomicReference<String> zi=new AtomicReference<>(s4);
        tom.send(reply->{
            if(reply.succeeded()) {
                r.response().putHeader("Content-Type", "appliaction/json").end(reply.result().body().toString());
                JsonObject job = reply.result().body();
                double latti=0.0,longi=0.0,weather_id=0.0,temp=0.0,feels_like=0.0,temp_min=0.0,temp_max=0.0;
                double pressure = 0,humidity=0,sea_level=0,grnd_level=0,speed =0,deg = 0,gust=0,r1h=0,r3h=0,s1h=0,s3h=0,all=0;
                int sr=0,ss=0;
                String weather_main = null,weather_description = null,weather_icon = null;
                JsonObject coord=job.getJsonObject("coord");
                if(coord!=null) {
                     latti = coord.getDouble("lat");
                     longi = coord.getDouble("lon");
                }

                JsonArray weather=job.getJsonArray("weather");
                if(weather!=null) {
                    JsonObject mw = weather.getJsonObject(0);
                    weather_id = mw.getDouble("id");
                    weather_main = mw.getString("main");
                    weather_description = mw.getString("description");
                    weather_icon = mw.getString("icon");
                }

                String base=job.getString("base");

                JsonObject Main=job.getJsonObject("main");
                if(Main!=null) {
                     temp = Main.getDouble("temp");
                     feels_like = Main.getDouble("feels_like");
                     temp_min = Main.getDouble("temp_min");
                     temp_max = Main.getDouble("temp_max");
                     pressure = Main.getDouble("pressure");
                     humidity = Main.getDouble("humidity");
                     if(Main.getDouble("sea_level")!=null)
                     sea_level = Main.getDouble("sea_level");
                     if(Main.getDouble("grnd_level")!=null)
                     grnd_level = Main.getDouble("grnd_level");
                }

                double visi=job.getDouble("visibility");

                JsonObject wind=job.getJsonObject("wind");
                if(wind!=null) {
                    if(wind.getDouble("speed")!=null)
                     speed = wind.getDouble("speed");
                    if(wind.getDouble("deg")!=null)
                     deg = wind.getDouble("deg");
                    if(wind.getDouble("gust")!=null)
                     gust = wind.getDouble("gust");
                }

                JsonObject rain=job.getJsonObject("rain");
                if(rain!=null) {
                     r1h = rain.getDouble("1h");
                     r3h = rain.getDouble("3h");
                }

                JsonObject snow=job.getJsonObject("snow");
                if(snow!=null) {
                     s1h = snow.getDouble("1h");
                     s3h = snow.getDouble("3h");
                }

                JsonObject clouds=job.getJsonObject("clouds");
                if(clouds!=null) {
                     all = clouds.getDouble("all");
                }

                int date=job.getInteger("dt");

                JsonObject sys=job.getJsonObject("sys");
                if(sys!=null) {
                     sr = sys.getInteger("sunrise");
                     ss = sys.getInteger("sunset");
                }
                int timezone=job.getInteger("timezone");
                client.preparedQuery("INSERT IGNORE INTO " +
                                "Data(lon,lat,weather_id,weather_main,weather_description," +
                                "weather_icon,base,temp,feel_like,temp_min,temp_max," +
                                "pressure,humidity,sea_level,grnd_level,visibility,wind_speed,wind_deg,wind_gust" +
                                ",clouds_all,snow_1h,rain_1h,rain_3h,snow_3h,sunrise,sunset,timezone,dt) VALUES (" +
                                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);").execute(Tuple.of(
                                        longi,latti,weather_id,weather_main,weather_description,weather_icon,base,temp,feels_like,temp_min,temp_max,
                                       pressure,humidity,sea_level,grnd_level,visi,speed,deg,gust,all,s1h,r1h,r3h,s3h,sr,ss,timezone,date),ar->{
                                    if(ar.succeeded())
                                        log.info("yay");
                                    else
                                        ar.cause().printStackTrace();
                                }

                );
                int checker=check.intValue();
                if(checker==1)
                   client.preparedQuery("INSERT IGNORE INTO city(name,lat,lon) VALUES(?,?,?)").execute(Tuple.of(ci,latti,longi));
                if(checker==2)
                    client.preparedQuery("INSERT IGNORE INTO zdata(zip,lat,lon) VALUES(?,?,?)").execute(Tuple.of(zi,latti,longi));
                log.info(""+latti+" "+longi+" "+weather_id+" "+temp+" "+temp_min+" "+feels_like+" "+temp_max+" "+date);
            }
            else {
                r.response().putHeader("Content-Type", "appliaction/json").end("Error");
                reply.cause().printStackTrace();
            }
        });
        //final EventBus eb=vertx.eventBus();
        //JsonObject obj=new JsonObject();
        //obj.put("temprature",temp);?,?,?,?,?,?,?,?,?,?,?,?
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
    void hoho(RoutingContext r) {
        int params = r.queryParams().size();
        log.info(params);
        boolean isit = true;
        if (params != 1 && params != 2)
            isit = false;
        String s1 = r.queryParams().get("lat");
        String s2 = r.queryParams().get("lon");
        if (params == 2 && (s1 == null || s2 == null))
            isit = false;
        if (isit == false) {
            r.response().end("Error 100");
            return;
        }
        String s3 = r.queryParams().get("city");
        String s4 = r.queryParams().get("zip");
        val[0] = (s1 == null) ? -1000 : Double.parseDouble(s1);
        val[1] = (s2 == null) ? -1000 : Double.parseDouble(s2);
        if (s3 != null) {
            client.preparedQuery("SELECT * FROM city where name=?").execute(Tuple.of(s3), ar -> {
                if (ar.failed())
                    ar.cause().printStackTrace();
                else {
                    String ans;
                    RowSet<Row> rx = ar.result();
                    log.info(rx.size());
                    for (Row rr : rx) {
                        val[0] = rr.getDouble("lat");
                        val[1] = rr.getDouble("lon");
                    }
                    log.info(val[0] + " " + val[1]);
                    doq(val[0], val[1], r);
                }
            });
        } else if (s4 != null){
            client.preparedQuery("SELECT * FROM zdata where zip=?").execute(Tuple.of(s4), ar -> {
                if (ar.failed())
                    ar.cause().printStackTrace();
                else {
                    String ans;
                    RowSet<Row> rx = ar.result();
                    log.info(rx.size());
                    for (Row rr : rx) {
                        val[0] = rr.getDouble("lat");
                        val[1] = rr.getDouble("lon");
                    }
                    log.info(val[0] + " " + val[1]);
                    doq(val[0], val[1], r);
                }
            });
        }
        else
            doq(val[0], val[1], r);
    }
    void doq(double lat,double lon,RoutingContext r)
    {
        client.preparedQuery("SELECT * FROM Data where lat=? AND lon=?").execute(Tuple.of(lat,lon),res->{
            if(res.failed())
                res.cause().printStackTrace();
            else
            {
                String ans="";
                RowSet<Row> rows=res.result();
                if(rows.size()==0)
                    ans="NOT FOUND";
                for(Row xx:rows)
                {

                    for(int i=0;i<=27;i++)
                    {
                        String curr=xx.getColumnName(i);
                        if(i>=3&&i<=6) {
                            ans += (curr + " :  " + xx.getString(curr) + "\n");
                            continue;
                        }
                        if(i>=24&&i<=27) {
                            ans += (curr + " :  " + xx.getInteger(curr) + "\n");
                            continue;
                        }
                        ans+=(curr+" :  "+xx.getDouble(curr)+"\n");
                    }
                }
                r.response().putHeader("Content-Type","text/plain").end(ans);
            }
        });
    }

    /*private void upd(long fat)
    {
        temp=temp+ran.nextGaussian()/2.0d;
        System.out.println(temp);
    }*/



}
