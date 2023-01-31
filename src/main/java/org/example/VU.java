package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
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

    public Single<Weather_data> search(String qPara[], model mdl)
    {
        Single<Weather_data> obs;
        if(qPara[3]!=null)
              obs=mdl.searchByCity(qPara[3]);
        else if(qPara[4]!=null)
            obs=mdl.searchByZip(qPara[4]);
        else
            obs=mdl.search(Double.parseDouble(qPara[1]),Double.parseDouble(qPara[2]));
        return obs;
    }
    public Single<Weather_data> search(String qPara[],model mdl,double range)
    {
        Single<Weather_data> obs;
        if(qPara[3]!=null)
            obs=mdl.searchByCity(qPara[3],range);
        else if(qPara[4]!=null)
            obs=mdl.searchByZip(qPara[4],range);
        else
            obs=mdl.search(Double.parseDouble(qPara[1]),Double.parseDouble(qPara[2]),range);
        return obs;
    }

    public Observable<Integer> update(String params[], model mdl, HashMap<String,String> mp,RoutingContext rc)
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
            return mdl.updateCity(wdata,lis,params[3]);
        else if(params[4]!=null)
            return mdl.updateZip(wdata,lis,params[4]);
        else
        return mdl.update(wdata,lis);
    }



}
