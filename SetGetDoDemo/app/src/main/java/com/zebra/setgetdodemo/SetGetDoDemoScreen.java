/********************************************** 
 * CONFIDENTIAL AND PROPRIETARY 
 *
 * The source code and other information contained herein is the confidential and the exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published, 
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 * 
 * Copyright ZIH Corp. 2010
 *
 * ALL RIGHTS RESERVED 
 ***********************************************/

package com.zebra.setgetdodemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.zebra.android.comm.BluetoothPrinterConnection;
import com.zebra.android.comm.TcpPrinterConnection;
import com.zebra.android.comm.ZebraPrinterConnection;
import com.zebra.android.comm.ZebraPrinterConnectionException;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.printer.FormatUtil;
import com.zebra.sdk.printer.SGD;

import java.util.ArrayList;

public class SetGetDoDemoScreen extends AppCompatActivity {

	private boolean bluetoothSelected;
	private String macAddress;
	private String tcpAddress;
	private String tcpPort;

	private EditText setVarNameEditText;
	private EditText getVarNameEditText;
	private EditText setVarValueEditText;
	private EditText getVarValueEditText;

	private Button sendAllcvButton;
	private Button setVarButton;
	private Button getVarButton;

	private Connection zebraPrinterConnection;
	private PrinterVariableListAdapter variableListAdapter;
	private ArrayList<PrinterVariablesListData> variableValuesList = new ArrayList<PrinterVariablesListData>();
	private ListView variableListView;
	private ProgressDialog loadingProgressDialog;

	private final static String tag = "SetGetDoDemo";



	/**
	 * This application is a sample implementation of how to send Set Get Do
	 * commands to Zebra printer connected via TCP or Bluetooth from Android.
	 * The application lists all the variables using the allcv command and
	 * allows the user to change the value of the variables.
	 *
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		setContentView(R.layout.sgd_demo_screen);
		Bundle b = getIntent().getExtras();
		bluetoothSelected = b.getBoolean("bluetooth selected");
		macAddress = b.getString("mac address");
		tcpAddress = b.getString("tcp address");
		tcpPort = b.getString("tcp port");
		initUI();
		// Opens the connection to the printer in a new thread
		new Thread(new Runnable() {
			public void run() {
				Looper.prepare();
				openPrinterConnection();
				Looper.loop();
				Looper.myLooper().quit();
			}
		}).start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//  Close the connection if it is open and close this view
		if (zebraPrinterConnection != null
				&& zebraPrinterConnection.isConnected()) {
			disconnect();
			finish();
		}
		dismissLoadingDialog();
	}

	/**
	 * Dismisses the ProgressDialog on the UI thread
	 * 
	 **/
	private void dismissLoadingDialog() {
		runOnUiThread(new Runnable() {
			public void run() {
				Log.d(tag, "in dismissLoadingDialog");
				if (loadingProgressDialog != null) {
					if (loadingProgressDialog.isShowing())
						loadingProgressDialog.dismiss();
					Log.d(tag, "dismissed localLoadingDialog");
					loadingProgressDialog = null;
				}
			}
		});

		/*
		 * Log.d(tag, "in dismissLoadingDialog"); if (loadingProgressDialog !=
		 * null) { if (loadingProgressDialog.isShowing())
		 * loadingProgressDialog.dismiss(); Log.d(tag,
		 * "dismissed localLoadingDialog"); loadingProgressDialog = null; }
		 */

	}

	/**
	 * Shows the ProgressDialog on the UI thread
	 * 
	 **/
	private void showLoadingDialog(final String message) {
		runOnUiThread(new Runnable() {
			public void run() {
				Log.d(tag, "in showLoadingDialog");
				loadingProgressDialog = ProgressDialog.show(
						SetGetDoDemoScreen.this, "", message, true, false);
			}
		});
	}

	/**
	 * Pop-ups the Error Dialog
	 * 
	 **/
	private void showErrorDialog(String errorMessage) {
		new AlertDialog.Builder(this).setMessage(errorMessage)
				.setTitle("Error")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).create().show();
	}

	/**
	 * Pop-ups the Error Dialog and closes the current view on clicking ok
	 * 
	 **/
	private void showErrorDialogReturnToMain(final String errorMessage) {
		runOnUiThread(new Runnable() {
			public void run() {
				new AlertDialog.Builder(SetGetDoDemoScreen.this)
						.setMessage(errorMessage)
						.setTitle("Error")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
										finish();
									}
								}).create().show();
			}
		});

	}

	/**
	 * Initialize the UI - Sets the EditText and Button references. - Sets the
	 * button click actions
	 * */

	private void initUI() {

		setVarNameEditText = (EditText) this.findViewById(R.id.set_var_input);
		getVarNameEditText = (EditText) this.findViewById(R.id.get_var_input);
		setVarValueEditText = (EditText) this.findViewById(R.id.set_var_value);
		getVarValueEditText = (EditText) this.findViewById(R.id.get_var_value);

		setVarButton = (Button) this.findViewById(R.id.set_var_button);
		setVarButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				final String setting = setVarNameEditText.getText().toString()
						.trim();

				Thread backgroundThread = new Thread(new Runnable() {
					public void run() {
						showLoadingDialog("Changing value for Variable "
								+ setting + "...");
						if (sendSetVarCommand(setting, setVarValueEditText
								.getText().toString())) {
							final String value = sendGetVarCommand(setting);
							runOnUiThread(new Runnable() {
								public void run() {
									getVarValueEditText.setText(value);
								}
							});

						} else {
							showErrorDialog("Error Setting Value");
						}
						dismissLoadingDialog();
					}
				});
				backgroundThread.start();

			}
		});

		getVarButton = (Button) this.findViewById(R.id.get_var_button);
		getVarButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				final String setting = getVarNameEditText.getText().toString()
						.trim();

				Log.d("SGD Demo", "Retrieving the value of Variable " + setting
						+ "...");
				Thread backgroundThread = new Thread(new Runnable() {
					public void run() {
						showLoadingDialog("Retrieving the value of Variable "
								+ setting + "...");
						final String value = sendGetVarCommand(setting);
						if (value != null) {
							runOnUiThread(new Runnable() {
								public void run() {
									getVarValueEditText.setText(value);
								}
							});


						} else {
							showErrorDialog("Error Setting Value");
						}
						dismissLoadingDialog();
					}
				});
				backgroundThread.start();

			}
		});

		sendAllcvButton = (Button) this.findViewById(R.id.send_allcv_button);
		sendAllcvButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Thread backgroundThread = new Thread(new Runnable() {
					public void run() {
						showLoadingDialog("Sending allcv command...");
						sendAllcvCommand();
						dismissLoadingDialog();
					}
				});
				backgroundThread.start();
			}
		});

		variableListAdapter = new PrinterVariableListAdapter(this,
				android.R.layout.simple_list_item_1, variableValuesList);
		variableListView = (ListView) this.findViewById(R.id.statusList);
		variableListView.setAdapter(variableListAdapter);

		variableListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				PrinterVariablesListData selectedVariable = (PrinterVariablesListData) variableListView
						.getItemAtPosition(position);

				final String setting = selectedVariable.getVariableName()
						.trim();
				String value = selectedVariable.getVariableValue().trim();
				setVarNameEditText.setText(setting);
				getVarNameEditText.setText(setting);
				setVarValueEditText.setText(value);
				getVarValueEditText.setText(value);

			}
		});

	}

	/**
	 * Zebra Printer Connection Setup Methods
	 * 
	 * */

	private boolean openPrinterConnection() {
		zebraPrinterConnection = null;
		// dismissLoadingDialog();
		if (bluetoothSelected) {
			return openBluetoothConnection();
		} else {
			return openTCPConnection();
		}
	}

	/**
	 * Open Bluetooth Connection to Printer
	 * 
	 * */
	private boolean openBluetoothConnection() {
		boolean success = false;
		showLoadingDialog("Connecting to Printer via Bluetooth...");
		try {
			zebraPrinterConnection = new BluetoothConnection(macAddress);
			zebraPrinterConnection.open();
			SettingsHelper.saveBluetoothAddress(this, macAddress);
			success = true;

		} catch (ConnectionException e) {
			showErrorDialogReturnToMain(e.getMessage());
			disconnect();
		}
		dismissLoadingDialog();
		return success;
	}

	/**
	 * Open TCP/IP Connection to Printer
	 * 
	 * */
	private boolean openTCPConnection() {
		boolean success = false;
		int portNumber = 0;
		showLoadingDialog("Connecting to Printer via TCP/IP...");
		try {
			portNumber = Integer.parseInt(tcpPort.trim());
			zebraPrinterConnection = new TcpConnection(tcpAddress,
					portNumber);
			zebraPrinterConnection.open();
			SettingsHelper.saveIp(this, tcpAddress);
			SettingsHelper.savePort(this, tcpPort);
			success = true;
			Log.d("SGD Demo", "Connection Succesful");
		} catch (ConnectionException e) {
			showErrorDialogReturnToMain(e.getMessage());
			disconnect();
		} catch (NumberFormatException e) {
			showErrorDialogReturnToMain("Port " + tcpPort + " is not valid.");
			disconnect();
		}
		dismissLoadingDialog();
		return success;
	}

	public void disconnect() {
		try {
			if (zebraPrinterConnection != null) {
				zebraPrinterConnection.close();
			}
			dismissLoadingDialog();
		} catch (ConnectionException e) {
		}
	}

	/**
	 * This method parses the allv comamnd output and extracts all the variables
	 * and the current values as a list of PrinterVariablesListData
	 * 
	 * */
	private ArrayList<PrinterVariablesListData> getAllcvOutputAsArray(
			String text) {
		ArrayList<PrinterVariablesListData> returnList = new ArrayList<PrinterVariablesListData>();
		PrinterVariablesListData temp;
		String[] strings = text.split("\n");
		String selectedItems[];
		String setting;
		String value;
		for (String s : strings) {
			if (s.contains(" : ")) {
				selectedItems = s.split(" : ");
				setting = selectedItems[0].trim();
				value = selectedItems[1].trim();
				temp = new PrinterVariablesListData(setting, value);
				returnList.add(temp);
			}
		}
		return returnList;
	}

	/**
	 * This method sets the value for the variable setting with the passed value
	 * 
	 * */
	private boolean sendSetVarCommand(String setting, String value) {
		boolean success = false;
		try {
			// zebraPrinterConnection.read();
		   SGD.SET(setting, value, zebraPrinterConnection);
			success = true;

		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * This method sends the GetVar commands for the variable passed and returns
	 * the output.
	 * 
	 * */
	private String sendGetVarCommand(String setting) {
		String getvarOutput = null;
		try {
			zebraPrinterConnection.read();
			getvarOutput = SGD.GET(setting, zebraPrinterConnection);
		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getvarOutput;
	}

	/**
	 * This method sends the allcv command to the printer.
	 * */
	private void sendAllcvCommand() {
		String outputString;

		outputString = sendGetVarCommand("allv");

		if (outputString != null) {
			// Log.d("SGD DEmo", outputString);
			final ArrayList<PrinterVariablesListData> variableList = getAllcvOutputAsArray(outputString);
			if (variableList.isEmpty()) {
				Log.d("SGD Demo", "Cannot retrieve the variable list");

			} else {
				runOnUiThread(new Runnable() {
					public void run() {
						variableValuesList.clear();
						variableListAdapter.clear();
						variableValuesList.addAll(variableList);
						variableListAdapter.notifyDataSetChanged();
						variableListView.setSelection(0);
					}
				});
			}
		}

	}

	/**
	 * This Class contains the data for each of the variable list entries.
	 * variableName - name of the variable variableValue - current value of the
	 * variable
	 */

	private class PrinterVariablesListData {

		private String variableName;
		private String variableValue;

		private PrinterVariablesListData(String variableName,
				String variableValue) {
			this.variableName = variableName;
			this.variableValue = variableValue;
		}

		public String getVariableName() {
			return variableName;
		}

		@SuppressWarnings("unused")
		public void setVariableName(String variableName) {
			this.variableName = variableName;
		}

		public String getVariableValue() {
			return variableValue;
		}

		@SuppressWarnings("unused")
		public void setVariableValue(String variableValue) {
			this.variableValue = variableValue;
		}
	}

	/**
	 * This Class is the custom implementation of the Array adapter for the
	 * variables ListView.
	 */

	private class PrinterVariableListAdapter extends
			ArrayAdapter<PrinterVariablesListData> {

		private ArrayList<PrinterVariablesListData> items;

		public PrinterVariableListAdapter(Context context,
				int textViewResourceId,
				ArrayList<PrinterVariablesListData> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.row, null);
			}
			PrinterVariablesListData o = items.get(position);
			if (o != null) {
				TextView vName = (TextView) v
						.findViewById(R.id.rowvariablename);
				TextView vValue = (TextView) v
						.findViewById(R.id.rowvariablevalue);
				if (vName != null) {
					vName.setText(o.getVariableName() + " : ");
				}
				if (vValue != null) {
					vValue.setText(o.getVariableValue());
				}
			}
			return v;
		}
	}

}
