package com.dizzy1354.firefly.models;

import com.dizzy1354.firefly.helpers.Utils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by akshat on 20/12/15.
 */
public class Vehicle {
    private int serialNumber;
    private BeaconInVehicle beacon;

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public BeaconInVehicle getBeacon() {
        return beacon;
    }

    public void setBeacon(BeaconInVehicle beacon) {
        this.beacon = beacon;
    }

    @JsonCreator
    public Vehicle(
            @JsonProperty("serial_number") int serialNumber,
            @JsonProperty("beacon") BeaconInVehicle beacon) {
        this.serialNumber = serialNumber;
        this.beacon = beacon;
    }

    public double getLat() {
        return beacon.getLat();
    }

    public double getLng() {
        return beacon.getLng();
    }

    public String getLastSeenAsString() {

        return Utils.getDisplayTimefromTimestampInSeconds(beacon.getLastSeenTimestamp());
    }
}
