package com.example.terryooops.tmap;


public class EvChargerItem {
    private String statId; //충전소ID
    private String statNm; //충전소명
    private String chgerId; //충전기ID
    private int chgerType; //충전기타입
    private int stat; //충전기상태
    private double lat; //위도
    private double lng; //경도
    private String addr; //주소
    private String userTime; //충전기 이용시간


    public void setStatId(String statId) {
        this.statId = statId;
    }

    public void setStatNm(String statNm) {
        this.statNm = statNm;
    }

    public void setChgerId(String chgerId) {
        this.chgerId = chgerId;
    }

    public void setChgerType(int chgerType) {
        this.chgerType = chgerType;
    }

    public void setStat(int stat) { this.stat = stat; }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public void setUserTime(String userTime) {
        this.userTime = userTime;
    }

    public String getStatId() { return statId; }

    public String getStatNm() { return statNm;  }

    public String getChgerId() {
        return chgerId;
    }

    public int getChgerType() { return chgerType; }

    public int getStat() {
        return stat;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getAddr() {
        return addr;
    }

    public String getUserTime() {
        return userTime;
    }

    @Override
    public String toString() {
        return "EvChargerItem{" +
                "statId='" + statId + '\'' +
                ", statNm='" + statNm + '\'' +
                ", chgerId='" + chgerId + '\'' +
                ", chgerType=" + chgerType +
                ", stat=" + stat +
                ", lat=" + lat +
                ", lng=" + lng +
                ", addr='" + addr + '\'' +
                ", userTime='" + userTime + '\'' +
                '}';
    }
}
