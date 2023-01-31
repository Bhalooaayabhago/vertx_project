package org.example;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;
import io.vertx.mysqlclient.*;

import javax.sql.RowSetEvent;
import java.util.ArrayList;

public class model
{
    private static final Logger log=  LoggerFactory.getLogger(Register.class);
    MySQLConnectOptions connectOptions;
    PoolOptions poolOptions;
    io.vertx.rxjava3.sqlclient.SqlClient rxClient;
    SqlClient client;
    model(Vertx vertx) {
        log.info(System.getenv("dbpass"));

        connectOptions = new MySQLConnectOptions()
                .setPort(3306).setHost("127.0.0.1")
                .setDatabase("weather_data")
                .setUser("root").setPassword("crystalkaran1A@");
        poolOptions = new PoolOptions().setMaxSize(10);
       // io.vertx.rxjava3.mysqlclient.MySQLPool.pool((io.vertx.rxjava3.core.Vertx) vertx,connectOptions,poolOptions);
        client= MySQLPool.pool(vertx,connectOptions,poolOptions);
       rxClient=io.vertx.rxjava3.mysqlclient.MySQLPool.pool(io.vertx.rxjava3.core.Vertx.vertx(),connectOptions,poolOptions);
    }
    public SingleSource<Integer> apply1(AsyncResult<RowSet<Row>> rowSetAsyncResult) throws Throwable {
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
        AsyncResult<RowSet< Row >> ar=client.preparedQuery("INSERT IGNORE INTO " +
                "Data(lon,lat,weather_id,weather_main,weather_description," +
                "weather_icon,base,temp,feel_like,temp_min,temp_max," +
                "pressure,humidity,sea_level,grnd_level,visibility,wind_speed,wind_deg,wind_gust" +
                ",clouds_all,rain_1h,rain_3h,snow_1h,snow_3h,sunrise,sunset,timezone,dt) VALUES (" +
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);").execute(Tuple.of
                        (parameter_values.getLon(),parameter_values.getLat(),parameter_values.getWeather_id(),parameter_values.getWeather_main(),
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
               ii=res.flatMap(this::apply1);
               //checker chk=new checker();
               ress=Observable.fromSingle(ii);
               if(qPara[3]!=null)
               {
                  resCity=saveCity(qPara[3],parameter_values.getLat(),parameter_values.getLon());
                  resCityCoord=updateCityCoordinates(qPara[3],parameter_values.getLat(),parameter_values.getLon());
                   ress=ress.mergeWith(resCity.flatMap(this::apply1));
                   ress=ress.mergeWith( resCityCoord.flatMap(this::apply1));
               }
               else if(qPara[4]!=null)
               {
                     resZip=saveZip(qPara[4],parameter_values.getLat(),parameter_values.getLon());
                     resZipCoord=updateZipCoordinates(qPara[4],parameter_values.getLat(),parameter_values.getLon());
                   ress=ress.mergeWith(resZip.flatMap(this::apply1));
                   ress=ress.mergeWith( resZipCoord.flatMap(this::apply1));
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
        public SingleSource<Weather_data> applyMain(io.vertx.rxjava3.sqlclient.RowSet<io.vertx.rxjava3.sqlclient.Row> rows) throws Throwable {
            if (rows == null) {
                Weather_data err = new Weather_data();
                err.setLat(-1000);
                err.setLon(-1000);
                return Single.just(err);
            } else if (rows.size() == 0) {
                Weather_data err = new Weather_data();
                err.setLat(-1000);
                err.setLon(1000);
                return Single.just(err);
            }
            else
            {
                Weather_data wdata = new Weather_data();
                for (io.vertx.rxjava3.sqlclient.Row xx : rows) {
                    log.info("Record found");
                    for (int i = 0; i <= 27; i++) {
                        String curr = xx.getColumnName(i);
                        if (i >= 3 && i <= 6) {
                            wdata.set(curr, xx.getString(curr));
                            log.info(xx.getString(curr));
                            continue;
                        }
                        if (i >= 24 && i <= 27) {
                            wdata.set(curr, xx.getInteger(curr));
                            continue;
                        }
                        wdata.set(curr, xx.getDouble(curr));
                    }
                }
                return Single.just(wdata);
            }
        }
    Single<Weather_data> search(double lat, double lon)
    {
        //Weather_data wdata=new Weather_data();
        log.info(lat+" "+lon);
        Single<Weather_data> result;
        Future<RowSet<Row>> qResult;
        //client.preparedQuery("SELECT * FROM Data where lat=? AND lon=?");
        Single<Weather_data> ans=rxClient.preparedQuery("SELECT * FROM Data where lat=? AND lon=?").rxExecute(io.vertx.rxjava3.sqlclient.Tuple.of(lat,lon)).flatMap(this::applyMain);
//        qResult=client.preparedQuery("SELECT * FROM Data where lat=? AND lon=?").execute(Tuple.of(lat,lon));
//        Single<Future<RowSet<Row>>> asyncResultSingle=Single.just(qResult);
//        result=asyncResultSingle.flatMap(this::applyMain);
//        return result;
        return ans;
    }
    public SingleSource<Weather_data> applySearch(io.vertx.rxjava3.sqlclient.RowSet<io.vertx.rxjava3.sqlclient.Row> rows) throws Throwable {
        if (rows==null) {
            log.info("search failed");
            Weather_data err=new Weather_data();
            err.setLat(-1000);
            err.setLon(-1000);
            return Single.just(err);
        }
        else
        {
           // log.info(ar.failed());
            double lat = -1000, lon = -1000;
            for (io.vertx.rxjava3.sqlclient.Row rr : rows) {
                lat = rr.getDouble("lat");
                lon = rr.getDouble("lon");
            }
            log.info(lat + " " + lon);
            return search(lat, lon);
        }
    }
    public SingleSource<coordinates> applySearchRange(io.vertx.rxjava3.sqlclient.RowSet<io.vertx.rxjava3.sqlclient.Row> rows) throws Throwable {
        if (rows==null)
        {
            log.info("search failed");
            coordinates err=new coordinates(-1000,-1000,0);
            return Single.just(err);
        }
        else
        {
            double lat = -1000, lon = 1000;
            for (io.vertx.rxjava3.sqlclient.Row rr : rows) {
                lat = rr.getDouble("lat");
                lon = rr.getDouble("lon");
            }
            log.info(lat + " " + lon);
            coordinates crd=new coordinates(lat,lon,0);
            return Single.just(crd);
        }
    }
    public SingleSource<Weather_data> crdToData(coordinates crd) throws Throwable
    {
        return search(crd.getLat(),crd.getLon(),crd.getRange());
    }

    Single<Weather_data> searchByZip(String zip)
    {
        Single<io.vertx.rxjava3.sqlclient.RowSet<io.vertx.rxjava3.sqlclient.Row>> zipRes;
        zipRes=rxClient.preparedQuery("SELECT * FROM zdata where zip=?").rxExecute(io.vertx.rxjava3.sqlclient.Tuple.of(zip));
        return zipRes.flatMap(this::applySearch);
    }
    Single<Weather_data> searchByCity(String city)
    {
        Single<io.vertx.rxjava3.sqlclient.RowSet<io.vertx.rxjava3.sqlclient.Row>> cityRes;
        cityRes=rxClient.preparedQuery("SELECT * FROM city where name=?").rxExecute(io.vertx.rxjava3.sqlclient.Tuple.of(city));
         return cityRes.flatMap(this::applySearch);
     }
    Single<Weather_data> searchByZip(String zip,double range)
    {
        Single<io.vertx.rxjava3.sqlclient.RowSet<io.vertx.rxjava3.sqlclient.Row>> zipRes;
        zipRes=rxClient.preparedQuery("SELECT * FROM zdata where zip=?").rxExecute(io.vertx.rxjava3.sqlclient.Tuple.of(zip));
        return zipRes.flatMap(this::applySearchRange).flatMap(obs->{obs.setRange(range);return Single.just(obs);}).flatMap(this::crdToData);
    }
    Single<Weather_data> searchByCity(String city,double range)
    {
        Single<io.vertx.rxjava3.sqlclient.RowSet<io.vertx.rxjava3.sqlclient.Row>> cityRes;
        cityRes=rxClient.preparedQuery("SELECT * FROM city where name=?").rxExecute(io.vertx.rxjava3.sqlclient.Tuple.of(city));
        return cityRes.flatMap(this::applySearchRange).flatMap(obs->{obs.setRange(range);return Single.just(obs);}).flatMap(this::crdToData);
    }
      Single<Weather_data> search(double lat, double lon,double range)
      {
           //Weather_data wdata=new Weather_data();io.vertx.sqlclient.Tuple.of(lat,lon,lat,range)
           Single<io.vertx.rxjava3.sqlclient.RowSet<io.vertx.rxjava3.sqlclient.Row>> rws=
           rxClient.preparedQuery("SELECT * FROM Data where 13462*asin(sqrt(power(sin(((lat-?)*0.01745329251)/2),2)+power(sin(((lon-?)*0.01745329251)/2),2)*cos(lat)*cos(?))) <= ?").execute(io.vertx.rxjava3.sqlclient.Tuple.of(lat,lon,lat,range));
           return rws.flatMap(this::applyMain);
           //return Single.just(ar).flatMap(this::applyMain);
       }
       public ObservableSource<Integer> applyUpdate(io.vertx.rxjava3.sqlclient.RowSet<io.vertx.rxjava3.sqlclient.Row> rows) throws Throwable
       {
           if(rows==null)
               return Observable.just(0);
           return Observable.just(1);
       }

  public Observable<Integer> update(Weather_data wdata,ArrayList<String> paraNames)
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
            return Observable.just(1);
        }
        query_start+=" ";
        query_start+=query_end;
        log.info(query_start);
       return rxClient.preparedQuery(query_start).rxExecute(io.vertx.rxjava3.sqlclient.Tuple.of(wdata.getLat(),wdata.getLon())).flatMapObservable(this::applyUpdate);
    }

    Observable<Integer> updateCity(Weather_data wdata,ArrayList<String> paraNames,String city)
    {
        log.info("update city");
        return rxClient.preparedQuery("SELECT * from city where name=?").rxExecute(io.vertx.rxjava3.sqlclient.Tuple.of(city)).flatMapObservable(new Function<io.vertx.rxjava3.sqlclient.RowSet<io.vertx.rxjava3.sqlclient.Row>, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(io.vertx.rxjava3.sqlclient.RowSet<io.vertx.rxjava3.sqlclient.Row> rows) throws Throwable {
                if(rows==null)
                    return Observable.just(0);
                else {
                    if(rows.size()==0) {
                        Observable<Integer> si;
                        Observable<Integer> sii;
                        si = rxClient.preparedQuery("INSERT IGNORE INTO city(name,lat,lon) Values (?,?,?)").rxExecute(io.vertx.rxjava3.sqlclient.Tuple.of(city, wdata.getLat(), wdata.getLon())).flatMapObservable(model.this::applyUpdate);
                        sii=update(wdata,paraNames);
                        return si.mergeWith(sii);
                    }
                    else {
                        Observable<Integer> sii=update(wdata,paraNames);
                        return sii;
                    }
                }
            }
        });

     }
    Observable<Integer> updateZip(Weather_data wdata,ArrayList<String> paraNames,String zip)
    {
        log.info("update city");
        return rxClient.preparedQuery("SELECT * from zdata where zip=?").rxExecute(io.vertx.rxjava3.sqlclient.Tuple.of(zip)).flatMapObservable(new Function<io.vertx.rxjava3.sqlclient.RowSet<io.vertx.rxjava3.sqlclient.Row>, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(io.vertx.rxjava3.sqlclient.RowSet<io.vertx.rxjava3.sqlclient.Row> rows) throws Throwable {
                if(rows==null)
                    return Observable.just(0);
                else {
                    if(rows.size()==0) {
                        Observable<Integer> si;
                        Observable<Integer> sii;
                        si = rxClient.preparedQuery("INSERT IGNORE INTO zdata(zip,lat,lon) Values (?,?,?)").rxExecute(io.vertx.rxjava3.sqlclient.Tuple.of(zip, wdata.getLat(), wdata.getLon())).flatMapObservable(model.this::applyUpdate);
                        sii=update(wdata,paraNames);
                        return si.mergeWith(sii);
                    }
                    else {
                        Observable<Integer> sii=update(wdata,paraNames);
                        return sii;
                    }
                }
            }
        });

    }
}
