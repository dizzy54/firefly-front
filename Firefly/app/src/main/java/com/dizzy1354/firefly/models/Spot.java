package com.dizzy1354.firefly.models;

/**
 * Created by akshat on 21/12/15.
 */
public class Spot {
    private String user;
    private Long spot_timestamp;
    private float lat;
    private float lng;
    private String namespaceId;
    private String instanceId;

    public Spot(String user, Long spot_timestamp, float lat, float lng, String namespaceId, String instanceId) {
        this.user = user;
        this.spot_timestamp = spot_timestamp;
        this.lat = lat;
        this.lng = lng;
        this.namespaceId = namespaceId;
        this.instanceId = instanceId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Long getSpot_timestamp() {
        return spot_timestamp;
    }

    public void setSpot_timestamp(Long spot_timestamp) {
        this.spot_timestamp = spot_timestamp;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLng() {
        return lng;
    }

    public void setLng(float lng) {
        this.lng = lng;
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
}
