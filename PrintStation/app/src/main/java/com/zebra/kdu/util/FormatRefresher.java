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
package com.zebra.kdu.util;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Environment;
import android.view.View;
import android.widget.BaseAdapter;

import com.zebra.kdu.R;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

public class FormatRefresher {
	
	public static final String FORMAT_LOCATION_KEY = "formatLocation";
	public static final String FORMAT_SOURCE_KEY = "formatSource";
	public static final String FORMAT_SOURCE_PRINTER = "printer";
	public static final String FORMAT_SOURCE_DATABASE = "database";
	public static final String FORMAT_SOURCE_FILESYSTEM = "filesystem";
	public static final String FORMAT_NAME = "format name";
	
	private static AsyncTask<Void, Map<String, String>, Void> task;
	
	
	public static void execute(final Activity activity, final UIHelper uiHelper, final String [] attributeKeys, final List<Map<String, String>> formatsList, final BaseAdapter statusListAdapter) {
		if (task == null || task.getStatus() == Status.FINISHED) {
			task =  new AsyncTask<Void, Map<String, String>, Void>() {

				protected void onPreExecute() {
					uiHelper.showLoadingDialog("Retrieving Formats...");
					formatsList.clear();
				}

				@Override
				protected Void doInBackground(Void... params) {
					try {
						Connection connection = SelectedPrinterManager.getPrinterConnection();
						connection.open();
						ZebraPrinter printer = ZebraPrinterFactory.getInstance(PrinterLanguage.ZPL, connection);
						
						getSavedFormats();
						getFilesystemFormats();

						String[] formats = printer.retrieveFileNames(new String [] { "ZPL" });

						for (int i = 0; i < formats.length; i++) {
							int colonPosition = formats[i].indexOf(':') + 1;
							int dotPosition = formats[i].lastIndexOf('.');
							if (dotPosition < 0) { dotPosition = formats[i].length(); }

							String drive = formats[i].substring(0, colonPosition);

							String extension = formats[i].substring(dotPosition, formats[i].length());
							if (dotPosition < 0) {
								dotPosition = formats[i].length();
							}
							String name = formats[i].substring(colonPosition, dotPosition);

							Map<String, String> formatAttributes = new HashMap<String, String>();
							formatAttributes.put(attributeKeys[0], drive);
							formatAttributes.put(attributeKeys[1], name);
							formatAttributes.put(attributeKeys[2], extension);
							formatAttributes.put(attributeKeys[3], Integer.toString(R.drawable.btn_star_big_off));
							formatAttributes.put(attributeKeys[4], "");
							formatAttributes.put(FORMAT_SOURCE_KEY, FORMAT_SOURCE_PRINTER);

							publishProgress(formatAttributes);
						}
						connection.close();

					} catch (Exception e) {
						uiHelper.showErrorDialogOnGuiThread("Could not retrieve format list from the specified printer");
					} finally {
						uiHelper.dismissLoadingDialog();
					}
					return null;
				}

				private void getSavedFormats() {
					SavedFormatProvider savedFormatProvider = new SavedFormatProvider(activity);
					Collection<SavedFormat> savedFormats = savedFormatProvider.getSavedFormats();
					for (SavedFormat format : savedFormats) {
						SimpleDateFormat timeFormat = new SimpleDateFormat(SavedFormatProvider.DATE_FORMAT);
						Date date = new Date(format.timestamp);
						String timestamp = timeFormat.format(date);
						
						Map<String, String> formatAttributes = new HashMap<String, String>();
						formatAttributes.put(attributeKeys[0], "");
						formatAttributes.put(attributeKeys[1], format.formatName);
						formatAttributes.put(attributeKeys[2], format.formatExtension);
						formatAttributes.put(attributeKeys[3], Integer.toString(R.drawable.btn_star_big_on));
						formatAttributes.put(attributeKeys[4], "Retrieved from: " + format.sourcePrinterName + " on " + timestamp);
						formatAttributes.put(FORMAT_SOURCE_KEY, FORMAT_SOURCE_DATABASE);
						formatAttributes.put(FORMAT_LOCATION_KEY, Long.toString(format.id));
						publishProgress(formatAttributes);
					}
				}
				
				private void getFilesystemFormats() {
					String state = Environment.getExternalStorageState();
					File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
					if (!(state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) || storageDirectory.exists() == false) {
						return;
					}
					File[] savedFormats = storageDirectory.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String filename) {
							return filename.toLowerCase().endsWith(".zpl");
						}});
					for (File format : savedFormats) {
						String formatName = format.getName();
						String formatExtension = "";
						
						int indexOfLastDot = formatName.lastIndexOf(".");
						if (indexOfLastDot >= 0) {
							formatExtension = formatName.substring(indexOfLastDot);
							formatName = formatName.substring(0, indexOfLastDot);
						}
						
						Map<String, String> formatAttributes = new HashMap<String, String>();
						formatAttributes.put(attributeKeys[0], "");
						formatAttributes.put(attributeKeys[1], formatName);
						formatAttributes.put(attributeKeys[2], formatExtension);
						formatAttributes.put(attributeKeys[3], Integer.toString(R.drawable.btn_star_big_on));
						formatAttributes.put(attributeKeys[4], format.getAbsolutePath());
						formatAttributes.put(FORMAT_SOURCE_KEY, FORMAT_SOURCE_FILESYSTEM);
						formatAttributes.put(FORMAT_LOCATION_KEY, format.getAbsolutePath());
						publishProgress(formatAttributes);
					}
				}

				protected void onProgressUpdate(Map<String, String>... formatAttributes) {
					formatsList.add(formatAttributes[0]);
					statusListAdapter.notifyDataSetChanged();
				}

				protected void onPostExecute(Void result) {
					activity.findViewById(R.id.emptyFormat).setVisibility(View.VISIBLE);
					statusListAdapter.notifyDataSetChanged();
				}
			};
			task.execute((Void)null);
		}
	}



}
