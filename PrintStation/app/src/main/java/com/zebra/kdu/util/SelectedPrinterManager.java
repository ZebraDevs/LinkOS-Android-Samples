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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterNetwork;

public class SelectedPrinterManager {
	private static final int MAX_HISTORY_SIZE = 5;
	private static final String PRINTER_ADDRESS_KEY = "PrinterAddress";
	private static final String PRINTER_NAME_KEY = "PrinterName";
	private static final String PRINTER_TYPE_KEY = "PrinterType";
	private static final String PRINTER_TYPE_BLUETOOTH = "Bluetooth";
	private static final String PRINTER_TYPE_NETWORK = "Network";

	private static List<DiscoveredPrinter> selectedPrinterHistory = new ArrayList<DiscoveredPrinter>();

	public static DiscoveredPrinter getSelectedPrinter() {
		if (selectedPrinterHistory.size() > 0) {
			return selectedPrinterHistory.get(0);
		} else {
			return null;
		}
	}

	public static DiscoveredPrinter[] getPrinterHistory() {
		return selectedPrinterHistory.toArray(new DiscoveredPrinter [] {});
	}

	public static void populatePrinterHistory(DiscoveredPrinter [] newHistory) {
		for (int i = newHistory.length; i > 0; i--) {
			setSelectedPrinter(newHistory[i-1]);
		}
	}

	public static void setSelectedPrinter(DiscoveredPrinter printer) {
		for(int i = 0; i < selectedPrinterHistory.size(); i++) {
			if (selectedPrinterHistory.get(i).address.equals(printer.address)) {
				selectedPrinterHistory.remove(i);
				break;
			}
		}
		selectedPrinterHistory.add(0, printer);
		if (selectedPrinterHistory.size() > MAX_HISTORY_SIZE) {
			selectedPrinterHistory.remove(selectedPrinterHistory.size() - 1);
		}
	}

	public static Connection getPrinterConnection() {
		return SelectedPrinterManager.getSelectedPrinter().getConnection();
	}

	public static void removeHistoryItemAtIndex(int index) {
		int historySize = selectedPrinterHistory.size(); 
		if (historySize > 0 && index < historySize) {
			selectedPrinterHistory.remove(index);
		}
	}

	public static void storePrinterHistoryInPreferences(Context appContext) {
		Editor editor =  PreferenceManager.getDefaultSharedPreferences(appContext).edit();
		int storageIndex = 0;
		for (DiscoveredPrinter thisPrinter : selectedPrinterHistory) {
			if (thisPrinter instanceof DiscoveredPrinterNetwork) {
				DiscoveredPrinterNetwork thisPrinterNetwork = (DiscoveredPrinterNetwork)thisPrinter;
				String addressToStore = thisPrinterNetwork.address + ":" + thisPrinterNetwork.getDiscoveryDataMap().get("PORT_NUMBER");
				editor.putString(PRINTER_ADDRESS_KEY + storageIndex, addressToStore);
				editor.putString(PRINTER_NAME_KEY + storageIndex, thisPrinterNetwork.getDiscoveryDataMap().get("DNS_NAME"));
				editor.putString(PRINTER_TYPE_KEY + storageIndex, PRINTER_TYPE_NETWORK);
				storageIndex++;
			} else if (thisPrinter instanceof DiscoveredPrinterBluetooth) {
				DiscoveredPrinterBluetooth thisPrinterBluetooth = (DiscoveredPrinterBluetooth)thisPrinter;
				editor.putString(PRINTER_ADDRESS_KEY + storageIndex, thisPrinterBluetooth.address);
				editor.putString(PRINTER_NAME_KEY + storageIndex, thisPrinterBluetooth.friendlyName);
				editor.putString(PRINTER_TYPE_KEY + storageIndex, PRINTER_TYPE_BLUETOOTH);
				storageIndex++;
			}
		}
		editor.commit();
	}

	public static void populatePrinterHistoryFromPreferences(Context appContext) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

		List<DiscoveredPrinter> printers = new ArrayList<DiscoveredPrinter>();
		for (int i = 0; preferences.contains(PRINTER_ADDRESS_KEY + i); i++) {
			if (preferences.getString(PRINTER_TYPE_KEY + i, PRINTER_TYPE_BLUETOOTH).equals(PRINTER_TYPE_NETWORK)) {
				String[] addressAndPort = preferences.getString(PRINTER_ADDRESS_KEY + i, "").split(":");
				DiscoveredPrinterNetwork printer = new DiscoveredPrinterNetwork(addressAndPort[0], Integer.parseInt(addressAndPort[1]));
				printer.getDiscoveryDataMap().put("DNS_NAME", preferences.getString(PRINTER_NAME_KEY + i, ""));
				printers.add(printer);
			} else if (preferences.getString(PRINTER_TYPE_KEY + i, PRINTER_TYPE_NETWORK).equals(PRINTER_TYPE_BLUETOOTH)){
				String address = preferences.getString(PRINTER_ADDRESS_KEY + i, "");
				String name = preferences.getString(PRINTER_NAME_KEY + i, "");
				printers.add(new DiscoveredPrinterBluetooth(address, name));
			}

		}
		populatePrinterHistory(printers.toArray(new DiscoveredPrinter [] {}));
	}
}
