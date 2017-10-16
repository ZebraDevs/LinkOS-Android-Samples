package com.zebra.isv.tapbluetoothwifi;

/**
 * Created by BWai on 8/17/2015.
 */

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SettingsHelper saves WIFI information as a SharedPreferences key value pair that can be
 * exported into other applications. This is used in lieu of server communication, which can be implemented
 * with the TCP/IP connections in the MainActivity.
 */
public class SettingsHelper {

    private static final String PREFS_NAME = "SavedAddresses";
    private static final String bluetoothAddressKey = "ZEBRA_BLUETOOTH_ADDRESS";
    private static final String ipAddressKey = "ZEBRA_IP_ADDRESS";
    private static final String wlanPortKey = "ZEBRA_WLAN_PORT";
    private static final String wlanAddressKey = "ZEBRA_WLAN_ADDRESS";
    private static final String wiredAddressKey = "ZEBRA_WIRED_ADDRESS";

    public static String getIp (Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(ipAddressKey, "");
    }

    public static String getPort (Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(wlanPortKey, "");
    }

    public static String getBluetoothAddressKey (Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(bluetoothAddressKey, "");
    }

    public static void saveIp(Context context, String ip){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(ipAddressKey, ip);
        editor.commit();
    }

    public static void savePort(Context context, String port){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(wlanPortKey, port);
        editor.commit();
    }

    public static void saveBluetoothAddress(Context context, String address){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(bluetoothAddressKey, address);
        editor.commit();
    }

    public static void saveWlanAddress(Context context, String address){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(wlanAddressKey, address);
        editor.commit();
    }

    public static void saveWiredAddress(Context context, String address){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(wiredAddressKey, address);
        editor.commit();
    }

}
