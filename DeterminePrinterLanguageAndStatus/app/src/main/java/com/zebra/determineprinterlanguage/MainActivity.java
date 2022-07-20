/***********************************************
 * CONFIDENTIAL AND PROPRIETARY
 *
 * The source code and other information contained herein is the confidential and the exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published,
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 *
 * Copyright ZIH Corp. 2015
 *
 * ALL RIGHTS RESERVED
 ***********************************************/


package com.zebra.determineprinterlanguage;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

public class MainActivity extends AppCompatActivity {

    private Connection connection;
    private RadioButton btRadioButton;
    private EditText macAddressEditText;
    private EditText ipAddressEditText;
    private EditText portNumberEditText;
    private static final String bluetoothAddressKey = "ZEBRA_DEMO_BLUETOOTH_ADDRESS";
    private static final String tcpAddressKey = "ZEBRA_DEMO_TCP_ADDRESS";
    private static final String tcpPortKey = "ZEBRA_DEMO_TCP_PORT";
    private static final String PREFS_NAME = "OurSavedAddress";
    private UIHelper helper = new UIHelper(this);
    private Button determinePrinterLanguageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        TextView t2 = (TextView) findViewById(R.id.launchpad_link);
        t2.setMovementMethod(LinkMovementMethod.getInstance());


        btRadioButton = (RadioButton) this.findViewById(R.id.bluetoothRadio);

        determinePrinterLanguageButton=(Button)findViewById(R.id.testButton);
        determinePrinterLanguageButton.setText(R.string.printer_language);


        RadioGroup radioGroup = (RadioGroup) this.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

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
        //when we click the button it shows the language and status of the app
        determinePrinterLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Disable the button
                determinePrinterLanguageButton.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getPrinterLanguage();
                    }
                }).start();
            }

        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     * This method makes the call to the printer and get the status and language of the printer.
     *
     * @param
     */
    private void getPrinterLanguage() {

        new Thread(new Runnable() {
            public void run() {

                try {
                    getAndSaveSettings();
                    connection = getZebraPrinterConn();
                    connection.open();
                    ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);

                    ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);

                    PrinterStatus printerStatus = (linkOsPrinter != null) ? linkOsPrinter.getCurrentStatus() : printer.getCurrentStatus();

                    final String printerLanguage = SGD.GET("device.languages", connection);

                    final String displayPrinterLanguage = "Printer Language is " + printerLanguage;

                    /**
                     * check the printer status and get the language
                     */

                    if (printerStatus.isReadyToPrint) {

                        helper.showLoading(displayPrinterLanguage + "\n" +"\n" + "Printer Status: Printer Ready");

                    } else if (printerStatus.isHeadOpen) {
                        helper.showLoading(displayPrinterLanguage + "\n" + "\n" + "Printer Status: Head Open ");
                    } else if (printerStatus.isPaused) {
                        helper.showLoading(displayPrinterLanguage + "\n" + "\n" + "Printer Status: Printer Paused");
                    } else if (printerStatus.isPaperOut) {
                        helper.showLoading(displayPrinterLanguage + "\n" + "\n" + "Printer Status: Media Out ");
                    } else {
                        helper.showLoading(displayPrinterLanguage + "\n" +"\n" +"Printer Status: Connection Error");
                    }

                    connection.close();
                    connection = null;

                } catch (ConnectionException e) {
                    helper.showErrorDialogOnGuiThread(e.getMessage());
                } catch (ZebraPrinterLanguageUnknownException e) {
                    helper.showErrorDialogOnGuiThread(e.getMessage());
                } finally {
                    helper.dismissLoadingDialog();
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (ConnectionException e) {
                            // Do nothing.
                        }
                    }

                    // Enable the button
                    runOnUiThread(new Runnable() {
                        public void run() {
                            determinePrinterLanguageButton.setEnabled(true);
                        }
                    });
                }
            }

        }).start();

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

    /**
     * This method checks the mode of connection.
     *
     * @return
     */
    private Connection getZebraPrinterConn() {
        int portNumber;
        try {
            portNumber = Integer.parseInt(getTcpPortNumber());
        } catch (NumberFormatException e) {
            portNumber = 0;
        }
        return isBluetoothSelected() ? new BluetoothConnection(getMacAddressFieldText()) : new TcpConnection(getTcpAddress(), portNumber);
    }

    /**
     * This method saves the entered address for the printer.
     */

    private void getAndSaveSettings() {
        SettingsHelper.saveBluetoothAddress(MainActivity.this, getMacAddressFieldText());
        SettingsHelper.saveIp(MainActivity.this, getTcpAddress());
        SettingsHelper.savePort(MainActivity.this, getTcpPortNumber());
    }


}
