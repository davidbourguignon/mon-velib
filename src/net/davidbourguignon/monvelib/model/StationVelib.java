package net.davidbourguignon.monvelib.model;

import java.io.Serializable;

public class StationVelib {

    private String name;
    private int number;
    private String address;
    private String fullAddress;
    private Double latitude;
    private Double longitude;
    private Boolean open;
    private Boolean bonus;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public String getAddress() {
        return address;
    }
    public String getFullAddress() {
        return fullAddress;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }
    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public Boolean getOpen() {
        return open;
    }
    public void setOpen(Boolean open) {
        this.open = open;
    }
    public Boolean getBonus() {
        return bonus;
    }
    public void setBonus(Boolean bonus) {
        this.bonus = bonus;
    }
    public String toString(){
      return getName();
    }
}


