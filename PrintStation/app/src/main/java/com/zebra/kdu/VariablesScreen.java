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
package com.zebra.kdu;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.Intents;
import com.zebra.kdu.util.FileLoader;
import com.zebra.kdu.util.FinishInfo;
import com.zebra.kdu.util.FormatRefresher;
import com.zebra.kdu.util.SavedFormatProvider;
import com.zebra.kdu.util.SelectedPrinterManager;
import com.zebra.kdu.util.UIHelper;
import com.zebra.kdu.util.UsbHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.FieldDescriptionData;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;

public class VariablesScreen extends Activity implements FinishInfo {
	
    private String formatName;
    private DiscoveredPrinter formatPrinter;
    private String formatLocation;
    private String formatSource;
    private String formatContents;
    private List<FieldDescriptionData> variablesList = new ArrayList<FieldDescriptionData>();
    private List<EditText> variableEditTexts = new ArrayList<EditText>();
    private List<String> variableValues = new ArrayList<String>();
    private int scanTargetIndex = -1;
    private UIHelper uiHelper = new UIHelper(this);
    private UsbHelper usbHelper = new UsbHelper(this) {
		@Override
		public void usbDisconnected(UsbDevice device) {
			
			DiscoveredPrinter [] printers = SelectedPrinterManager.getPrinterHistory();
			for (int i = 0; i < printers.length; i++) {
				DiscoveredPrinter printer = printers[i];
				if (printer instanceof DiscoveredPrinterUsb && device.getDeviceName().equals(((DiscoveredPrinterUsb) printer).device.getDeviceName())) {
					SelectedPrinterManager.removeHistoryItemAtIndex(i);
					if (printer.address.equals(formatPrinter.address)) {
						uiHelper.dismissLoadingDialog();
						finish();
					}
					return;
				}
			}
		}

		@Override
		public void usbConnectedAndPermissionGranted(UsbDevice device) {
			uiHelper.dismissLoadingDialog();
			finish();
		}
    };
    private Connection connection;
	private SavedFormatProvider savedFormatProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	savedFormatProvider = new SavedFormatProvider(this);
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.stored_format_variables);
        
        Bundle b = getIntent().getExtras();
        formatName = b.getString(FormatRefresher.FORMAT_NAME);
        formatSource = b.getString(FormatRefresher.FORMAT_SOURCE_KEY);
        formatLocation = b.getString(FormatRefresher.FORMAT_LOCATION_KEY);
        formatPrinter = SelectedPrinterManager.getSelectedPrinter();
        
        updatePrintButton();

        usbHelper.onCreate(getIntent());
        
        new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
			    getVariables();
				return null;
			}}.execute((Void)null);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	TextView formatNameTextView = (TextView) this.findViewById(R.id.formatName);
        formatNameTextView.setText(formatName);
        usbHelper.onResume();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	usbHelper.onPause();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
    	usbHelper.onNewIntent(intent);
    }
    
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	   switch (item.getItemId()) 
    	   {        
    	      case android.R.id.home:            
    	         finish();
    	         return true;        
    	      default:            
    	         return super.onOptionsItemSelected(item);    
    	   }
    }

	private void updatePrintButton() {
		final Button printButton = (Button) this.findViewById(R.id.printFormatButton);
        printButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
            	new AsyncTask<Void, Void, Void>() {

            		protected void onPreExecute() {
            			VariablesScreen.this.findViewById(R.id.printFormatButton).setEnabled(false);
            		};
            		
					@Override
					protected Void doInBackground(Void... params) {
						printFormat();
						return null;
					}

					protected void onPostExecute(Void result) {
						VariablesScreen.this.findViewById(R.id.printFormatButton).setEnabled(true);
					};
            	}.execute((Void)null);
                
            }
        });
	}
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      setContentView(R.layout.stored_format_variables);
      updateGuiWithVariables();
      updatePrintButton();
      
      TextView formatNameTextView = (TextView) this.findViewById(R.id.formatName);
      formatNameTextView.setText(formatName);
    }

    protected void getVariables() {
        uiHelper.showLoadingDialog("Retrieving variables...");
        
        connection = SelectedPrinterManager.getPrinterConnection();

        if (connection != null) {
            try {
                connection.open();
                ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
                
                FieldDescriptionData[] variables;
                
                if (formatSource.equals(FormatRefresher.FORMAT_SOURCE_DATABASE)) {
                	formatContents = savedFormatProvider.getFormatContents(Long.parseLong(formatLocation));
                } else if (formatSource.equals(FormatRefresher.FORMAT_SOURCE_FILESYSTEM)) {
                	formatContents = FileLoader.toUtf8String(formatLocation);
                } else {
                	formatContents = new String(printer.retrieveFormatFromPrinter(formatName), "UTF-8");
                }
                
                variables = printer.getVariableFields(formatContents);
                
                for (int i = 0; i < variables.length; i++) {
                    variablesList.add(variables[i]);
                }
                updateGuiWithVariables();
            } catch (ConnectionException e) {
                uiHelper.showErrorDialogOnGuiThread(e.getMessage());
            } catch (ZebraPrinterLanguageUnknownException e) {
                uiHelper.showErrorDialogOnGuiThread(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                uiHelper.showErrorDialogOnGuiThread(e.getMessage());
            } finally {
            	try {
					connection.close();
				} catch (ConnectionException e) {
					uiHelper.showErrorDialogOnGuiThread(e.getMessage());
				}
            }
        }
        uiHelper.dismissLoadingDialog();
    }

    protected void printFormat() {
        uiHelper.showLoadingDialog("Printing " + formatName + "...");
        connection = SelectedPrinterManager.getPrinterConnection();

        if (connection != null) {
            try {
                connection.open();
                Map<Integer, String> vars = new HashMap<Integer, String>();
                for (int i = 0; i < variablesList.size(); i++) {
                    FieldDescriptionData var = variablesList.get(i);
                    vars.put(var.fieldNumber, variableEditTexts.get(i).getText().toString());
                }
                String quantityString = variableEditTexts.get(variablesList.size()).getText().toString();
                
                int quantity;
                try {
					quantity = Integer.parseInt(quantityString);
					if (quantity < 1) {
						quantity = 1;
					}
				} catch (NumberFormatException e) {
					quantity = 1;
				}
                
                if (formatSource.equals(FormatRefresher.FORMAT_SOURCE_PRINTER) == false) {
                	connection.write(formatContents.getBytes("UTF-8"));
                }
                ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
                for(int i = 0; i < quantity; i++) {
                	printer.printStoredFormat(formatName, vars);
                }
            } catch (ConnectionException e) {
                uiHelper.showErrorDialogOnGuiThread(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                uiHelper.showErrorDialogOnGuiThread(e.getMessage());
            } catch (ZebraPrinterLanguageUnknownException e) {
            	uiHelper.showErrorDialogOnGuiThread(e.getMessage());
			} finally {
            	try { connection.close(); } catch (ConnectionException e) {}
            }
        }
        try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
        uiHelper.dismissLoadingDialog();
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			Bundle extras = data.getExtras();
			String result = extras.getString(Intents.Scan.RESULT);
			
			if (result != null && scanTargetIndex != -1) {
				variableEditTexts.get(scanTargetIndex).setText(result);
				scanTargetIndex = -1;
			}
		}
	}

    private void updateGuiWithVariables() {
        runOnUiThread(new Runnable() {
            public void run() {

                TableLayout varTable = (TableLayout) findViewById(R.id.variablesTable);
                variableEditTexts.clear();

                for (int i = 0; i < variablesList.size(); i++) {
                	FieldDescriptionData var = variablesList.get(i);
                	boolean showScanButton = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
                	String fieldName = var.fieldName == null ? "Field " + var.fieldNumber : var.fieldName;
                	addLineItem(varTable, i, fieldName,  showScanButton, InputType.TYPE_TEXT_VARIATION_NORMAL | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                }
                
                variableValues.add("1");
                addLineItem(varTable, variablesList.size(), "Quantity", false, InputType.TYPE_CLASS_NUMBER);
            }
        });
    }
    
    int convertDpToPx(int dp) {
    	DisplayMetrics metrics = getResources().getDisplayMetrics();
    	return (int) (metrics.density * dp + 0.5f);
    }
    
    private volatile boolean isFinished = false;
    
    @Override
    public void finish() {
    	isFinished = true;
    	uiHelper.dismissLoadingDialog();
    	super.finish();
    	overridePendingTransition(0, android.R.anim.slide_out_right);
    }

	public boolean isFinished() {
		return isFinished;
	}

	private TableRow addLineItem(TableLayout varTable, int i, String fieldName, boolean showScanButton, int inputType) {
		final int scanIndex = i;

		TableRow aRow = new TableRow(VariablesScreen.this);
		aRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

		TextView varName = new TextView(VariablesScreen.this);
		varName.setPadding(varName.getPaddingLeft(), varName.getPaddingTop(), varName.getPaddingRight() + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()), varName.getPaddingBottom());

		varName.setText(fieldName);
		LayoutParams varNameLayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		varNameLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		varName.setLayoutParams(varNameLayoutParams);
		aRow.addView(varName);

		RelativeLayout lineItemLayout = new RelativeLayout(VariablesScreen.this);
		LayoutParams lineItemLayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		lineItemLayoutParams.weight = 1.0f;
		lineItemLayout.setLayoutParams(lineItemLayoutParams);
		
		final EditText value = new EditText(VariablesScreen.this);
		value.setInputType(inputType);
		value.setPadding(value.getPaddingLeft(), value.getPaddingTop(), value.getPaddingRight() + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics()), value.getPaddingBottom());
		value.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
		value.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				variableValues.set(scanIndex, s.toString());
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}});
		if (i < variableValues.size() && variableValues.get(i) != null) {
			value.setText(variableValues.get(i));
		}
		variableEditTexts.add(value);
		if (i >= variableValues.size()) {
			variableValues.add(value.getText().toString());
		} else {
			variableValues.set(i, value.getText().toString());
		}
		lineItemLayout.addView(value);
		
		if (showScanButton) {
			final ImageButton scanButton = new ImageButton(VariablesScreen.this);
			scanButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					scanTargetIndex = scanIndex;
					Intent intent = new Intent(VariablesScreen.this, CaptureActivity.class);
					startActivityForResult(intent, 0);
				}});
			RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			buttonLayoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
			buttonLayoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
			buttonLayoutParams.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
			scanButton.setLayoutParams(buttonLayoutParams);
			scanButton.setPadding(0, 0, 0, 0);
			scanButton.setScaleType(ScaleType.FIT_CENTER);

			scanButton.setVisibility(View.INVISIBLE);
			value.setOnFocusChangeListener(new OnFocusChangeListener() {

				public void onFocusChange(View v, boolean hasFocus) {
					scanButton.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
				}});

			scanButton.setImageResource(R.drawable.image_scan);
			lineItemLayout.addView(scanButton);
		}
		                    
		aRow.addView(lineItemLayout);
		
		varTable.addView(aRow, new TableLayout.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		return aRow;
	}
}
