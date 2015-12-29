package com.dizzy1354.firefly.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by akshat on 21/12/15.
 */
public class BeaconInVehicle {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
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

    public long getLastSeenTimestamp() {
        return lastSeenTimestamp;
    }

    public void setLastSeenTimestamp(long lastSeenTimestamp) {
        this.lastSeenTimestamp = lastSeenTimestamp;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setIsLive(boolean isLive) {
        this.isLive = isLive;
    }

    private int id;
    private String namespaceId;
    private String instanceId;
    private double lat;
    private double lng;
    private long lastSeenTimestamp;
    private boolean isLive;

    @JsonCreator
    public BeaconInVehicle(
            @JsonProperty("id") int id,
            @JsonProperty("namespace_id") String namespaceId,
            @JsonProperty("instance_id") String instanceId,
            @JsonProperty("lat") double lat,
            @JsonProperty("lng") double lng,
            @JsonProperty("last_seen_timestamp") long lastSeenTimestamp,
            @JsonProperty("is_live") boolean isLive) {
        this.id = id;
        this.namespaceId = namespaceId;
        this.instanceId = instanceId;
        this.lat = lat;
        this.lng = lng;
        this.lastSeenTimestamp = lastSeenTimestamp;
        this.isLive = isLive;
    }
}
