package org.example;

public class coordinates
{
    double lat,lon,range;

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    coordinates(double lat, double lon, double range)
    {
        this.lat=lat;
        this.lon=lon;
        this.range=range;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
