package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.MultiMap;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;

import java.util.ArrayList;
import java.util.HashMap;

public class VU
{
    private static final Logger log=  LoggerFactory.getLogger(Register.class);
    public void sendQuery(String qPara[], RoutingContext r,HttpRequest<JsonObject> httpRequest)
    {
        int flag=0;
        if(qPara[3]==null!=true)
            flag= 1;
        else if(qPara[4]==null!=true)
            flag= 2;
        else
            flag=0;
        switch(flag)
        {
            case 0:
                httpRequest.addQueryParam("lat", qPara[1] + "");
                httpRequest.addQueryParam("lon", qPara[2] + "");
                break;
            case 1:
                httpRequest.addQueryParam("q", qPara[3] + "");
                break;
            case 2:
                httpRequest.addQueryParam("zip", qPara[4] + "");

        }
        String mainkey=System.getenv("api_key");
        if(mainkey==null)
            mainkey="1";
        httpRequest.addQueryParam("appid",mainkey);
    }
//    Observable<Integer> save(String qPara[], Weather_data responseWdata, model mdl)
//    {
//        checker chk=mdl.saveWeatherData(responseWdata, qPara);
//        Observable<Integer> xx;
//        if(chk.getFlgCoord()==null) {
//            xx=Observable.fromSingle(chk.getFlgData());
//        }
//        else
//        {
//            xx= chk.getFlgData().mergeWith(chk.getFlgCoord()).mergeWith(chk.getFlgName()).toObservable();
//        }
//        return xx;
//    }

    public void search(String qPara[],model mdl,RoutingContext r)
    {
        if(qPara[3]!=null)
              mdl.searchByCity(qPara[3],r);
        else if(qPara[4]!=null)
            mdl.searchByZip(qPara[4],r);
        else
            mdl.search(Double.parseDouble(qPara[1]),Double.parseDouble(qPara[2]),r);
    }
    public void search(String qPara[],model mdl,RoutingContext r,double range)
    {
        if(qPara[3]!=null)
            mdl.searchByCity(qPara[3],r,range);
        else if(qPara[4]!=null)
            mdl.searchByZip(qPara[4],r,range);
        else
            mdl.search(Double.parseDouble(qPara[1]),Double.parseDouble(qPara[2]),r,range);
    }

    public void update(String params[], model mdl, HashMap<String,String> mp,RoutingContext rc)
    {
        ArrayList<String> lis=new ArrayList<>();
        Weather_data wdata=new Weather_data();
        for(String key:mp.keySet())
        {
            wdata.nset(key, mp.get(key));
            log.info(key+" "+wdata.get(key));
            lis.add(key);
        }
        if(params[3]!=null)
            mdl.updateCity(wdata,lis,params[3],rc);
        else if(params[4]!=null)
            mdl.updateZip(wdata,lis,params[4],rc);
        else
        mdl.update(wdata,lis,rc);
    }



}
