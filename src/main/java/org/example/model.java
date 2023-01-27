package org.example;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.rxjava3.SingleHelper;
import io.vertx.sqlclient.*;
import org.example.Register;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class model
{
    private static final Logger log=  LoggerFactory.getLogger(Register.class);
    MySQLConnectOptions connectOptions;
    PoolOptions poolOptions;
    SqlClient client;
    model(Vertx vertx)
    {
        log.info(System.getenv("dbpass"));
        connectOptions = new MySQLConnectOptions()
                .setPort(3306).setHost("127.0.0.1")
                .setDatabase("weather_data")
                .setUser("root").setPassword("crystalkaran1A@");
        poolOptions = new PoolOptions().setMaxSize(10);
        client = MySQLPool.client(vertx,connectOptions, poolOptions);
    }
    public SingleSource<Integer> apply(AsyncResult<RowSet<Row>> rowSetAsyncResult) throws Throwable {
        Single<Integer> ss;
        if(rowSetAsyncResult.failed())
            ss=Single.just(0);
        else {
            ss = Single.just(1);
        }
        return ss;
    }
    Observable<Integer> saveWeatherData(Weather_data parameter_values,String qPara[])
    {
        log.info(parameter_values.getTemp());
        AsyncResult<RowSet<Row>> ar=client.preparedQuery("INSERT IGNORE INTO " +
                "Data(lon,lat,weather_id,weather_main,weather_description," +
                "weather_icon,base,temp,feel_like,temp_min,temp_max," +
                "pressure,humidity,sea_level,grnd_level,visibility,wind_speed,wind_deg,wind_gust" +
                ",clouds_all,rain_1h,rain_3h,snow_1h,snow_3h,sunrise,sunset,timezone,dt) VALUES (" +
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);").execute(Tuple.of
                        (
                                parameter_values.getLon(),parameter_values.getLat(),parameter_values.getWeather_id(),parameter_values.getWeather_main(),
                parameter_values.getWeather_description(),
                parameter_values.getWeather_icon(),parameter_values.getBase(),parameter_values.getTemp(), parameter_values.getFeel_like(),
                parameter_values.getTemp_min(),parameter_values.getTemp_max(),parameter_values.getPressure(),
                parameter_values.getHumidity(),parameter_values.getSea_level(),parameter_values.getGrnd_level(),parameter_values.getVisibility(),
                parameter_values.getWind_speed(),parameter_values.getWind_deg(),parameter_values.getWind_gust(),parameter_values.getClouds_all(),parameter_values.getRain_1h(),parameter_values.getRain_3h(),
                parameter_values.getSnow_1h(),parameter_values.getSnow_3h(), parameter_values.getSunrise(),parameter_values.getSunset()
                ,parameter_values.getTimezone(), parameter_values.getDt()));
               Single<AsyncResult<RowSet<Row>>> res=Single.just(ar);
               Single<AsyncResult<RowSet<Row>>> resCity;
               Single<AsyncResult<RowSet<Row>>> resCityCoord;
               Single<AsyncResult<RowSet<Row>>> resZip;
               Single<AsyncResult<RowSet<Row>>> resZipCoord;
               Observable<Integer> ress;
               Single<Integer> ii;
               ii=res.flatMap(this::apply);
               //checker chk=new checker();
               ress=Observable.fromSingle(ii);
               if(qPara[3]!=null)
               {
                  resCity=saveCity(qPara[3],parameter_values.getLat(),parameter_values.getLon());
                  resCityCoord=updateCityCoordinates(qPara[3],parameter_values.getLat(),parameter_values.getLon());
                   ress=ress.mergeWith(resCity.flatMap(this::apply));
                   ress=ress.mergeWith( resCityCoord.flatMap(this::apply));
               }
               else if(qPara[4]!=null)
               {
                     resZip=saveZip(qPara[4],parameter_values.getLat(),parameter_values.getLon());
                     resZipCoord=updateZipCoordinates(qPara[4],parameter_values.getLat(),parameter_values.getLon());
                   ress=ress.mergeWith(resZip.flatMap(this::apply));
                   ress=ress.mergeWith( resZipCoord.flatMap(this::apply));
               }
               return ress;
    }
    Single<AsyncResult<RowSet<Row>>> saveCity(String name, double lat, double lon) {
        AsyncResult<RowSet<Row>> ar = client.preparedQuery("INSERT IGNORE INTO city(name,lat,lon) VALUES(?,?,?)").execute(Tuple.of(name, lat, lon));
        Single<AsyncResult<RowSet<Row>>> si = Single.just(ar);
        //Single<AsyncResult<RowSet<Row>>> sii=updateCityCoordinates(name,lat,lon);
        return si;
    }
    Single<AsyncResult<RowSet<Row>>> updateCityCoordinates(String name,double lat,double lon)
    {
        AsyncResult<RowSet<Row>> ar=client.preparedQuery("UPDATE city lat=?,lon=? where name=?").execute(Tuple.of(lat,lon,name));
        Single<AsyncResult<RowSet<Row>>> si = Single.just(ar);
        return si;
    }
    Single<AsyncResult<RowSet<Row>>> saveZip(String zip, double lat, double lon)
    {
        AsyncResult<RowSet<Row>> ar = client.preparedQuery("INSERT IGNORE INTO zdata(zip,lat,lon) VALUES(?,?,?)").execute(Tuple.of(zip,lat,lon));
        Single<AsyncResult<RowSet<Row>>> si=Single.just(ar);
        //Single<AsyncResult<RowSet<Row>>> sii=updateZipCoordinates(zip,lat,lon);
        return si;
    }
    Single<AsyncResult<RowSet<Row>>> updateZipCoordinates(String zip,double lat,double lon)
    {
        AsyncResult<RowSet<Row>> ar=client.preparedQuery("UPDATE zdata lat=?,lon=? where zip=?").execute(Tuple.of(lat,lon,zip));
        Single<AsyncResult<RowSet<Row>>> si = Single.just(ar);
        return si;
    }
    void search(double lat, double lon, RoutingContext rc)
    {
        //Weather_data wdata=new Weather_data();
        log.info(lat+" "+lon);
        client.preparedQuery("SELECT * FROM Data where lat=? AND lon=?").execute(Tuple.of(lat,lon),
                res->{
            if(res.failed()) {
                res.cause().printStackTrace();
                log.info("Search failed");
                JsonObject reply;
                reply = new JsonObject("{\"Error\":401}");
                  rc.response().putHeader("Content-Type", "application/json").end("{\"Error\":401}");
            }
            else
            {
                RowSet<Row> rows=res.result();
                if(rows.size()==0) {
                    log.info("No such record!");
                     rc.response().putHeader("Content-Type", "application/json").end("{\"Error\":101}");
                     return;
               }
                Weather_data wdata=new Weather_data();
                for(Row xx:rows)
                {
                    log.info("Record found");
                    for(int i=0;i<=27;i++)
                    {
                        String curr=xx.getColumnName(i);
                        //log.info(curr);
                        if(i>=3&&i<=6) {
                            wdata.set(curr,xx.getString(curr));
                           log.info(xx.getString(curr));
                            continue;
                        }
                        if(i>=24&&i<=27) {
                            wdata.set(curr,xx.getInteger(curr));
                           //log.info(xx.getInteger(curr));
                            continue;
                        }
                        wdata.set(curr,xx.getDouble(curr));
                       //log.info(xx.getDouble(curr));
                    }
                   /* Field flds[]=Weather_data.class.getDeclaredFields();
                    for(Field f:flds)
                    {
                        log.info(f.getName()+" "+wdata.get(f.getName()));
                    }*/
                }
                ObjectMapper mapper=new ObjectMapper();
                JsonObject reply;
                try {
                     reply=new JsonObject(mapper.writeValueAsString(wdata));
                     rc.response().putHeader("Content-Type", "application/json").end(reply.toString());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }
    void searchByZip(String zip,RoutingContext rc)
    {
         //Weather_data wdata = new Weather_data();
        client.preparedQuery("SELECT * FROM zdata where zip=?").execute(Tuple.of(zip), ar -> {

            if (ar.failed()) {
                ar.cause().printStackTrace();
                log.info("search failed");
                rc.response().putHeader("Content-Type", "application/json").end("{\"Error\":401}");
            }
            else{
                String ans;
                RowSet<Row> rx = ar.result();
                log.info(rx.size());
                double lat = -1000, lon = -1000;
                for (Row rr : rx) {
                    lat = rr.getDouble("lat");
                    lon = rr.getDouble("lon");
                }
                log.info(lat + " " + lon);
                search(lat, lon, rc);
            }

        });
    }
    void searchByCity(String city,RoutingContext rc)
    {
        //Weather_data wdata = new Weather_data();
        client.preparedQuery("SELECT * FROM city where name=?").execute(Tuple.of(city), ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
                log.info("search failed");
                JsonObject reply;
                  reply = new JsonObject("\"Error\":401");
                rc.response().putHeader("Content-Type","application/json").end("{\"Error\":401}");
            }
            else {
                String ans;
                RowSet<Row> rx = ar.result();
                log.info(rx.size());
                double lat=-1000,lon=-1000;
                for (Row rr : rx)
                {
                    lat= rr.getDouble("lat");
                    lon = rr.getDouble("lon");
                }
                log.info(lat + " " + lon);
                search(lat,lon,rc);
                //wdata.copy(e);
            }
        });
        //return wdata;
     }
     void searchByCity(String city,RoutingContext rc,double range)
     {
         //Weather_data wdata = new Weather_data();
         client.preparedQuery("SELECT * FROM city where name=?").execute(Tuple.of(city), ar -> {
             if (ar.failed())
             {
                   ar.cause().printStackTrace();
                   log.info("search failed");
                   JsonObject reply;
                   reply = new JsonObject("\"Error\":401");
                   rc.response().putHeader("Content-Type","application/json").end("{\"Error\":401}");
             }
             else
             {
                    String ans;
                    RowSet<Row> rx = ar.result();
                    log.info(rx.size());
                    double lat=-1000,lon=-1000;
                    for (Row rr : rx)
                    {
                       lat= rr.getDouble("lat");
                       lon = rr.getDouble("lon");
                    }
                    log.info(lat + " " + lon);
                    search(lat,lon,rc,range);
                    //wdata.copy(e);
             }
         });
         //return wdata;
      }
       void searchByZip(String zip,RoutingContext rc,double range)
       {
            //Weather_data wdata = new Weather_data();
           client.preparedQuery("SELECT * FROM zdata where zip=?").execute(Tuple.of(zip), ar -> {
               if (ar.failed())
               {
                   ar.cause().printStackTrace();
                   log.info("search failed");
                   rc.response().putHeader("Content-Type", "application/json").end("{\"Error\":401}");
               }
               else
               {
                   RowSet<Row> rx = ar.result();
                   log.info(rx.size());
                   double lat = -1000, lon = -1000;
                   for (Row rr : rx)
                   {
                       lat = rr.getDouble("lat");
                       lon = rr.getDouble("lon");
                   }
                   log.info(lat + " " + lon);
                   search(lat, lon, rc,range);
               }
           });
       }
       void search(double lat, double lon, RoutingContext rc,double range)
       {
           //Weather_data wdata=new Weather_data();
           log.info(lat+" "+lon);
           client.preparedQuery("SELECT * FROM Data where 13462*asin(sqrt(power(sin(((lat-?)*0.01745329251)/2),2)+power(sin(((lon-?)*0.01745329251)/2),2)*cos(lat)*cos(?))) <= ?").execute(Tuple.of(lat,lon,lat,range),
                   res->{
               if(res.failed())
               {
                   res.cause().printStackTrace();
                   log.info("Search failed");
                   JsonObject reply;
                   reply = new JsonObject("{Error:401}");
                   rc.response().putHeader("Content-Type", "application/json").end("{\"Error\":401}");
               }
               else
               {
                   RowSet<Row> rows=res.result();
                   if(rows.size()==0)
                   {
                       log.info("No such record!");
                        rc.response().putHeader("Content-Type", "application/json").end("{\"Error\":101}");
                        return;
                  }
                   Weather_data wdata=new Weather_data();
                   for(Row xx:rows)
                   {
                       log.info("Record found");
                       for(int i=0;i<=27;i++)
                       {
                           String curr=xx.getColumnName(i);
                           log.info(curr);
                           if(i>=3&&i<=6) {
                               wdata.set(curr,xx.getString(curr));
                              log.info(xx.getString(curr));
                               continue;
                           }
                           if(i>=24&&i<=27) {
                               wdata.set(curr,xx.getInteger(curr));
                              log.info(xx.getInteger(curr));
                               continue;
                           }
                           wdata.set(curr,xx.getDouble(curr));
                          log.info(xx.getDouble(curr));
                       }
                       Field flds[]=Weather_data.class.getDeclaredFields();
                       /*for(Field f:flds)
                       {
                           log.info(f.getName()+" "+wdata.get(f.getName()));
                       } */
                   }
                   ObjectMapper mapper=new ObjectMapper();
                   JsonObject reply;
                   try {
                        reply=new JsonObject(mapper.writeValueAsString(wdata));
                   } catch (JsonProcessingException e) {
                       throw new RuntimeException(e);
                   }
                   rc.response().putHeader("Content-Type","application/json").end(reply.toString());
               }
           });
       }
    public void update(Weather_data wdata,ArrayList<String> paraNames,RoutingContext rc)
    {
        //log.info("update begin");
        for(String s:paraNames)
            log.info(s);
        String query_start="",query_end="";
        query_start="update Data ";
        query_end="where lat=? and lon=?";
        int cnt=0;
        for(int i=0;i<paraNames.size();i++)
        {
            String curr=paraNames.get(i);
            if(curr.equals("lat")||curr.equals("lon")||curr.equals("city")||curr.equals("zip"))
                continue;
            query_start+=(cnt==0)?"set ":",";
            query_start+=(paraNames.get(i)+"=");
             if(curr.equals("weather_icon")||curr.equals("weather_main")||curr.equals("base")||curr.equals("weather_description"))
                        query_start+="\""+wdata.get(paraNames.get(i))+"\"";
             else
                 query_start+=wdata.get(paraNames.get(i));
            //log.info(query_start);
            cnt++;
        }
        if(cnt==0)
        {
            if(rc==null)
            log.info("Update successful");
            else
                rc.response().putHeader("Content-Type","application/json").end("{\"SUCCESS\":\"UPDATED\"}");
            return;
        }
        query_start+=" ";
        query_start+=query_end;
        log.info(query_start);
        client.preparedQuery(query_start).execute(Tuple.of(wdata.getLat(),wdata.getLon()),ar->{
            if(ar.failed())
            {
                 if(rc==null)
                 log.info("Update successful");
                 else
                     rc.response().putHeader("Content-Type","application/json").end("{\"error\":\"UPDATE failed\"}");
                 ar.cause().printStackTrace();
            }
            else
            {
                 if(rc==null)
                 log.info("Update successful");
                 else
                     rc.response().putHeader("Content-Type","application/json").end("{\"SUCCESS\":\"UPDATED\"}");
             }
        });
    }
    public void updateCity(Weather_data wdata,ArrayList<String> paraNames,String city,RoutingContext rc)
    {
        log.info("update city");
        client.preparedQuery("SELECT * from city where name=?").execute(Tuple.of(city),ar->{
            if(ar.failed()) {
                log.info("update city failed");
                ar.cause().printStackTrace();
            }
            else
            {
                if(ar.result().size()==0)
                {

                    client.preparedQuery("INSERT IGNORE INTO city(name,lat,lon) Values (?,?,?)").execute(Tuple.of(city,wdata.getLat(),wdata.getLon()),arr->{
                        if(arr.failed()) {
                            log.info("update city failed");
                            arr.cause().printStackTrace();
                        }
                        else {
                            //log.info("check");
                            update(wdata, paraNames,rc);
                        }
                    });
                }
                else
                {
                         for(Row rws:ar.result())
                         {
                             wdata.setLat(rws.getDouble("lat"));
                              wdata.setLon(rws.getDouble("lon"));
                         }
                         update(wdata,paraNames,rc);
                }
            }
        });
     }
    public void updateZip(Weather_data wdata,ArrayList<String> paraNames,String zip,RoutingContext rc)
    {
        client.preparedQuery("select * from zdata where zip=?").execute(Tuple.of(zip),ar->{
            if(ar.failed()) {
                log.info("update city failed");
                ar.cause().printStackTrace();
            }
                else
                {
                    if(ar.result().size()==0)
                    {

                        client.preparedQuery("INSERT IGNORE INTO zdata(zip,lat,lon) Values (?,?,?)").execute(Tuple.of(zip,wdata.getLat(),wdata.getLon()),arr->{
                            if(arr.failed()) {
                                log.info("update city failed");
                                arr.cause().printStackTrace();
                            }
                            else
                                update(wdata,paraNames,rc);
                        });
                    }
                    else
                    {
                             for(Row rws:ar.result())
                             {
                                 wdata.setLat(rws.getDouble("lat"));
                                  wdata.setLat(rws.getDouble("lon"));
                             }
                             update(wdata,paraNames,rc);
                    }
                }

         });
    }


}
