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

package com.zebra.android.devdemo.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.zebra.android.devdemo.ConnectionScreen;
import com.zebra.android.devdemo.R;
import com.zebra.android.devdemo.util.DemoSleeper;
import com.zebra.android.devdemo.util.SettingsHelper;
import com.zebra.android.devdemo.util.UIHelper;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.PrinterStatusMessages;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

public class PrinterStatusScreen extends Activity {

    private boolean bluetoothSelected;
    private String macAddress;
    private String tcpAddress;
    private String tcpPort;
    private Connection Connection;
    private ZebraPrinter printer;
    private ArrayAdapter<String> statusListAdapter;
    private List<String> statusMessageList = new ArrayList<String>();
    private UIHelper helper = new UIHelper(this);
    private boolean activityIsActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityIsActive = true;
        setContentView(R.layout.print_status_activity);
        Bundle b = getIntent().getExtras();
        bluetoothSelected = b.getBoolean("bluetooth selected");
        macAddress = b.getString("mac address");
        tcpAddress = b.getString("tcp address");
        tcpPort = b.getString("tcp port");
        statusListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, statusMessageList);
        ListView statusList = (ListView) this.findViewById(R.id.statusList);
        statusList.setAdapter(statusListAdapter);
        new Thread(new Runnable() {

            public void run() {
                saveSettings();
                Looper.prepare();
                pollForStatus();
                Looper.myLooper().quit();
            }
        }).start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        activityIsActive = false;
        if (Connection != null && Connection.isConnected()) {
            disconnect();
        }
        if (helper.isDialogActive()) {
            helper.dismissLoadingDialog();
        }
    }

    public ZebraPrinter connect() {
        helper.showLoadingDialog("Updating Status...");

        if (bluetoothSelected) {
            pairBT();
        } else {
            connectToTcp();
        }

        ZebraPrinter printer = null;

        if (Connection != null && Connection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(Connection);
                printer.getPrinterControlLanguage();
            } catch (ConnectionException e) {
                displayConnectionError(e.getMessage());
                printer = null;
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                displayConnectionError(e.getMessage());
                printer = null;
                disconnect();
            }
        }
        helper.dismissLoadingDialog();
        return printer;
    }

    public void disconnect() {
        try {
            if (Connection != null) {
                Connection.close();
            }
        } catch (ConnectionException e) {
        }
    }

    private void pairBT() {
        try {
            Connection = new BluetoothConnection(macAddress);
            Connection.open();
            setStoredString(ConnectionScreen.bluetoothAddressKey, macAddress);
        } catch (ConnectionException e) {
            displayConnectionError(e.getMessage());
            disconnect();
        }
    }

    private void connectToTcp() {
        if (Connection != null && Connection.isConnected() == true) {
            return;
        }
        tryTcpConnect();
    }

    private void tryTcpConnect() {
        try {
            Connection = new TcpConnection(tcpAddress, Integer.parseInt(tcpPort));
            Connection.open();
            setStoredString(ConnectionScreen.tcpAddressKey, tcpAddress);
            setStoredString(ConnectionScreen.tcpPortKey, tcpPort);
        } catch (ConnectionException e) {
            displayConnectionError(e.getMessage());
            disconnect();
        } catch (NumberFormatException e) {
            displayConnectionError("Invalid port number");
        }
    }

    private void displayConnectionError(final String message) {
        if (activityIsActive == true) {
            runOnUiThread(new Runnable() {
                public void run() {
                    new AlertDialog.Builder(PrinterStatusScreen.this).setMessage(message).setTitle("Error").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create().show();
                }
            });
        }
    }

    private void pollForStatus() {
        printer = connect();
        while (Connection != null && Connection.isConnected()) {
            try {
                updatePrinterStatus();
            } catch (ConnectionException e) {
                displayConnectionError(e.getMessage());
                e.printStackTrace();
            }
            DemoSleeper.sleep(3000);
        }
    }

    private void updatePrinterStatus() throws ConnectionException {
        if (Connection != null && Connection.isConnected()) {

            ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);

            PrinterStatus printerStatus = (linkOsPrinter != null) ? linkOsPrinter.getCurrentStatus() : printer.getCurrentStatus();

            final String[] printerStatusString = new PrinterStatusMessages(printerStatus).getStatusMessage();
            final String[] printerStatusPrefix = getPrinterStatusPrefix(printerStatus);
            runOnUiThread(new Runnable() {
                public void run() {
                    statusListAdapter.clear();
                    statusMessageList.clear();
                    statusMessageList.addAll(Arrays.asList(printerStatusPrefix));
                    statusMessageList.addAll(Arrays.asList(printerStatusString));
                    statusListAdapter.notifyDataSetChanged();
                }
            });
        } else {
            displayConnectionError("No printer connection");
        }

    }

    private void setStoredString(String key, String value) {
        SharedPreferences settings = getSharedPreferences(ConnectionScreen.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private String[] getPrinterStatusPrefix(PrinterStatus printerStatus) {
        boolean ready = printerStatus != null ? printerStatus.isReadyToPrint : false;
        String readyString = "Printer " + (ready ? "ready" : "not ready");
        String labelsInBatch = "Labels in batch: " + String.valueOf(printerStatus.labelsRemainingInBatch);
        String labelsInRecvBuffer = "Labels in buffer: " + String.valueOf(printerStatus.numberOfFormatsInReceiveBuffer);
        return new String[] { readyString, labelsInBatch, labelsInRecvBuffer };
    }

    private void saveSettings() {
        SettingsHelper.saveBluetoothAddress(PrinterStatusScreen.this, macAddress);
        SettingsHelper.saveIp(PrinterStatusScreen.this, tcpAddress);
        SettingsHelper.savePort(PrinterStatusScreen.this, tcpPort);
    }

}
