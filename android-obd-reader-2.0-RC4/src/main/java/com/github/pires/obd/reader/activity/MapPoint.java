package com.github.pires.obd.reader.activity;

import com.skp.Tmap.TMapData;

/**
 * Created by TerryOoops on 2016-11-02.
 */

public class MapPoint {
    private String Name;
    private double latitude;
    private double longitude;
    private String usetime;
    private String addr;
    private int chargerType;
    private int stat;
    private double distance;
    private double price;
    TMapData path;

    public MapPoint(){
        super();
    }

    public MapPoint(String Name, double latitude, double longtitude, String usetime, String addr, double distance, int chargerType, int stat, double price){
        this.Name = Name;
        this.latitude = latitude;
        this.longitude = longtitude;
        this.usetime = usetime;
        this.addr = addr;
        this.distance = distance;
        this.chargerType = chargerType;
        this.stat = stat;
        this.price = price;
    }

    public String getName(){
        return Name;
    }

    public void setName(String Name){
        this.Name = Name;
    }

    public double getLatitude(){
        return latitude;
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public String getUsetime(){
        return usetime;
    }

    public void setUsetime(String usetime){
        this.usetime = usetime;
    }

    public String getAddr(){
        return addr;
    }

    public void setAddr(String addr){
        this.addr = addr;
    }

    public void setChargerType(int chargerType)
    {
        this.chargerType = chargerType;
    }

    public int getChargerType() {
        return this.chargerType ;
    }

    public void setStat(int stat)
    {
        this.stat = stat;
    }

    public int getStat() {
        return this.stat ;
    }

    public double getDistance(){return distance;}

    public void setDistance(double distance){this.distance = distance;}

    public double getPrice(){return price;}

    public void setPrice(double price){this.price = price;}

    public TMapData getPath(){return path;}

    public void setPath(TMapData path){this.path = path;}
}
