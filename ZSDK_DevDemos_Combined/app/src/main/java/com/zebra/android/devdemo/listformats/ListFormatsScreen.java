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

package com.zebra.android.devdemo.listformats;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.zebra.android.devdemo.R;
import com.zebra.android.devdemo.util.SettingsHelper;
import com.zebra.android.devdemo.util.UIHelper;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.device.ZebraIllegalArgumentException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

public class ListFormatsScreen extends Activity {

    private boolean bluetoothSelected;
    private String macAddress;
    private String tcpAddress;
    private String tcpPort;
    private ArrayAdapter<String> statusListAdapter;
    private List<String> formatsList = new ArrayList<String>();
    private UIHelper helper = new UIHelper(this);
    private boolean retrieveFormats = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.formats_list_activity);
        Bundle b = getIntent().getExtras();
        bluetoothSelected = b.getBoolean("bluetooth selected");
        macAddress = b.getString("mac address");
        tcpAddress = b.getString("tcp address");
        tcpPort = b.getString("tcp port");
        retrieveFormats = b.getBoolean("retrieveFormats");
        statusListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, formatsList);
        ListView statusList = (ListView) this.findViewById(R.id.formatsList);
        statusList.setAdapter(statusListAdapter);

        new Thread(new Runnable() {

            public void run() {
                Looper.prepare();
                getFileList();
                Looper.loop();
                Looper.myLooper().quit();
            }
        }).start();

    }

    private void getFileList() {
        Connection connection = null;
        if (bluetoothSelected == false) {
            try {
                int port = Integer.parseInt(tcpPort);
                connection = new TcpConnection(tcpAddress, port);
            } catch (NumberFormatException e) {
                helper.showErrorDialogOnGuiThread("Port number is invalid");
                return;
            }
        } else {
            connection = new BluetoothConnection(macAddress);
        }
        try {
            helper.showLoadingDialog("Retrieving " + (retrieveFormats ? " Formats" : " Files") + "...");
            connection.open();
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
            PrinterLanguage pl = printer.getPrinterControlLanguage();
            String[] formatExtensions;
            if (pl == PrinterLanguage.ZPL) {
                formatExtensions = new String[] { "ZPL" };
            } else {
                formatExtensions = new String[] { "FMT", "LBL" };
            }

            String[] formats = null;

            if (retrieveFormats) {
                formats = printer.retrieveFileNames(formatExtensions);
            } else {
                formats = printer.retrieveFileNames();
            }
            for (int i = 0; i < formats.length; i++) {
                formatsList.add(formats[i]);
            }
            connection.close();
            saveSettings();
            updateGuiWithFormats();
        } catch (ConnectionException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } catch (ZebraPrinterLanguageUnknownException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } catch (ZebraIllegalArgumentException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } finally {
            helper.dismissLoadingDialog();
        }
    }

    private void saveSettings() {
        SettingsHelper.saveBluetoothAddress(ListFormatsScreen.this, macAddress);
        SettingsHelper.saveIp(ListFormatsScreen.this, tcpAddress);
        SettingsHelper.savePort(ListFormatsScreen.this, tcpPort);
    }

    private void updateGuiWithFormats() {
        runOnUiThread(new Runnable() {
            public void run() {
                statusListAdapter.notifyDataSetChanged();
                Toast.makeText(ListFormatsScreen.this, "Found " + formatsList.size() + (retrieveFormats ? " Formats" : " Files"), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
