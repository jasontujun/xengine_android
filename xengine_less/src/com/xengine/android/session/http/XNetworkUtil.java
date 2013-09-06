package com.xengine.android.session.http;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;

/**
 * Created by 赵之韵.
 * Date: 12-2-29
 * Time: 下午6:56
 */
public class XNetworkUtil {

    /**
     * 当前网络是否可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }

    /**
     * Wifi是否连接
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        WifiManager wifiManager = (WifiManager) context.
                getSystemService(Context.WIFI_SERVICE);
        switch (wifiManager.getWifiState()) {
            case WifiManager.WIFI_STATE_ENABLED:
                return true;
            case WifiManager.WIFI_STATE_ENABLING:
                return true;
            default:
                return false;
        }
    }

    /**
     * 进入网络设置页面
     */
    public static void gotoNetworkSetting(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // 没有发现位置设置
            intent.setAction(Settings.ACTION_SETTINGS);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
            }
        }
    }
}
