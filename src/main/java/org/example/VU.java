package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;

import java.util.ArrayList;

public class VU
{

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
            case -1:
                r.response().putHeader("Content-Type","application/json").end("Error in Parameters");
                break;
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
    public JsonObject search(String qPara[],model mdl)
    {
        JsonObject replyToUser;
        ObjectMapper mapper=new ObjectMapper();
        if(qPara[3]!=null) {
            try {
                replyToUser=new JsonObject(mapper.writeValueAsString(mdl.searchByCity(qPara[3])));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        else if(qPara[4]!=null) {
            try {
                replyToUser=new JsonObject(mapper.writeValueAsString(mdl.searchByCity(qPara[4])));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        else
        {
            try {
                replyToUser=new JsonObject(mapper.writeValueAsString( mdl.search(Double.parseDouble(qPara[1]),Double.parseDouble(qPara[2]))));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
       return replyToUser;
    }
    public void update(String params[], model mdl, MultiMap mp)
    {
        ArrayList<String> lis=new ArrayList<>();
        lis.add("lat");
        lis.add("lon");
        Weather_data wdata=new Weather_data();
        for(String key:mp.names())
            wdata.set(key,mp.get(key));
        if(params[3]!=null)
        {
            lis.add("city");
            mdl.updateCity(wdata,lis,params[3]);
        }
        else if(params[4]!=null)
        {
            lis.add("zip");
            mdl.updateZip(wdata,lis,params[4]);
        }
        else
        mdl.update(wdata,lis);
    }



}
