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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

public class SavedFormatProvider {

	public static final String DATE_FORMAT = "d MMM yyyy HH:mm:ss";

	private static final String DATABASE_NAME = "saved_formats.db";
	private static final int DATABASE_VERSION = 1;
	
	private DatabaseHelper mOpenHelper;

	class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + SavedFormat.TABLE_NAME + " ("
					+ SavedFormat._ID + " INTEGER PRIMARY KEY,"
					+ SavedFormat.FORMAT_DRIVE + " TEXT,"
					+ SavedFormat.FORMAT_NAME + " TEXT,"
					+ SavedFormat.FORMAT_EXTENSION + " TEXT,"
					+ SavedFormat.SOURCE_PRINTER_NAME + " TEXT,"
					+ SavedFormat.FORMAT_TEXT + " TEXT,"
					+ SavedFormat.TIMESTAMP + " INTEGER"
					+ ");");
			
			String oilchangeFormat = 	"^XA" +
										"^CI28" +
										"^DFE:OILCHANGE.ZPL^FS" +
										"^FT181,184^A0N,28,28^FH\\^FN1\"Date\"^FS" +
										"^FT181,282^A0N,28,28^FH\\^FN2\"Mileage\"^FS" +
										"^FT32,106^GFA,1024,1024,16::::::::::::::L0F8,L0HFI0IFC0,L0HFC00FHFC0K0380,K01FHFC0FHFC0J01FC0,K03E7FF00780K03FE0,K03E1FF00780J01FHF0,K03C03F00780J07FDF8,K07800700780I01FF8F0,K078007FKF807FF870,K07F007FKFC1FHF0,K07FF07FLF7FDF0,K03FFC7FNF3E0,L0JFL0HFC3E010,M0IFL07F07C010,M03FF0K03C078018,N03F0N0F8018,O0F0M01F0038,O070M01E003C,O070M03E003C,O070M07C003C,O070M0780030,O07FNF8,O07FNF0,:O07FMFE0,,:::::::::::::::::::::::^FT60,177^A0N,34,33^FH\\^FDDATE^FS" +
										"^FT33,282^A0N,34,33^FH\\^FDMILEAGE^FS" +
										"^FO172,239^GB287,64,8^FS" +
										"^FO172,139^GB287,64,8^FS" +
										"^FT153,77^A0N,56,55^FH\\^FDOIL CHANGE^FS" +
										"^FO27,110^GB432,0,8^FS" +
										"^XZ";
			String addressFormat = 	"^XA" +
									"^CI28" +
									"^DFE:ADDRESS.ZPL^FS" +
									"^FT80,80^A0N,28,28^FH\\^FN1\"Name\"^FS" +
									"^FT80,110^A0N,28,28^FH\\^FN2\"Address1\"^FS" +
									"^FT80,140^A0N,28,28^FH\\^FN3\"Address2\"^FS" +
									"^FT80,170^A0N,28,28^FH\\^FN4\"CityStateZip\"^FS" +
									"^XZ";
			
			db.execSQL("INSERT INTO "+ SavedFormat.TABLE_NAME + "(" + SavedFormat._ID + ", " + 
																	  SavedFormat.FORMAT_DRIVE + ", " + 
																	  SavedFormat.FORMAT_NAME + ", " +
																	  SavedFormat.FORMAT_EXTENSION + ", " +
																	  SavedFormat.SOURCE_PRINTER_NAME + ", " +
																	  SavedFormat.FORMAT_TEXT + ", " +
																	  SavedFormat.TIMESTAMP + ") " +
														"VALUES (1, 'E:', 'OILCHANGE', '.ZPL', 'Sample', '" + oilchangeFormat + "', 1350426632404)"
					  );
			
			db.execSQL("INSERT INTO "+ SavedFormat.TABLE_NAME + "(" + SavedFormat._ID + ", " + 
																	  SavedFormat.FORMAT_DRIVE + ", " + 
																	  SavedFormat.FORMAT_NAME + ", " +
																	  SavedFormat.FORMAT_EXTENSION + ", " +
																	  SavedFormat.SOURCE_PRINTER_NAME + ", " +
																	  SavedFormat.FORMAT_TEXT + ", " +
																	  SavedFormat.TIMESTAMP + ") " +
					  									"VALUES (2, 'E:', 'ADDRESS', '.ZPL', 'Sample', '" + addressFormat + "', 1350426632404)"
					  );
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + SavedFormat.TABLE_NAME);
			onCreate(db);
		}
	}
	
	public SavedFormatProvider (Context context) {
		mOpenHelper = new DatabaseHelper(context);
	}

	public Collection<SavedFormat> getSavedFormats() {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(SavedFormat.TABLE_NAME);

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		Cursor c = qb.query(
				db,
				null,
				null,
				null,
				null,
				null,
				SavedFormat.TIMESTAMP
				);

		ArrayList<SavedFormat> savedFormats = new ArrayList<SavedFormat>();
		for(int i = 0; i < c.getCount(); i++) {
			c.moveToNext();
			
			Long id = 0L;
			String formatDrive = "";
			String formatName = "";
			String formatExtension = "";
			String sourcePrinterName = "";
			Long timestamp = 0L;
			String formatText = "";
			
			for (int j = 0; j < c.getColumnCount(); j++ ) {
				String currentColumnName = c.getColumnName(j);
				if (currentColumnName.equals(SavedFormat._ID)) {
					id = c.getLong(j);
				} else if (currentColumnName.equals(SavedFormat.FORMAT_DRIVE)) {
					formatDrive = c.getString(j);
				} else if (currentColumnName.equals(SavedFormat.FORMAT_NAME)) {
					formatName = c.getString(j);
				} else if (currentColumnName.equals(SavedFormat.FORMAT_EXTENSION)) {
					formatExtension = c.getString(j);
				} else if (currentColumnName.equals(SavedFormat.SOURCE_PRINTER_NAME)) {
					sourcePrinterName = c.getString(j);
				} else if (currentColumnName.equals(SavedFormat.TIMESTAMP)) {
					timestamp = c.getLong(j);
				} else if (currentColumnName.equals(SavedFormat.FORMAT_TEXT)) {
					formatText = c.getString(j);
				}
			}
			savedFormats.add(new SavedFormat(id, formatDrive, formatName, formatExtension, sourcePrinterName, timestamp, formatText));
		}
		
		db.close();
		
		return savedFormats;
	}


	public long insert(String formatDrive, String formatName, String formatExtension, String sourcePrinterName, String formatText) {

		ContentValues values = new ContentValues();

		Long now = Long.valueOf(System.currentTimeMillis());

		values.put(SavedFormat.TIMESTAMP, now);
		values.put(SavedFormat.FORMAT_DRIVE, formatDrive);
		values.put(SavedFormat.FORMAT_NAME, formatName);
		values.put(SavedFormat.FORMAT_EXTENSION, formatExtension);
		values.put(SavedFormat.SOURCE_PRINTER_NAME, sourcePrinterName);
		values.put(SavedFormat.FORMAT_TEXT, formatText);
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		long rowId = db.insert(
				SavedFormat.TABLE_NAME,
				null,
				values);
		
		db.close();

		return rowId;
	}

	public boolean delete(String id) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int rowsDeleted = db.delete(SavedFormat.TABLE_NAME, SavedFormat._ID + "=?", new String [] {id} );
		db.close();
		return rowsDeleted > 0;
	}
	
	public int getNumberOfStoredFormats() {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(SavedFormat.TABLE_NAME);

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		Cursor c = qb.query(
				db,
				null,
				null,
				null,
				null,
				null,
				SavedFormat.TIMESTAMP
				);
		int retVal = c.getCount();
		db.close();
		return retVal;
	}
	
	public String getTimestampOfFormat(long id) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(SavedFormat.TABLE_NAME);

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		Cursor c = qb.query(
				db,
				null,
				SavedFormat._ID + "=?",
				new String[] {Long.toString(id)},
				null,
				null,
				SavedFormat.TIMESTAMP
				);
		
		try {
			if (c.getCount() <= 0) {
				return "";
			} else {
				c.moveToFirst();
				SimpleDateFormat timeFormat = new SimpleDateFormat(DATE_FORMAT);
				Date date = new Date(c.getLong(c.getColumnIndex(SavedFormat.TIMESTAMP)));
				return timeFormat.format(date);
			}
		} finally {
			db.close();
		}
	}
	
	public String getFormatContents(long id) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(SavedFormat.TABLE_NAME);

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		Cursor c = qb.query(
				db,
				null,
				SavedFormat._ID + "=?",
				new String[] {Long.toString(id)},
				null,
				null,
				SavedFormat.TIMESTAMP
				);
		
		try {
			if (c.getCount() <= 0) {
				return "";
			} else {
				c.moveToFirst();
				return c.getString(c.getColumnIndex(SavedFormat.FORMAT_TEXT));
			}
		} finally {
			db.close();
		}
	}


}
