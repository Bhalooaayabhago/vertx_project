package org.example;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;
import org.example.Register;

import java.util.ArrayList;

public class model
{
    private static final Logger log=  LoggerFactory.getLogger(Register.class);
    MySQLConnectOptions connectOptions;
    PoolOptions poolOptions;
    SqlClient client;
    model()
    {
        connectOptions = new MySQLConnectOptions()
                .setPort(3306).setHost("127.0.0.1")
                .setDatabase("weather_data")
                .setUser("root").setPassword(System.getenv("dbpass"));
        log.info(System.getenv("dbpass"));
        poolOptions = new PoolOptions().setMaxSize(10);
        client = MySQLPool.client(connectOptions, poolOptions);
    }
    void saveWeatherData(Weather_data parameter_values)
    {
        client.preparedQuery("INSERT IGNORE INTO " +
                "Data(lon,lat,weather_id,weather_main,weather_description," +
                "weather_icon,base,temp,feel_like,temp_min,temp_max," +
                "pressure,humidity,sea_level,grnd_level,visibility,wind_speed,wind_deg,wind_gust" +
                ",clouds_all,rain_1h,rain_3h,snow_1h,snow_3h,sunrise,sunset,timezone,dt) VALUES (" +
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);").execute(Tuple.of
                        (
                                parameter_values.getLon(),parameter_values.getLat(),parameter_values.getWeather_id(),parameter_values.getWeather_main(),
                parameter_values.getWeather_description(),parameter_values.getWeather_description(),
                parameter_values.getWeather_icon(),parameter_values.getBase(),parameter_values.getTemp(), parameter_values.getFeel_like(),
                parameter_values.getTemp_min(),parameter_values.getTemp_max(),parameter_values.getPressure(),
                parameter_values.getHumidity(),parameter_values.getSea_level(),parameter_values.getGrnd_level(),parameter_values.getVisibility(),
                parameter_values.getWind_speed(),parameter_values.getWind_deg(),parameter_values.getWind_gust(),parameter_values.getRain_1h(),parameter_values.getRain_3h(),
                parameter_values.getSnow_1h(),parameter_values.getSnow_3h(), parameter_values.getSunrise(),parameter_values.getSunset()
                ,parameter_values.getTimezone(), parameter_values.getDt()),
                ar->{
            if(ar.succeeded())
                log.info("Save weather data succeeded");
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
            }
        });
    }
    Weather_data search(double lat, double lon)
    {
        Weather_data wdata=new Weather_data();
        client.preparedQuery("SELECT * FROM Data where lat=? AND lon=?").execute(Tuple.of(lat,lon),
                res->{
            if(res.failed()) {
                res.cause().printStackTrace();
                log.info("Search failed");
                wdata.setLat(-1000);
                wdata.setLon(-1000);
            }
            else
            {
                RowSet<Row> rows=res.result();
                if(rows.size()==0) {
                    log.info("No such record!");
                    wdata.setLat(-1000);
                    wdata.setLon(-1000);
                }
                for(Row xx:rows)
                {

                    for(int i=0;i<=27;i++)
                    {
                        String curr=xx.getColumnName(i);
                        if(i>=3&&i<=6) {
                            wdata.set(curr,xx.getString(curr));
                            continue;
                        }
                        if(i>=24&&i<=27) {
                            wdata.set(curr,xx.getInteger(curr));
                            continue;
                        }
                        wdata.set(curr,xx.getDouble(curr));
                    }
                }
            }
        });
        return wdata;
    }
    Weather_data searchByZip(String zip)
    {
         Weather_data wdata = new Weather_data();
        client.preparedQuery("SELECT * FROM zdata where zip=?").execute(Tuple.of(zip), ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
                log.info("search failed");
                wdata.setLat(-1000);
                wdata.setLon(-1000);
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
                Weather_data e=search(lat,lon);
                wdata.copy(e);
            }

        });
        return wdata;
    }
    Weather_data searchByCity(String city)
    {
        Weather_data wdata = new Weather_data();
        client.preparedQuery("SELECT * FROM city where zip=?").execute(Tuple.of(city), ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
                log.info("search failed");
                wdata.setLat(-1000);
                wdata.setLon(-1000);
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
                Weather_data e=search(lat,lon);
                wdata.copy(e);
            }
        });
        return wdata;
     }
    public void update(Weather_data wdata,ArrayList<String> paraNames)
    {
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
            query_start+=(paraNames.get(i)+"="+wdata.get(paraNames.get(i)));
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
