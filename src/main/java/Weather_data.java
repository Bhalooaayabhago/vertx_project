import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Weather_data
{
    String weather_icon,weather_description,weather_main,base;
    private double lat,lon,weather_id,temp,feel_like,temp_max,temp_min,sea_level
            ,grnd_level,humidity,pressure,visibility,wind_speed,wind_deg,wind_gust
            ,clouds_all,snow_1h,snow_3h,rain_1h,rain_3h;
    int sunrise,sunset,timezone, dt;

    public void setBase(String base) {
        this.base = base;
    }

    public void setClouds_all(double clouds_all) {
        this.clouds_all = clouds_all;
    }

    public void setDt(int dt) {
        this.dt = dt;
    }

    public void setFeel_like(double feel_like) {
        this.feel_like = feel_like;
    }

    public void setGrnd_level(double grnd_level) {
        this.grnd_level = grnd_level;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public void setRain_1h(double rain_1h) {
        this.rain_1h = rain_1h;
    }

    public void setRain_3h(double rain_3h) {
        this.rain_3h = rain_3h;
    }

    public double getWeather_id() {
        return weather_id;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getFeel_like() {
        return feel_like;
    }

    public double getTemp_max() {
        return temp_max;
    }

    public void setTemp_max(double temp_max) {
        this.temp_max = temp_max;
    }

    public double getTemp_min() {
        return temp_min;
    }

    public void setTemp_min(double temp_min) {
        this.temp_min = temp_min;
    }

    public double getSea_level() {
        return sea_level;
    }

    public double getGrnd_level() {
        return grnd_level;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getPressure() {
        return pressure;
    }

    public double getVisibility() {
        return visibility;
    }

    public void setVisibility(double visibility) {
        this.visibility = visibility;
    }

    public double getWind_speed() {
        return wind_speed;
    }

    public void setWind_speed(double wind_speed) {
        this.wind_speed = wind_speed;
    }

    public double getWind_deg() {
        return wind_deg;
    }

    public void setWind_deg(double wind_deg) {
        this.wind_deg = wind_deg;
    }

    public double getWind_gust() {
        return wind_gust;
    }

    public void setWind_gust(double wind_gust) {
        this.wind_gust = wind_gust;
    }

    public double getClouds_all() {
        return clouds_all;
    }

    public double getSnow_1h() {
        return snow_1h;
    }

    public void setSnow_1h(double snow_1h) {
        this.snow_1h = snow_1h;
    }

    public double getSnow_3h() {
        return snow_3h;
    }

    public void setSnow_3h(double snow_3h) {
        this.snow_3h = snow_3h;
    }

    public double getRain_1h() {
        return rain_1h;
    }

    public double getRain_3h() {
        return rain_3h;
    }

    public int getSunrise() {
        return sunrise;
    }

    public void setSunrise(int sunrise) {
        this.sunrise = sunrise;
    }

    public int getSunset() {
        return sunset;
    }

    public void setSunset(int sunset) {
        this.sunset = sunset;
    }

    public int getTimezone() {
        return timezone;
    }

    public void setTimezone(int timezone) {
        this.timezone = timezone;
    }

    public int getDt() {
        return dt;
    }

    public void setWeather_id(double weather_id) {
        this.weather_id = weather_id;
    }

    public String getWeather_description() {
        return weather_description;
    }

    public void setWeather_description(String weather_description) {
        this.weather_description = weather_description;
    }

    public String getBase() {
        return base;
    }

    public void setSea_level(double sea_level) {
        this.sea_level = sea_level;
    }

    public String getWeather_main() {
        return weather_main;
    }

    public void setWeather_main(String weather_main) {
        this.weather_main = weather_main;
    }

    public String getWeather_icon() {
        return weather_icon;
    }

    public void setWeather_icon(String weather_icon) {
        this.weather_icon = weather_icon;
    }
    public void extractFromJson(JsonObject job)
    {
        JsonObject coord=job.getJsonObject("coord");
        if(coord!=null) {
            lat = coord.getDouble("lat");
            lon = coord.getDouble("lon");
        }

        JsonArray weather=job.getJsonArray("weather");
        if(weather!=null) {
            JsonObject mw = weather.getJsonObject(0);
            weather_id = mw.getDouble("id");
            weather_main = mw.getString("main");
            weather_description = mw.getString("description");
            weather_icon = mw.getString("icon");
        }

        base=job.getString("base");
        JsonObject Main=job.getJsonObject("main");
        if(Main!=null) {
            temp = Main.getDouble("temp");
            feel_like = Main.getDouble("feels_like");
            temp_min = Main.getDouble("temp_min");
            temp_max = Main.getDouble("temp_max");
            pressure = Main.getDouble("pressure");
            humidity = Main.getDouble("humidity");
            if(Main.getDouble("sea_level")!=null)
                sea_level = Main.getDouble("sea_level");
            if(Main.getDouble("grnd_level")!=null)
                grnd_level = Main.getDouble("grnd_level");
        }

         visibility=job.getDouble("visibility");

        JsonObject wind=job.getJsonObject("wind");
        if(wind!=null) {
            if(wind.getDouble("speed")!=null)
                wind_speed = wind.getDouble("speed");
            if(wind.getDouble("deg")!=null)
                wind_deg = wind.getDouble("deg");

            if(wind.getDouble("gust")!=null)
                wind_gust = wind.getDouble("gust");
        }

        JsonObject clouds=job.getJsonObject("clouds");
        if(clouds!=null)
            clouds_all = clouds.getDouble("all");

        JsonObject rain=job.getJsonObject("rain");
        if(rain!=null) {
            rain_1h = rain.getDouble("1h");
            rain_3h = rain.getDouble("3h");
        }

        JsonObject snow=job.getJsonObject("snow");
        if(snow!=null) {
            snow_1h = snow.getDouble("1h");
            snow_3h = snow.getDouble("3h");
        }



        JsonObject sys=job.getJsonObject("sys");
        if(sys!=null) {
            sunrise = sys.getInteger("sunrise");
            sunset = sys.getInteger("sunset");
        }

        timezone=job.getInteger("timezone");

        dt =job.getInteger("dt");
    }
    public void set(String s,double value)
    {
    switch(s)
    {
        case "lat":
             lat=value;
        case "lon":
             lon=value;
        case "weather_id":
             weather_id=value;
        case "temp":
             temp=value;
        case "feel_like":
             feel_like=value;
        case "temp_min":
             temp_min=value;
        case "temp_max":
             temp_max=value;
        case "sea_level":
             sea_level=value;
        case "grnd_level":
             grnd_level=value;
        case "humidity":
             humidity=value;
        case "pressure":
             pressure=value;
        case "visibility":
             visibility=value;
        case "wind_speed":
             wind_speed=value;
        case "wind_deg":
             wind_deg=value;
        case "wind_gust":
             wind_gust=value;
        case "clouds_all":
             clouds_all=value;
        case "rain_1h":
             rain_1h=value;
        case "rain_3h":
             rain_3h=value;
        case "snow_1h":
             snow_1h=value;
        case "snow_3h":
             snow_3h=value;
    }
    }
    public void set(String s,String value)
    {
        switch(s)
        {
            case "weather_icon":
                 weather_icon=value;
            case "weather_description":
                 weather_description=value;
            case "weather_main":
                 weather_main=value;
            case "base":
                 base=value;

        }
    }
    public void set(String s,int value)
    {
        switch(s)
        {
            case "sunrise":
                 sunrise=value;
            case "sunset":
                 sunset=value;
            case "timezone":
                 timezone=value;
            case "date":
                 dt =value;
        }
    }

    public void copy(Weather_data e)
    {
        lat=e.getLat();
        lon=e.getLon();
        temp=e.getTemp();
        weather_id=e.getWeather_id();
        feel_like=e.getFeel_like();
        temp_max=e.getTemp_max();
        temp_min=e.getTemp_min();
        sea_level=e.getSea_level();
        grnd_level=e.getGrnd_level();
        humidity=e.getHumidity();
        pressure=e.getPressure();
        visibility=e.getVisibility();
        wind_speed=e.getWind_speed();
        wind_deg=e.getWind_deg();
        wind_gust=e.getWind_gust();
        clouds_all=e.getClouds_all();
        snow_1h=e.getSnow_1h();
        snow_3h=e.getSnow_3h();
        rain_1h=e.getRain_1h();
        rain_3h=e.getRain_3h();
        sunrise=e.getSunrise();
        sunset=e.getSunset();
        timezone=e.getTimezone();
        dt =e.getDt();
        weather_id=e.getWeather_id();
        weather_icon=e.getWeather_icon();
        weather_main=e.getWeather_main();
        base=e.getBase();

    }
    public double getD(String s)
    {
        switch(s)
        {
            case "lat":
              return lat;
            case "lon":
                return lon;
            case "weather_id":
                return weather_id;
            case "temp":
                return temp;
            case "feel_like":
                return feel_like;
            case "temp_min":
                return temp_min;
            case "temp_max":
                return temp_max;
            case "sea_level":
                return sea_level;
            case "grnd_level":
                return grnd_level;
            case "humidity":
                return humidity;
            case "pressure":
                return pressure;
            case "visibility":
                return visibility;
            case "wind_speed":
                return wind_speed;
            case "wind_deg":
                return wind_deg;
            case "wind_gust":
                return wind_gust;
            case "clouds_all":
                return clouds_all;
            case "rain_1h":
                return rain_1h;
            case "rain_3h":
                return rain_3h;
            case "snow_1h":
                return snow_1h;
            case "snow_3h":
                return snow_3h;
        }
        return -1000;
    }
    public String getS(String s)
    {
        switch(s)
        {
            case "weather_icon":
                return weather_icon;
            case "weather_description":
                return weather_description;
            case "weather_main":
                return weather_main;
            case "base":
                return base;

        }
        return null;
    }
    public int getI(String s)
    {
        switch(s)
        {
            case "sunrise":
                return  sunrise;
            case "sunset":
                return sunset;
            case "timezone":
                return timezone;
            case "date":
                return dt;
        }
        return -1000;
    }
    public String get(String s)
    {
        if(getS(s)!=null)
            return getS(s);
        else if(getD(s)!=-1000)
            return getD(s)+"";
        else
            return getI(s)+"";

    }


}
