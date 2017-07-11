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

package com.zebra.android.devdemo.magcard;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.zebra.android.devdemo.R;
import com.zebra.android.devdemo.util.SettingsHelper;
import com.zebra.android.devdemo.util.UIHelper;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.device.MagCardReader;
import com.zebra.sdk.device.MagCardReaderFactory;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

public class MagCardDemo extends Activity {

    private boolean bluetoothSelected;
    private String tcpAddress;
    private String tcpPort;
    private String macAddress;
    private RadioButton btRadioButton;
    private EditText macAddressEditText;
    private EditText ipAddressEditText;
    private EditText portNumberEditText;
    private static final String bluetoothAddressKey = "ZEBRA_DEMO_BLUETOOTH_ADDRESS";
    private static final String tcpAddressKey = "ZEBRA_DEMO_TCP_ADDRESS";
    private static final String tcpPortKey = "ZEBRA_DEMO_TCP_PORT";
    private static final String PREFS_NAME = "OurSavedAddress";
    private UIHelper helper = new UIHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mag_card_demo);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        ipAddressEditText = (EditText) this.findViewById(R.id.ipAddressInput);
        String ip = settings.getString(tcpAddressKey, "");
        ipAddressEditText.setText(ip);

        portNumberEditText = (EditText) this.findViewById(R.id.portInput);
        String port = settings.getString(tcpPortKey, "");
        portNumberEditText.setText(port);

        macAddressEditText = (EditText) this.findViewById(R.id.macInput);
        String mac = settings.getString(bluetoothAddressKey, "");
        macAddressEditText.setText(mac);

        btRadioButton = (RadioButton) this.findViewById(R.id.bluetoothRadio);

        Button testButton = (Button) this.findViewById(R.id.testButton);
        testButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                performTest();
            }
        });

        RadioGroup radioGroup = (RadioGroup) this.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.bluetoothRadio) {
                    toggleEditField(macAddressEditText, true);
                    toggleEditField(portNumberEditText, false);
                    toggleEditField(ipAddressEditText, false);
                } else {
                    toggleEditField(portNumberEditText, true);
                    toggleEditField(ipAddressEditText, true);
                    toggleEditField(macAddressEditText, false);
                }
            }
        });
    }

    private void toggleEditField(EditText editText, boolean set) {
        /*
         * Note: Disabled EditText fields may still get focus by some other means, and allow text input.
         *       See http://code.google.com/p/android/issues/detail?id=2771
         */
        editText.setEnabled(set);
        editText.setFocusable(set);
        editText.setFocusableInTouchMode(set);
    }

    private boolean isBluetoothSelected() {
        return btRadioButton.isChecked();
    }

    private String getMacAddressFieldText() {
        return macAddressEditText.getText().toString();
    }

    private String getTcpAddress() {
        return ipAddressEditText.getText().toString();
    }

    private String getTcpPortNumber() {
        return portNumberEditText.getText().toString();
    }

    public void performTest() {
        bluetoothSelected = isBluetoothSelected();
        macAddress = getMacAddressFieldText();
        tcpAddress = getTcpAddress();
        tcpPort = getTcpPortNumber();

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                getMagCardData();
                Looper.loop();
                Looper.myLooper().quit();
            }
        }).start();

    }

    private void getMagCardData() {
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
            helper.showLoadingDialog("Connecting...");
            connection.open();
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
            MagCardReader magCardReader = MagCardReaderFactory.create(printer);
            if (magCardReader != null) {
                helper.updateLoadingDialog("Swipe Card Now");

                final String[] trackData = magCardReader.read(30000);

                if (trackData[0].equals("")) {
                    helper.showErrorDialogOnGuiThread("Connection Timed Out");

                }
                runOnUiThread(new Runnable() {

                    public void run() {
                        ((EditText) findViewById(R.id.t1Result)).setText(trackData[0]);
                        ((EditText) findViewById(R.id.t2Result)).setText(trackData[1]);
                        ((EditText) findViewById(R.id.t3Result)).setText(trackData[2]);
                    }
                });
            } else {
                helper.showErrorDialogOnGuiThread("Printer does not support Mag Cards");
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

    private void saveSettings() {
        SettingsHelper.saveBluetoothAddress(MagCardDemo.this, macAddress);
        SettingsHelper.saveIp(MagCardDemo.this, tcpAddress);
        SettingsHelper.savePort(MagCardDemo.this, tcpPort);
    }
}
