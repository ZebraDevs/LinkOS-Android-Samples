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

package com.zebra.android.devdemo.connectionbuilder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.zebra.android.devdemo.R;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionBuilder;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

public class ConnectionBuilderDemo extends Activity {

    private Spinner connectionPrefixSpinner;
    private EditText addressEditText;
    private TextView connectionString;
    private TextView connectionBuilderLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_builder);
        connectionPrefixSpinner = (Spinner) findViewById(R.id.connectionPrefixSpinner);
        addressEditText = (EditText) findViewById(R.id.connectionStringText);
        connectionString = (TextView) findViewById(R.id.connectionStringForSdk);
        connectionBuilderLog = (TextView) findViewById(R.id.connectionBuilderLog);

        connectionPrefixSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                connectionString.setText(getConnectionStringForSdk());
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        addressEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                connectionString.setText(getConnectionStringForSdk());
            }
        });
    }

    public void testConnectionStringButtonClicked(View v) {
        hideKeyboard();

        new AsyncTask<Void, String, Void>() {
            ProgressDialog pd;

            protected void onPreExecute() {
                connectionBuilderLog.setText("");
                pd = new ProgressDialog(ConnectionBuilderDemo.this);
                pd.setMessage("Attempting to connect...");
                pd.setIndeterminate(true);
                pd.setCancelable(false);
                pd.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Connection connection = ConnectionBuilder.build(getConnectionStringForSdk());
                    publishProgress("Connection string evaluated as class type " + connection.getClass().getSimpleName());
                    connection.open();
                    publishProgress("Connection opened successfully");

                    if (isAttemptingStatusConnection()) {
                        ZebraPrinterLinkOs printer = ZebraPrinterFactory.getLinkOsPrinter(connection);
                        publishProgress("Created a printer, attempting to retrieve status");
                        PrinterStatus status = printer.getCurrentStatus();
                        publishProgress("Is printer ready to print? " + status.isReadyToPrint);

                    } else {
                        ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
                        publishProgress("Created a printer, attempting to print a config label");
                        printer.printConfigurationLabel();
                    }

                    publishProgress("Closing connection");
                    connection.close();
                } catch (ConnectionException e) {
                    publishProgress("Connection could not be opened");
                } catch (ZebraPrinterLanguageUnknownException e) {
                    publishProgress("Unable to create printer");
                }
                return null;
            }

            protected void onProgressUpdate(String... s) {
                connectionBuilderLog.append(s[0] + System.getProperty("line.separator") + System.getProperty("line.separator"));
            };

            protected void onPostExecute(Void result) {
                pd.dismiss();
            };
        }.execute((Void) null);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(addressEditText.getWindowToken(), 0);
    }

    private String getConnectionStringForSdk() {
        String selectedPrefix = "";
        if (connectionPrefixSpinner.getSelectedItemPosition() > 0) {
            selectedPrefix = connectionPrefixSpinner.getSelectedItem().toString() + ":";
        }
        String userSuppliedDescriptionString = addressEditText.getText().toString();
        final String finalConnectionString = selectedPrefix + userSuppliedDescriptionString;
        return finalConnectionString;
    }

    private boolean isAttemptingStatusConnection() {
        return connectionPrefixSpinner.getSelectedItem().toString().contains("STATUS");
    }

}
