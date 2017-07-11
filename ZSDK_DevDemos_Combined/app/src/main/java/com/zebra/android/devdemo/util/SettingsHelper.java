/***********************************************
 * CONFIDENTIAL AND PROPRIETARY 
 * 
 * The source code and other information contained herein is the confidential and the exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published, 
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 * 
 * Copyright ZIH Corp. 2012
 * 
 * ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.android.devdemo.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsHelper {

    private static final String PREFS_NAME = "OurSavedAddress";
    private static final String bluetoothAddressKey = "ZEBRA_DEMO_BLUETOOTH_ADDRESS";
    private static final String tcpAddressKey = "ZEBRA_DEMO_TCP_ADDRESS";
    private static final String tcpPortKey = "ZEBRA_DEMO_TCP_PORT";
    private static final String tcpStatusPortKey = "ZEBRA_DEMO_TCP_STATUS_PORT";

    public static String getIp(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(tcpAddressKey, "");
    }

    public static String getPort(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(tcpPortKey, "");
    }

    public static String getStatusPort(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(tcpStatusPortKey, "");
    }

    public static String getBluetoothAddress(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(bluetoothAddressKey, "");
    }

    public static void saveIp(Context context, String ip) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(tcpAddressKey, ip);
        editor.commit();
    }

    public static void savePort(Context context, String port) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(tcpPortKey, port);
        editor.commit();
    }

    public static void saveStatusPort(Context context, String port) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(tcpStatusPortKey, port);
        editor.commit();
    }

    public static void saveBluetoothAddress(Context context, String address) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(bluetoothAddressKey, address);
        editor.commit();
    }
}
