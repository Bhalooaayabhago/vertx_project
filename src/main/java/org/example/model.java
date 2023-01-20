package org.example;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
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
        connectOptions = new MySQLConnectOptions()
                .setPort(3306).setHost("127.0.0.1")
                .setDatabase("weather_data")
                .setUser("root").setPassword(System.getenv("dbpass"));
        log.info(System.getenv("dbpass"));
        poolOptions = new PoolOptions().setMaxSize(10);
        client = MySQLPool.client(vertx,connectOptions, poolOptions);
    }
    void saveWeatherData(Weather_data parameter_values)
    {
        log.info(parameter_values.getTemp());
        client.preparedQuery("INSERT IGNORE INTO " +
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
                ,parameter_values.getTimezone(), parameter_values.getDt()),
                ar->{
            if(ar.succeeded()) {
                log.info("Save weather data succeeded");
                Field flds[]=Weather_data.class.getDeclaredFields();
                log.info(flds.length);
                ArrayList<String> lis=new ArrayList<>();
                for(Field f:flds) {
                   log.info(f.getName());
                   lis.add(f.getName());
               }
                 update(parameter_values,lis);
               }
            else {
                log.info("Save weather data failed");
                ar.cause().printStackTrace();
            }
        });









    }
    void saveCity(String name,double lat,double lon)
    {
        client.preparedQuery("INSERT IGNORE INTO city(name,lat,lon) VALUES(?,?,?)").execute(Tuple.of(name,lat,lon),ar->{
            if(ar.failed())
            {
                log.info("Save City Failed");
                ar.cause().printStackTrace();
            }
            else
            {
                log.info("Save City Successful");
                client.preparedQuery("UPDATE city lat=?,lon=? where name=?").execute(Tuple.of(lat,lon,name),ares->{
                   if(ares.failed())
                   {
                       log.info("Update city coordinates failed");
                       ares.cause().printStackTrace();
                   }
                   else
                       log.info("Update city coordinates succeeded");
                });
            }
        });

    }
    void saveZip(String zip,double lat,double lon)
    {
        client.preparedQuery("INSERT IGNORE INTO city(name,lat,lon) VALUES(?,?,?)").execute(Tuple.of(zip,lat,lon),ar->{
            if(ar.failed())
            {
                log.info("Save Zip Failed");
                ar.cause().printStackTrace();
            }
            else
            {
                log.info("Save Zip Successful");
                client.preparedQuery("UPDATE zdata lat=?,lon=? where zip=?").execute(Tuple.of(lat,lon,zip),ares->{
                   if(ares.failed())
                   {
                       log.info("Update zip coordinates failed");
                       ares.cause().printStackTrace();
                   }
                   else
                       log.info("Update zip coordinates succeeded");
                });
             }
        });
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
                reply = new JsonObject("{Error:401}");
                  rc.response().putHeader("Content-Type", "application/json").end("{\"Error\":401}");
            }
            else
            {
                RowSet<Row> rows=res.result();
                if(rows.size()==0) {
                    log.info("No such record!");
                     rc.response().putHeader("Content-Type", "application/json").end("{\"Error\":101}");
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
                    for(Field f:flds)
                    {
                        log.info(f.getName()+" "+wdata.get(f.getName()));
                    }
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
     void searchByCityRange(String city,RoutingContext rc,double range)
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
                 searchRange(lat,lon,rc,range);
                 //wdata.copy(e);
             }
         });
         //return wdata;
      }
       void searchByZipRange(String zip,RoutingContext rc,double range)
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
                   searchRange(lat, lon, rc,range);
               }

           });
       }
       void searchRange(double lat, double lon, RoutingContext rc,double range)
       {
           //Weather_data wdata=new Weather_data();
           log.info(lat+" "+lon);
           client.preparedQuery("SELECT * FROM Data where lat=? AND lon=?").execute(Tuple.of(lat,lon),
                   res->{
               if(res.failed()) {
                   res.cause().printStackTrace();
                   log.info("Search failed");
                   JsonObject reply;
                   reply = new JsonObject("{Error:401}");
                     rc.response().putHeader("Content-Type", "application/json").end("{\"Error\":401}");
               }
               else
               {
                   RowSet<Row> rows=res.result();
                   if(rows.size()==0) {
                       log.info("No such record!");
                        rc.response().putHeader("Content-Type", "application/json").end("{\"Error\":101}");
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
                       for(Field f:flds)
                       {
                           log.info(f.getName()+" "+wdata.get(f.getName()));
                       }
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
    public void update(Weather_data wdata,ArrayList<String> paraNames)
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
            log.info("Update successful");
            return;
        }
        query_start+=" ";
        query_start+=query_end;
        log.info(query_start);
        client.preparedQuery(query_start).execute(Tuple.of(wdata.getLat(),wdata.getLon()),ar->{
            if(ar.failed())
            {
                log.info("Update failed");
                ar.cause().printStackTrace();
            }
            else
                log.info("update successful");
        });
    }
    public void updateCity(Weather_data wdata,ArrayList<String> paraNames,String city)
    {
        client.preparedQuery("INSERT IGNORE INTO city(name,lat,lon) Values (?,?,?)").execute(Tuple.of(city,wdata.getLat(),wdata.getLon()),ar->{
            if(ar.failed()) {
                log.info("update city failed");
                ar.cause().printStackTrace();
            }
            else
                update(wdata,paraNames);
        });
    }
    public void updateZip(Weather_data wdata,ArrayList<String> paraNames,String zip)
    {
        client.preparedQuery("INSERT IGNORE INTO zdata(name,lat,lon) Values (?,?,?)").execute(Tuple.of(zip,wdata.getLat(),wdata.getLon()),ar->{
            if(ar.failed()) {
                log.info("update city failed");
                ar.cause().printStackTrace();
            }
            else
                update(wdata,paraNames);
        });
    }


}
