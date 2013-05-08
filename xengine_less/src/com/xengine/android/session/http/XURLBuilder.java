package com.xengine.android.session.http;

import java.util.HashMap;
import java.util.Map;

public class XURLBuilder {
    private HashMap<String, String> query = new HashMap<String, String>();
    private String api;
    private static String host;

    public static XURLBuilder createInstance(String api) {
        return new XURLBuilder(api);
    }

    public static void setDefaultHost(String host) {
        XURLBuilder.host = host;
    }
    
    private XURLBuilder(String api) {
        this.api = api;
    }

    public XURLBuilder addStringQueryParam(String key, String value) {
        query.put(key, value);
        return this;
    }

    public XURLBuilder addDoubleQueryParam(String key, double value) {
        query.put(key, String.valueOf(value));
        return this;
    }

    public XURLBuilder addIntQueryParam(String key, int value) {
        query.put(key, String.valueOf(value));
        return this;
    }

    public XURLBuilder addLongQueryParam(String key, Long value) {
        query.put(key, String.valueOf(value));
        return this;
    }

    public String build() {
        StringBuilder queryString = new StringBuilder();
        for(Map.Entry<String, String> entry: query.entrySet()) {
            queryString.append(entry.getKey())
                       .append("=")
                       .append(entry.getValue())
                       .append("&");
        }
        if(!query.isEmpty())
            queryString.deleteCharAt(queryString.length()-1);
        return host + api + "?" + queryString.toString();
    }

}
