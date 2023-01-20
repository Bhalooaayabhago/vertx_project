package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;

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
        log.info("Processing");
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
            r.response().putHeader("Content-type", "application/json").end("Error in Parameters");
            return;
        }
        vu.sendQuery(qPara,r,httpRequest);
        Weather_data responseWdata=new Weather_data();
        httpRequest.send(reply-> {
            if (reply.succeeded())
            {
                r.response().putHeader("Content-Type", "appliaction/json").end(reply.result().body().toString());
                JsonObject response = reply.result().body();
                responseWdata.extractFromJson(response);
                mdl.saveWeatherData(responseWdata);
                if (qPara[3]!=null)
                    mdl.saveCity(qPara[3], responseWdata.getLat(), responseWdata.getLon());
                if (qPara[4]!=null)
                    mdl.saveZip(qPara[4], responseWdata.getLat(), responseWdata.getLon());
            } else {
                r.response().putHeader("Content-Type", "appliaction/json").end("Could not retrieve parameter value incorrect/not connected to internet");
            }
        });
    }
    public void srch(RoutingContext r)
    {
        String qPara[]=new String[5];
        qPara[1]=r.queryParams().get("lat");
        qPara[2]=r.queryParams().get("lon");
        qPara[3]=r.queryParams().get("city");
        qPara[4]=r.queryParams().get("zip");
        int size=r.queryParams().size();
        boolean fail1=(size!=1)&&(size!=2);
        boolean fail2=(size==1)&&(qPara[3]==null&&qPara[4]==null);
        boolean fail3=(size==2)&&(qPara[1]==null||qPara[2]==null);
        if(fail1||fail2||fail3)
            r.response().putHeader("Content-type","application/json").end("Error in Parameters");
       vu.search(qPara,mdl,r);
    }
    public void updt(RoutingContext r)
    {
        String qPara[]=new String[5];
        qPara[1]=r.queryParams().get("lat");
        qPara[2]=r.queryParams().get("lon");
        qPara[3]=r.queryParams().get("city");
        qPara[4]=r.queryParams().get("zip");
        int cnt_null=0;
        for(int i=1;i<=4;i++)
        {
            if(qPara[i]!=null)
                cnt_null++;
        }
        if((cnt_null<2||cnt_null>4)||(qPara[0]==null||qPara[1]==null))
            r.response().putHeader("Content-type","application/json").end("incorrect parameters");
        MultiMap mp=r.queryParams();
        for(String s:mp.names())
        {
            log.info(s+" "+mp.get(s));
        }
        vu.update(qPara,mdl,mp);
    }


}
