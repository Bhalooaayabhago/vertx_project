package org.example;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.*;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Controller extends AbstractVerticle
{
    private static final Logger log=  LoggerFactory.getLogger(Register.class);
    private WebClient wb;
    HttpRequest<JsonObject> httpRequest;
    model mdl;
    VU vu;
    public void start(Promise<Void> p)
    {
        mdl=new model(vertx);
        vu=new VU();
        Router r=Router.router(vertx);
        wb=WebClient.create(vertx);
        r.route("/get").handler(this::gt);
        r.route("/search").handler(this::srch);
        r.route("/update").handler(this::updt);
        vertx.createHttpServer().requestHandler(r).listen(8080).onSuccess(ok->{log.info("Success");p.complete();}).onFailure(p::fail);
    }
    public void gt(RoutingContext r)
    {
        log.info("Processing get");
        httpRequest =wb.get(443, "api.openweathermap.org", "/data/2.5/weather") // (2)
                .ssl(true)  // (3)
                .putHeader("Accept", "application/json")  // (4)
                .as(BodyCodec.jsonObject()) // (5)
                .expect(ResponsePredicate.SC_OK);

        String qPara[]=new String[5];
         qPara[1]=r.queryParams().get("lat");
         qPara[2]=r.queryParams().get("lon");
         qPara[3]=r.queryParams().get("city");
         qPara[4]=r.queryParams().get("zip");
         int size=r.queryParams().size();
        boolean fail1=size!=1&&size!=2;
        boolean fail2=size==1&&(qPara[3]==null&&qPara[4]==null);
        boolean fail3=size==2&&(qPara[1]==null||qPara[2]==null);
        if(fail1||fail2||fail3) {
            r.response().putHeader("Content-type", "application/json").end("{\"Error\":\"Error in Parameters\"");
            return;
        }
        vu.sendQuery(qPara,r,httpRequest);
        Weather_data responseWdata=new Weather_data();
        httpRequest.send(reply->
        {
            if (reply.succeeded())
            {
                //r.response().putHeader("Content-Type", "application/json").end(reply.result().body().toString());
                JsonObject response = reply.result().body();
                responseWdata.extractFromJson(response);
                Single<AsyncResult<RowSet<Row>>> res=mdl.saveWeatherData(responseWdata);
                r.response().setChunked(true);
                r.response().putHeader("Content-Type","text/plain");
                res.subscribe(ar->
                {
                    log.info(ar.failed());
                    if(ar.failed()!=true)
                    {
                        r.response().write("Save weather data succeeded\n");
                        log.info("Save weather data succeeded");
                        Field flds[]=Weather_data.class.getDeclaredFields();
                        log.info(flds.length);
                        ArrayList<String> lis=new ArrayList<>();
                        for(Field f:flds)
                        {
                            //log.info(f.getName());
                            lis.add(f.getName());
                        }
                        mdl.update(responseWdata,lis,null);
                        if (qPara[3]!=null) {
                            Single<AsyncResult<RowSet<Row>>> resCity=mdl.saveCity(qPara[3], responseWdata.getLat(), responseWdata.getLon());
                            resCity.subscribe(arr-> {
                                if (arr.failed()) {
                                    r.response().end("Save City Failed\n");
                                    log.info("Save City Failed");
                                    arr.cause().printStackTrace();
                                }
                                else{
                                    r.response().write("Save City Successful\n");
                                    log.info("Save City Successful");Single<AsyncResult<RowSet<Row>>>
                                    resCityUpd=mdl.updateCityCoordinates(qPara[3], responseWdata.getLat(),responseWdata.getLon());
                                    resCityUpd.subscribe(ares->{
                                        if(ares.succeeded()) {
                                            r.response().end("Update city coordinates succeeded\n");
                                            log.info("Update city coordinates succeeded");
                                        }
                                        else {
                                            r.response().end("Update city coordinates failed\n");
                                            log.info("Update city coordinates failed");
                                            ares.cause().printStackTrace();
                                        }
                                    });
                                }
                            });
                        }
                        else if (qPara[4]!=null) {
                            Single<AsyncResult<RowSet<Row>>> resCity=mdl.saveZip(qPara[4], responseWdata.getLat(), responseWdata.getLon());
                            resCity.subscribe(arr-> {
                                if (arr.failed()) {
                                    r.response().end("Save Zip Failed\n");
                                    log.info("Save Zip Failed");
                                    arr.cause().printStackTrace();
                                }
                                else{
                                    r.response().write("Save Zip Successful\n");
                                    log.info("Save Zip Successful");
                                    Single<AsyncResult<RowSet<Row>>> resCityUpd=mdl.updateCityCoordinates(qPara[4], responseWdata.getLat(),responseWdata.getLon());
                                    resCityUpd.subscribe(ares->{
                                        if(ares.succeeded()) {
                                            r.response().end("Update Zip coordinates succeeded\n");
                                            log.info("Update Zip coordinates succeeded");
                                        }
                                        else {
                                            r.response().end("Update Zip coordinates failed\n");
                                            log.info("Update Zip coordinates failed");
                                            ares.cause().printStackTrace();
                                        }
                                    });
                                }
                            });

                        }
                        else
                            r.response().end();
                    }
                    else
                    {
                        r.response().end("Save weather data failed\n");
                        log.info("Save weather data failed");
                        ar.cause().printStackTrace();
                    }
                });
                /*Single<RowSet<Row>> t = client.dbQuery();
                t.map();
                t.flatMap();*/
            }
            else
            {
                r.response().putHeader("Content-Type", "application/json").end("{\"Error\":\"Could not retrieve parameter value incorrect/not connected to internet\"");
            }
        });
    }
    public void srch(RoutingContext r)
    {
        log.info("Processing search");
        String qPara[]=new String[5];
        qPara[1]=r.queryParams().get("lat");
        qPara[2]=r.queryParams().get("lon");
        qPara[3]=r.queryParams().get("city");
        qPara[4]=r.queryParams().get("zip");
        int size=0;
        for(int i=1;i<=4;i++)
        {
            if(qPara[i]!=null)
                size++;
        }
        log.info(size);
        log.info(qPara[3]);
        boolean fail1=(size!=1)&&(size!=2);
        boolean fail2=(size==1)&&(qPara[3]==null&&qPara[4]==null);
        boolean fail3=(size==2)&&(qPara[1]==null||qPara[2]==null);
        if(fail1||fail2||fail3) {
            r.response().putHeader("Content-type", "application/json").end("{\"error\":\"incorrect parameters\"}");
            return;
        }
        log.info(r.queryParams().get("range"));
        if(r.queryParams().get("range")==null)
       vu.search(qPara,mdl,r);
        else
            vu.search(qPara,mdl,r,Double.parseDouble(r.queryParams().get("range")));
    }
    public void updt(RoutingContext r)
    {
        log.info("Processing update");
        String qPara[]=new String[5];
        qPara[1]=r.queryParams().get("lat");
        qPara[2]=r.queryParams().get("lon");
        qPara[3]=r.queryParams().get("city");
        qPara[4]=r.queryParams().get("zip");
        if(qPara[1]==null||qPara[2]==null)
        {
            if(qPara[3]==null&&qPara[4]==null) {
                r.response().putHeader("Content-type", "application/json").end("{\"error\":\"incorrect parameters\"}");
                return;
            }
        }
            MultiMap mp = r.queryParams();
      HashMap<String,String> mpp=new HashMap<>();
        for(String s:mp.names()) {
            //log.info(s + " " + mp.get(s));
            mpp.put(s, mp.get(s));
        }
        /*for(String s:mp.names())
        {
            log.info(s+" "+mp.get(s));
        }*/
            vu.update(qPara, mdl, mpp,r);

    }

}
