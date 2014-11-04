package com.xengine.android.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;

/**
 * <pre>
 * APN接入点的辅助类。
 * APN(Access Point Name)接入点，是指一种网络接入技术，是通过手机上网时必须配置的一个参数。
 * 注意：只有当前的APN为WAP时，网络通信才需要设置对应的代理；非WAP时不需要设置代理。
 * User: jasontujun
 * Date: 14-11-4
 * Time: 上午11:48
 * </pre>
 */
public class XApnUtil {

    public static final String CM_UNI_PROXY = "10.0.0.172";// 移动、联通的WAP代理服务器是10.0.0.172
    public static final String CT_PROXY = "10.0.0.200";// 电信的WAP代理服务器是10.0.0.200
    public static final int CM_UNI_CT_PORT = 80;// 移动、联通、电信的端口都是80

    // 获取当前APN的查询数据库的Uri
    private static Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

    public enum Apn {
        CMWAP,      // 移动WAP
        CMNET,      // 移动NET
        UNIWAP,     // 联通WAP
        UNINET,     // 联通NET
        _3GWAP,     // 联通3GWAP
        _3GNET,     // 联通3GNET
        CTWAP,      // 电信WAP
        CTNET,      // 电信NET
        INTERNET,   // 英特网连接(wifi等)
        UNKNOW      // 未知
    }

    /**
     * 获取当前的APN。
     * @param context
     * @return 返回当前的APN
     */
    public static Apn[] getCurrentApn(Context context) {
        // 如果当前为wifi连接，直接返回Apn.INTERNET，通信时无需设置APN代理
        ConnectivityManager ct = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = ct.getActiveNetworkInfo();
        if (info != null && (info.getType() == ConnectivityManager.TYPE_WIFI))
            return new Apn[] { Apn.INTERNET };

        // 其他情况，查询数据库获取当前的APN代理
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(PREFERRED_APN_URI,
                new String[] { "name", "apn", "proxy", "port" }, null, null, null);
        if (cursor == null)
            return null;
        int count = cursor.getCount();
        if (count <= 0)
            return null;

        Apn[] array = new Apn[count];
        int i = 0;
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String apn = cursor.getString(1);
            String proxy = cursor.getString(2);
            String port = cursor.getString(3);

            XLog.d("abcd", "cursor name:" + name + ",apn:" + apn + ",proxy:" + proxy + ",port:" + port);
            if (apn.toUpperCase().contains("CMWAP")
                    || name.toUpperCase().contains("CMWAP")) {
                array[i] = (!TextUtils.isEmpty(proxy) && !TextUtils.isEmpty(port)) ? Apn.CMWAP : Apn.CMNET;
            } else if (apn.toUpperCase().contains("CMNET")
                    || name.toUpperCase().contains("CMNET")) {
                array[i] = Apn.CMNET;
            } else if (apn.toUpperCase().contains("UNIWAP")
                    || name.toUpperCase().contains("UNIWAP")) {
                array[i] = (!TextUtils.isEmpty(proxy) && !TextUtils.isEmpty(port)) ? Apn.UNIWAP : Apn.UNINET;
            } else if (apn.toUpperCase().contains("UNINET")
                    || name.toUpperCase().contains("UNINET")) {
                array[i] = Apn.UNINET;
            } else if (apn.toUpperCase().contains("3GWAP")
                    || name.toUpperCase().contains("3GWAP")) {
                array[i] = (!TextUtils.isEmpty(proxy) && !TextUtils.isEmpty(port)) ? Apn._3GWAP : Apn._3GNET;
            } else if (apn.toUpperCase().contains("3GNET")
                    || name.toUpperCase().contains("3GNET")) {
                array[i] = Apn._3GNET;
            } else if (apn.toUpperCase().contains("CTWAP")
                    || name.toUpperCase().contains("CTWAP")) {
                array[i] = (!TextUtils.isEmpty(proxy) && !TextUtils.isEmpty(port)) ? Apn.CTWAP : Apn.CTNET;
            } else if (apn.toUpperCase().contains("CTNET")
                    || name.toUpperCase().contains("CTNET")) {
                array[i] = Apn.CTNET;
            } else if ((apn.toUpperCase().contains("INTERNET")
                    || name.toUpperCase().contains("INTERNET"))) {
                array[i] = Apn.INTERNET;
            } else if (name.toUpperCase().contains("T-MOBILE US")
                    || apn.toUpperCase().contains("epc.tmobile.com")) {
                array[i] = Apn.INTERNET;
            } else {
                array[i] = Apn.UNKNOW;
            }
            i++;
        }
        cursor.close();

        return array;
    }
}
