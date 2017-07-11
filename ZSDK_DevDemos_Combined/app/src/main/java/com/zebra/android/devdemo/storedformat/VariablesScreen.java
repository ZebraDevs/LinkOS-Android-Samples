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

package com.zebra.android.devdemo.storedformat;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.zebra.android.devdemo.R;
import com.zebra.android.devdemo.util.UIHelper;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.printer.FieldDescriptionData;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

public class VariablesScreen extends Activity {

    private boolean bluetoothSelected;
    private String macAddress;
    private String tcpAddress;
    private String tcpPort;
    private String formatName;
    private List<FieldDescriptionData> variablesList = new ArrayList<FieldDescriptionData>();
    private List<EditText> variableValues = new ArrayList<EditText>();
    private UIHelper helper = new UIHelper(this);
    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.stored_format_variables);
        Bundle b = getIntent().getExtras();
        bluetoothSelected = b.getBoolean("bluetooth selected");
        macAddress = b.getString("mac address");
        tcpAddress = b.getString("tcp address");
        tcpPort = b.getString("tcp port");
        formatName = b.getString("format name");

        TextView formatNameTextView = (TextView) this.findViewById(R.id.formatName);
        formatNameTextView.setText(formatName);

        final Button printButton = (Button) this.findViewById(R.id.printFormatButton);

        printButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                printButton.setEnabled(false);

                new Thread(new Runnable() {
                    public void run() {
                        printFormat();

                        runOnUiThread(new Runnable() {
                            public void run() {
                                printButton.setEnabled(true);
                            }
                        });
                    }
                }).start();
            }
        });

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                getVariables();
                Looper.loop();
                Looper.myLooper().quit();
            }
        }).start();

    }

    protected void getVariables() {
        helper.showLoadingDialog("Retrieving variables...");
        connection = getPrinterConnection();

        if (connection != null) {
            try {
                connection.open();
                ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);

                byte[] formatContents = printer.retrieveFormatFromPrinter(formatName);
                FieldDescriptionData[] variables = printer.getVariableFields(new String(formatContents, "utf8"));

                for (int i = 0; i < variables.length; i++) {
                    variablesList.add(variables[i]);
                }
                connection.close();
                updateGuiWithFormats();
            } catch (ConnectionException e) {
                helper.showErrorDialogOnGuiThread(e.getMessage());
            } catch (ZebraPrinterLanguageUnknownException e) {
                helper.showErrorDialogOnGuiThread(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                helper.showErrorDialogOnGuiThread(e.getMessage());
            }
        }
        helper.dismissLoadingDialog();
    }

    protected void printFormat() {
        helper.showLoadingDialog("Printing " + formatName + "...");
        connection = getPrinterConnection();

        if (connection != null) {
            try {
                connection.open();
                ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
                Map<Integer, String> vars = new HashMap<Integer, String>();

                for (int i = 0; i < variablesList.size(); i++) {
                    FieldDescriptionData var = variablesList.get(i);
                    vars.put(var.fieldNumber, variableValues.get(i).getText().toString());
                }
                printer.printStoredFormat(formatName, vars, "utf8");
                connection.close();
            } catch (ConnectionException e) {
                helper.showErrorDialogOnGuiThread(e.getMessage());
            } catch (ZebraPrinterLanguageUnknownException e) {
                helper.showErrorDialogOnGuiThread(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                helper.showErrorDialogOnGuiThread(e.getMessage());
            }
        }
        helper.dismissLoadingDialog();
    }

    private Connection getPrinterConnection() {
        if (bluetoothSelected == false) {
            try {
                int port = Integer.parseInt(tcpPort);
                connection = new TcpConnection(tcpAddress, port);
            } catch (NumberFormatException e) {
                helper.showErrorDialogOnGuiThread("Port number is invalid");
                return null;
            }
        } else {
            connection = new BluetoothConnection(macAddress);
        }
        return connection;
    }

    private void updateGuiWithFormats() {
        runOnUiThread(new Runnable() {
            public void run() {

                TableLayout varTable = (TableLayout) findViewById(R.id.variablesTable);

                for (int i = 0; i < variablesList.size(); i++) {
                    TableRow aRow = new TableRow(VariablesScreen.this);
                    aRow.setLayoutParams(new android.widget.TableRow.LayoutParams(android.widget.TableRow.LayoutParams.FILL_PARENT, android.widget.TableRow.LayoutParams.WRAP_CONTENT));

                    TextView varName = new TextView(VariablesScreen.this);

                    FieldDescriptionData var = variablesList.get(i);
                    varName.setText(var.fieldName == null ? "Field " + var.fieldNumber : var.fieldName);
                    varName.setLayoutParams(new android.widget.TableRow.LayoutParams(android.widget.TableRow.LayoutParams.FILL_PARENT, android.widget.TableRow.LayoutParams.WRAP_CONTENT));
                    aRow.addView(varName);

                    EditText value = new EditText(VariablesScreen.this);
                    value.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    value.setLayoutParams(new android.widget.TableRow.LayoutParams(android.widget.TableRow.LayoutParams.FILL_PARENT, android.widget.TableRow.LayoutParams.WRAP_CONTENT));
                    variableValues.add(value);
                    aRow.addView(value);

                    varTable.addView(aRow, new android.widget.TableLayout.LayoutParams(android.widget.TableRow.LayoutParams.FILL_PARENT, android.widget.TableRow.LayoutParams.WRAP_CONTENT));
                }

            }
        });
    }
}
