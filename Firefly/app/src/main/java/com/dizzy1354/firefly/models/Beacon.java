package com.dizzy1354.firefly.models;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.dizzy1354.firefly.Constants;
import com.dizzy1354.firefly.MainActivity;
import com.dizzy1354.firefly.helpers.MySingleton;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by akshat on 11/12/15.
 */
public class Beacon {
    private static final String TAG = "Beacon";
    private static final String BULLET = "● ";
    private static final String spotUrl = Constants.SERVER_URL+"spots/0/spot_with_id/";
    public final String deviceAddress;
    public int rssi;
    // TODO: rename to make explicit the validation intent of this timestamp. We use it to
    // remember a recent frame to make sure that non-monotonic TLM values increase.
    public long timestamp = System.currentTimeMillis();

    // Used to remove devices from the listview when they haven't been seen in a while.
    public long lastSeenTimestamp = System.currentTimeMillis()/1000;

    public byte[] uidServiceData;
    byte[] tlmServiceData;
    byte[] urlServiceData;

    public class UidStatus {
        public String uidValue;
        //int txPower;

        public String errTx;
        public String errUid;
        public String errRfu;

        public String getErrors() {
            StringBuilder sb = new StringBuilder();
            if (errTx != null) {
                sb.append(BULLET).append(errTx).append("\n");
            }
            if (errUid != null) {
                sb.append(BULLET).append(errUid).append("\n");
            }
            if (errRfu != null) {
                sb.append(BULLET).append(errRfu).append("\n");
            }
            return sb.toString().trim();
        }
    }

    public class TlmStatus {
        String version;
        String voltage;
        String temp;
        String advCnt;
        String secCnt;

        String errIdentialFrame;
        String errVersion;
        String errVoltage;
        String errTemp;
        String errPduCnt;
        String errSecCnt;
        String errRfu;

        public String getErrors() {
            StringBuilder sb = new StringBuilder();
            if (errIdentialFrame != null) {
                sb.append(BULLET).append(errIdentialFrame).append("\n");
            }
            if (errVersion != null) {
                sb.append(BULLET).append(errVersion).append("\n");
            }
            if (errVoltage != null) {
                sb.append(BULLET).append(errVoltage).append("\n");
            }
            if (errTemp != null) {
                sb.append(BULLET).append(errTemp).append("\n");
            }
            if (errPduCnt != null) {
                sb.append(BULLET).append(errPduCnt).append("\n");
            }
            if (errSecCnt != null) {
                sb.append(BULLET).append(errSecCnt).append("\n");
            }
            if (errRfu != null) {
                sb.append(BULLET).append(errRfu).append("\n");
            }
            return sb.toString().trim();
        }

        @Override
        public String toString() {
            return getErrors();
        }
    }

    public class UrlStatus {
        String urlValue;
        String urlNotSet;
        String txPower;

        public String getErrors() {
            StringBuilder sb = new StringBuilder();
            if (txPower != null) {
                sb.append(BULLET).append(txPower).append("\n");
            }
            if (urlNotSet != null) {
                sb.append(BULLET).append(urlNotSet).append("\n");
            }
            return sb.toString().trim();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (urlValue != null) {
                sb.append(urlValue).append("\n");
            }
            return sb.append(getErrors()).toString().trim();
        }
    }

    public class FrameStatus {
        public String nullServiceData;
        public String tooShortServiceData;
        public String invalidFrameType;

        public String getErrors() {
            StringBuilder sb = new StringBuilder();
            if (nullServiceData != null) {
                sb.append(BULLET).append(nullServiceData).append("\n");
            }
            if (tooShortServiceData != null) {
                sb.append(BULLET).append(tooShortServiceData).append("\n");
            }
            if (invalidFrameType != null) {
                sb.append(BULLET).append(invalidFrameType).append("\n");
            }
            return sb.toString().trim();
        }

        @Override
        public String toString() {
            return getErrors();
        }
    }

    public boolean hasUidFrame;
    public UidStatus uidStatus = new UidStatus();

    public boolean hasTlmFrame;
    public TlmStatus tlmStatus = new TlmStatus();

    public boolean hasUrlFrame;
    public UrlStatus urlStatus = new UrlStatus();

    public FrameStatus frameStatus = new FrameStatus();

    public Beacon(String deviceAddress, int rssi) {
        this.deviceAddress = deviceAddress;
        this.rssi = rssi;
    }

    /**
     * Performs a case-insensitive contains test of s on the device address (with or without the
     * colon separators) and/or the UID value, and/or the URL value.
     */
    public boolean contains(String s) {
        return s == null
                || s.isEmpty()
                || deviceAddress.replace(":", "").toLowerCase().contains(s.toLowerCase())
                || (uidStatus.uidValue != null
                && uidStatus.uidValue.toLowerCase().contains(s.toLowerCase()))
                || (urlStatus.urlValue != null
                && urlStatus.urlValue.toLowerCase().contains(s.toLowerCase()));
    }

    private class Spot {
        private double lat;
        private double lng;
        private String user;
        private long spotTimestamp;
        private String instanceId;
        private String namespaceId;

        public Spot(double lat,
                    double lng,
                    String user,
                    long spotTimestamp,
                    String namespaceId,
                    String instanceId) {
            this.lat = lat;
            this.lng = lng;
            this.user = user;
            this.spotTimestamp = spotTimestamp;
            this.instanceId = instanceId;
            this.namespaceId = namespaceId;
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

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public long getSpotTimestamp() {
            return spotTimestamp;
        }

        public void setSpotTimestamp(long spotinTimestamp) {
            this.spotTimestamp = spotinTimestamp;
        }

        public String getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(String instanceId) {
            this.instanceId = instanceId;
        }

        public String getNamespaceId() {
            return namespaceId;
        }

        public void setNamespaceId(String namespaceId) {
            this.namespaceId = namespaceId;
        }
    }

    public void postSpot(final Context context, double lat, double lng, String user) {
        final long curr_timestamp = System.currentTimeMillis()/1000;
        if (curr_timestamp - getLastSpottedTimestamp() < Constants.MIN_SPOT_INTERVAL) {
            return;
        } else {
            final Spot spot = new Spot(lat, lng, user, curr_timestamp, this.getNamespaceId(), this.getInstanceId());
            Log.d(TAG, "postSpot: Namespace Id = "+this.getNamespaceId()+", Instance Id = "+this.getInstanceId());
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray().put(new JSONObject(mapper.writeValueAsString(spot)));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, spotUrl, jsonArray,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            // Do something with the response
                            /*
                            String stringResponse = null;
                            try {
                                stringResponse = response.getJSONObject(0).get("message").toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            */
                            Log.d(TAG, "" + response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError e) {
                            Log.e(TAG, "onResponse()", e);
                            // Handle error
                        }
                    });
            // request.setRetryPolicy(new DefaultRetryPolicy(5000, 2, 1.5f));
            Log.d(TAG, "postSpot: request = "+request.toString());
            Log.d(TAG, "postSpot: request.body = "+new String(request.getBody()));
            MySingleton.getInstance(context).addToRequestQueue(request);
        }
    }

    public long getLastSpottedTimestamp() {
        if (MainActivity.mSpotRecord == null) {
            timestamp = 0;
        }
        else if (MainActivity.mSpotRecord.get(deviceAddress) == null) {
            timestamp = 0;
        }
        else {
            timestamp = MainActivity.mSpotRecord.get(deviceAddress);
        }
        return timestamp;
    }

    public String getNamespaceId() {
        if (this.uidStatus.uidValue == null) {
            return "invalid";
        }
        if (this.uidStatus.uidValue.length()==32) {
            return "0x" + this.uidStatus.uidValue.substring(0, 20);
        }
        else {
            return "length is "+Integer.toString(this.uidStatus.uidValue.length());
        }
    }

    public String getInstanceId() {
        if (this.uidStatus.uidValue == null) {
            return "invalid";
        }
        if (this.uidStatus.uidValue.length()==32) {
            return "0x" + this.uidStatus.uidValue.substring(20);
        }
        else {
            return "invalid";
        }
    }
}
