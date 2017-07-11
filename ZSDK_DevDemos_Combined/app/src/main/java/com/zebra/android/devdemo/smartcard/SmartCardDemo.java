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

package com.zebra.android.devdemo.smartcard;

import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zebra.android.devdemo.ConnectionScreen;
import com.zebra.android.devdemo.R;
import com.zebra.android.devdemo.util.SettingsHelper;
import com.zebra.android.devdemo.util.UIHelper;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.device.SmartcardReader;
import com.zebra.sdk.device.SmartcardReaderFactory;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

public class SmartCardDemo extends ConnectionScreen {

    private UIHelper helper = new UIHelper(this);
    private boolean sendData = true;
    private TextView response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        testButton.setText("Send Data");

        Button sendATR = new Button(this);
        sendATR.setText("Send ATR");

        TextView header = new TextView(this);
        header.setText("Response from printer");
        header.setTextSize((float) 20.0);

        response = new TextView(this);
        response.setBackgroundColor(0xffffffff);
        response.setTextSize((float) 20.0);

        LinearLayout connection_screen_layout = (LinearLayout) findViewById(R.id.connection_screen_layout);
        int index = connection_screen_layout.indexOfChild(testButton);

        connection_screen_layout.addView(sendATR, index + 1);
        connection_screen_layout.addView(header, index + 2);
        connection_screen_layout.addView(response, index + 3);

        sendATR.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                sendData = false;
                performTest();
            }
        });

    }

    @Override
    public void performTest() {
        response.setText("");

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                connectAndGetData();
                Looper.loop();
                Looper.myLooper().quit();
                sendData = true;
            }
        }).start();

    }

    private void connectAndGetData() {
        Connection connection = null;
        if (isBluetoothSelected() == false) {
            try {
                connection = new TcpConnection(getTcpAddress(), Integer.valueOf(getTcpPortNumber()));
            } catch (NumberFormatException e) {
                helper.showErrorDialogOnGuiThread("Port number is invalid");
                return;
            }
        } else {
            connection = new BluetoothConnection(getMacAddressFieldText());
        }
        try {
            helper.showLoadingDialog("Connecting...");
            connection.open();
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
            SmartcardReader smartcardReader = SmartcardReaderFactory.create(printer);
            if (smartcardReader != null) {
                final byte[] response;
                response = sendData ? smartcardReader.doCommand("8010000008") : smartcardReader.getATR();
                smartcardReader.close();
                runOnUiThread(new Runnable() {

                    public void run() {
                        SmartCardDemo.this.response.setText(toHexString(response));
                    }
                });
            } else {
                helper.showErrorDialogOnGuiThread("Printer does not support Smart Cards");
            }

            connection.close();
            saveSettings();
        } catch (ConnectionException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } catch (ZebraPrinterLanguageUnknownException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } finally {
            helper.dismissLoadingDialog();
        }
    }

    private String toHexString(byte[] byteArr) {
        StringBuilder sb = new StringBuilder();
        String temp;
        for (byte b : byteArr) {
            temp = String.format("%02x", b);
            sb.append(temp);
        }
        return sb.toString();
    }

    private void saveSettings() {
        SettingsHelper.saveBluetoothAddress(SmartCardDemo.this, getMacAddressFieldText());
        SettingsHelper.saveIp(SmartCardDemo.this, getTcpAddress());
        SettingsHelper.savePort(SmartCardDemo.this, getTcpPortNumber());
    }
}
