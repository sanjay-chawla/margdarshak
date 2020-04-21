package com.margdarshak.ui.data.model;
import com.margdarshak.ui.data.model.BikesData.Position;
public class BikesDataBuilder {
    private int number;
    private String contractName;
    private String name;
    private String address;
    private Position position;
    private boolean banking;
    private boolean bonus;
    private int bikeStands;
    private int availableStands;
    private int availableBikes;
    private String status;
    private long lastUpdate;

    public BikesDataBuilder setNumber(int number) {
        this.number = number;
        return this;
    }

    public BikesDataBuilder setContractName(String contractName) {
        this.contractName = contractName;
        return this;
    }

    public BikesDataBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public BikesDataBuilder setAddress(String address) {
        this.address = address;
        return this;
    }

    public BikesDataBuilder setPosition(double lat, double lng) {
        this.position = new BikesData().new Position(lat, lng);
        return this;
    }

    public BikesDataBuilder setBanking(boolean banking) {
        this.banking = banking;
        return this;
    }

    public BikesDataBuilder setBonus(boolean bonus) {
        this.bonus = bonus;
        return this;
    }

    public BikesDataBuilder setBikeStands(int bikeStands) {
        this.bikeStands = bikeStands;
        return this;
    }

    public BikesDataBuilder setAvailableStands(int availableStands) {
        this.availableStands = availableStands;
        return this;
    }

    public BikesDataBuilder setAvailableBikes(int availableBikes) {
        this.availableBikes = availableBikes;
        return this;
    }

    public BikesDataBuilder setStatus(String status) {
        this.status = status;
        return this;
    }

    public BikesDataBuilder setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    public BikesData createBikesData() {
        return new BikesData(number, contractName, name, address, position, banking, bonus, bikeStands, availableStands, availableBikes, status, lastUpdate);
    }
}