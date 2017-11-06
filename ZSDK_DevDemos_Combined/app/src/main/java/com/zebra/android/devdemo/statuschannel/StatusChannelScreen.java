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

package com.zebra.android.devdemo.statuschannel;

import java.util.ArrayList;
import java.util.List;

import com.zebra.android.devdemo.R;
import com.zebra.android.devdemo.util.SettingsHelper;
import com.zebra.android.devdemo.util.UIHelper;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.BluetoothStatusConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpStatusConnection;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.util.internal.Sleeper;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class StatusChannelScreen extends Activity {
    private boolean bluetoothSelected;
    private String macAddress;
    private String tcpAddress;
    private String tcpStatusPort;
    private UIHelper helper = new UIHelper(this);
    private ArrayAdapter<String> statusListAdapter;
    private List<String> statusMessageList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_channel_activity);
        Bundle b = getIntent().getExtras();
        bluetoothSelected = b.getBoolean("bluetooth selected");
        macAddress = b.getString("mac address");
        tcpAddress = b.getString("tcp address");
        tcpStatusPort = b.getString("tcp status port");
        statusListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, statusMessageList);
        ListView statusList = (ListView) this.findViewById(R.id.statusListView);
        statusList.setAdapter(statusListAdapter);
        new Thread(new Runnable() {

            public void run() {
                helper.showLoadingDialog("Getting printer status");
                saveSettings();
                getStatus();
            }
        }).start();

    }

    private void getStatus() {
        Connection connection = null;
        Connection connectionRaw = null;
        try {

            if (bluetoothSelected) {
                // Printer only broadcasts the status connection if a valid raw connection is open
                connectionRaw = new BluetoothConnection(macAddress);
                connection = new BluetoothStatusConnection(macAddress);

                connectionRaw.open();
                Sleeper.sleep(3000); // give the printer some time to start the status connection
            } else {
                connection = new TcpStatusConnection(tcpAddress, Integer.parseInt(tcpStatusPort));
            }

            connection.open();
            ZebraPrinter printer = ZebraPrinterFactory.getLinkOsPrinter(connection);

            final PrinterStatus myPrinterStatus = printer.getCurrentStatus();

            runOnUiThread(new Runnable() {
                public void run() {
                    statusListAdapter.clear();
                    statusMessageList.clear();
                    statusMessageList.add("Is Printer Ready: " + myPrinterStatus.isReadyToPrint);
                    statusMessageList.add("Is Head Open: " + myPrinterStatus.isHeadOpen);
                    statusMessageList.add("Is Paper Out: " + myPrinterStatus.isPaperOut);
                    statusMessageList.add("Is Printer Paused: " + myPrinterStatus.isPaused);
                    statusMessageList.add("Batch Labels Remaining: " + myPrinterStatus.labelsRemainingInBatch);
                    statusListAdapter.notifyDataSetChanged();
                    helper.dismissLoadingDialog();
                }
            });
        } catch (Exception e) {
            helper.dismissLoadingDialog();
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (connectionRaw != null) {
                    connectionRaw.close();
                }
            } catch (ConnectionException e) {
                helper.showErrorDialogOnGuiThread(e.getMessage());
            }
        }
    }

    private void saveSettings() {
        SettingsHelper.saveBluetoothAddress(StatusChannelScreen.this, macAddress);
        SettingsHelper.saveIp(StatusChannelScreen.this, tcpAddress);
        SettingsHelper.saveStatusPort(StatusChannelScreen.this, tcpStatusPort);
    }

}
