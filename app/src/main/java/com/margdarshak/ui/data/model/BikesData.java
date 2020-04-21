package com.margdarshak.ui.data.model;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class BikesData {

    /**
     * {
     *    "number":42,
     *    "contract_name":"dublin",
     *    "name":"SMITHFIELD NORTH",
     *    "address":"Smithfield North",
     *    "position":{
     *       "lat":53.349562,
     *       "lng":-6.278198
     *    },
     *    "banking":true,
     *    "bonus":false,
     *    "bike_stands":30,
     *    "available_bike_stands":13,
     *    "available_bikes":16,
     *    "status":"OPEN",
     *    "last_update":1585815900000
     * }
     * */
    int number;
    String contract_name;
    String name;
    String address;
    Position position;
    boolean banking;
    boolean bonus;
    int bike_stands;
    int available_bike_stands;
    int available_bikes;
    String status;
    long lastUpdate;
    boolean isFull;
    boolean isEmpty;

    public class Position {
        double lat;
        double lng;

        Position(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }
    }

    public BikesData() {
    }

    public BikesData(int number, String contract_name, String name, String address, Position position, boolean banking, boolean bonus, int bike_stands, int available_bike_stands, int available_bikes, String status, long lastUpdate) {
        this.number = number;
        this.contract_name = contract_name;
        this.name = name;
        this.address = address;
        this.position = position;
        this.banking = banking;
        this.bonus = bonus;
        this.bike_stands = bike_stands;
        this.available_bike_stands = available_bike_stands;
        this.available_bikes = available_bikes;
        this.status = status;
        this.lastUpdate = lastUpdate;

        this.isEmpty = available_bikes == 0?true:false;
        this.isFull = available_bike_stands == 0?true:false;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getContract_name() {
        return contract_name;
    }

    public void setContract_name(String contract_name) {
        this.contract_name = contract_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean isBanking() {
        return banking;
    }

    public void setBanking(boolean banking) {
        this.banking = banking;
    }

    public boolean isBonus() {
        return bonus;
    }

    public void setBonus(boolean bonus) {
        this.bonus = bonus;
    }

    public int getBike_stands() {
        return bike_stands;
    }

    public void setBike_stands(int bike_stands) {
        this.bike_stands = bike_stands;
    }

    public int getAvailable_bike_stands() {
        return available_bike_stands;
    }

    public void setAvailable_bike_stands(int available_bike_stands) {
        this.available_bike_stands = available_bike_stands;
    }

    public int getAvailable_bikes() {
        return available_bikes;
    }

    public void setAvailable_bikes(int available_bikes) {
        this.available_bikes = available_bikes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isFull() {
        return isFull;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

}
