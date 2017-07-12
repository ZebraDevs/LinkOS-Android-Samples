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
package com.zebra.kdu.util;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterNetwork;

public class ApplicationPreferences {
	
	public static final String APP_PREFERENCES_KEY = "PrintStationPreferences";
	public static final String PRINTER_NAME_KEY = "PrinterName";
	public static final String PRINTER_ADDRESS_KEY = "PrinterAddress";
	public static final String PRINTER_ISBLUETOOTH = "PrinterIsBluetooth";
	
	
	public static DiscoveredPrinter getSavedPrinter(Context c) {
		SharedPreferences preferences = c.getSharedPreferences(APP_PREFERENCES_KEY, 0);
		
		
		String printerName = preferences.getString(PRINTER_NAME_KEY, null);
		String printerAddress = preferences.getString(PRINTER_ADDRESS_KEY, null);
		boolean printerIsBluetooth = preferences.getBoolean(PRINTER_ISBLUETOOTH, false);
		
		if (printerName != null && printerAddress != null) {
			if (printerIsBluetooth) {
				return new DiscoveredPrinterBluetooth(printerAddress, printerName);
			} else {
				HashMap<String, String> discoveryMap = new HashMap<String, String>();
				discoveryMap.put("DNS_NAME", printerName);
				String[] addressParts = printerAddress.split(":");
				discoveryMap.put("ADDRESS", addressParts[0]);
				discoveryMap.put("PORT_NUMBER", addressParts[1]);
				return new DiscoveredPrinterNetwork(discoveryMap);
			}
		}
		
		return null;
	}
	
	public static boolean savePrinter(Context c, DiscoveredPrinter printer) {
		Editor editor = c.getSharedPreferences(APP_PREFERENCES_KEY, 0).edit();
		
		if (printer instanceof DiscoveredPrinterBluetooth) {
			DiscoveredPrinterBluetooth btPrinter = (DiscoveredPrinterBluetooth)printer;
			editor.putString(PRINTER_NAME_KEY, btPrinter.friendlyName);
			editor.putString(PRINTER_ADDRESS_KEY, printer.address);
			editor.putBoolean(PRINTER_ISBLUETOOTH, true);
		} else {
			DiscoveredPrinterNetwork ipPrinter = (DiscoveredPrinterNetwork)printer;
			editor.putString(PRINTER_NAME_KEY, ipPrinter.getDiscoveryDataMap().get("DNS_NAME"));
			editor.putString(PRINTER_ADDRESS_KEY, ipPrinter.address + ":" + ipPrinter.getDiscoveryDataMap().get("PORT_NUMBER"));
			editor.putBoolean(PRINTER_ISBLUETOOTH, false);
		}
		
		return editor.commit();
	}

}
