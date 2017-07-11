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

package com.zebra.android.devdemo.sigcapture;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zebra.android.devdemo.ConnectionScreen;
import com.zebra.android.devdemo.R;
import com.zebra.android.devdemo.util.SettingsHelper;
import com.zebra.android.devdemo.util.UIHelper;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

public class SigCaptureDemo extends ConnectionScreen {

    private UIHelper helper = new UIHelper(this);
    private SignatureArea signatureArea = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        testButton.setText("Print");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        signatureArea = new SignatureArea(this);
        LinearLayout connection_screen_layout = (LinearLayout) findViewById(R.id.connection_screen_layout);
        connection_screen_layout.addView(signatureArea, connection_screen_layout.indexOfChild(testButton) + 1, params);
    }

    public void performTest() {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                doPerformTest();
                Looper.loop();
                Looper.myLooper().quit();
            }
        }).start();

    }

    public void doPerformTest() {
        Connection connection;
        if (isBluetoothSelected() == false) {
            try {
                int port = Integer.parseInt(getTcpPortNumber());
                connection = new TcpConnection(getTcpAddress(), port);
            } catch (NumberFormatException e) {
                helper.showErrorDialogOnGuiThread("Port number is invalid");
                return;
            }
        } else {
            connection = new BluetoothConnection(getMacAddressFieldText());
        }
        try {
            helper.showLoadingDialog("Printing ...");
            connection.open();
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
            Bitmap image = signatureArea.getBitmap();
            printer.printImage(new ZebraImageAndroid(image), 0, 0, image.getWidth(), image.getHeight(), false);

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

    private void saveSettings() {
        SettingsHelper.saveBluetoothAddress(this, getMacAddressFieldText());
        SettingsHelper.saveIp(this, getTcpAddress());
        SettingsHelper.savePort(this, getTcpPortNumber());
    }
}
