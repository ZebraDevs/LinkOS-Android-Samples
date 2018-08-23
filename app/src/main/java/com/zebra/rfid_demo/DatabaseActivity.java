package com.zebra.rfid_demo;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

/**
 * Mega activity that all other activities extend.
 *
 * Handles all of the CRUD operations for the SQLite database and the SharedPreferences (persistent
 * variables).
 *
 * Also contains a couple of general helper functions needed in a few spots and the global settings
 * variables.
 */
public class DatabaseActivity extends AppCompatActivity {
    private static final String DATABASE_TAG = "database_setup";
    private static final String SQLITE_TAG = "SQLite_command";

    protected static final String settingsCredentials = "zebra";  //Password for accessing and modifying the settings

    String softScanTrigger = "com.motorolasolutions.emdk.datawedge.api.ACTION_SOFTSCANTRIGGER";
    String extraData = "com.motorolasolutions.emdk.datawedge.api.EXTRA_PARAMETER";

    private String database_name = "rfid_Db";     //Name of the SQLite database
    //SQLiteDatabase rfidListDb;    //Instance of the database
    private int entryCount;         //The next EntryCount to be used in the database
    String folderName;              //The folder name where new files are to be put
    String suggestedDelimiter;      //The suggested delimiter from the settings menu
    boolean timestampFileNames;     //true if new .csv files should be time and date stamped
    boolean customDataEntry;      //true if new lead entries should use custom fields
    boolean parseScannedData;       //true if the app should attempt to parse scanned data strings
    ArrayList<String> customEntryFields;            //The fields new entry will use if custom entry is turned on

    public static final String RFID_lABEL_ID = "ID";
    public static final String UPC_Number = "UPC";
    public static final String Company_Name = "Company_Name";
    public static final String Item_Name = "Product_Name";
    public static final String Item_Price = "Price";
    public static final String Item_Quantity = "Quantity";
    public static final String Item_Discount = "Discount";
    public static final String Item_Color = "Color";
    public static final String Item_Style ="Style";
    public static final String Item_Size = "Size";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSharedVariables();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateSharedVariables();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSharedVariables();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateSharedVariables();
    }

    public void createDb(String[] fields){
        SQLiteDatabase rfidListDb = openOrCreateDatabase(database_name, MODE_PRIVATE, null);

        String create_string = "CREATE TABLE IF NOT EXISTS " + database_name
                + "(" +RFID_lABEL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + UPC_Number+ " TEXT," + Company_Name + " TEXT ," + Item_Name + " TEXT ,"
                + Item_Price +" TEXT ," + Item_Quantity + " TEXT ,"
                + Item_Discount+ " TEXT," + Item_Color + " TEXT," + Item_Style +" TEXT ," + Item_Size + " TEXT "+ ");";
        Log.d(SQLITE_TAG,"Executing " + create_string);
        rfidListDb.execSQL(create_string);
        rfidListDb.close();
    }


    public void addNewEntry(String[] fields) {
        if(!fields[0].contains("100000000001") || !fields[0].contains("")){
        String[] tempoCheck = new String[9];
        tempoCheck = retrieveDataUPC(fields[0]);
        if(tempoCheck[0]==null ) {
            try {
              createDb(fields);
                SQLiteDatabase rfidListDb = openOrCreateDatabase(database_name, MODE_PRIVATE, null);
                /*retrieve data from database */

                /* Insert data first time to a Table*/
                rfidListDb.execSQL("INSERT INTO "
                        + database_name
                        + " (" + UPC_Number + "," + Company_Name + "," + Item_Name + "," + Item_Price + "," + Item_Quantity + "," + Item_Discount + "," + Item_Color + "," + Item_Style + "," + Item_Size + ")"
                        + " VALUES ('" + fields[0] + "','" + fields[1] + "','" + fields[2] + "','" + fields[3] + "','" + fields[4] + "','" + fields[5] + "','" + fields[6] + "','" + fields[7] + "','" + fields[8] + "');");
                rfidListDb.close();
            } catch (Exception e) {
                createDb(fields);

                SQLiteDatabase rfidListDb = openOrCreateDatabase(database_name, MODE_PRIVATE, null);
                /*retrieve data from database */

                rfidListDb.execSQL("INSERT INTO "
                        + database_name
                        + " (" + UPC_Number + "," + Company_Name + "," + Item_Name + "," + Item_Price + "," + Item_Quantity + ","
                        + Item_Discount + "," + Item_Color + "," + Item_Style + "," + Item_Size + ")"
                        + " VALUES ('" + fields[0] + "','" + fields[1] + "','" + fields[2] + "','" + fields[3] + "','" + fields[4] + "','" + fields[5] + "','" + fields[6] + "','" + fields[7] + "','" + fields[8] + "');");
                rfidListDb.close();
            }
        }else{
            updaterecord(fields);

        }}else{
            Log.d(DATABASE_TAG, "Invalid data or empty fields");
        }


    }

    public void updaterecord(String [] updateLabel){
        SQLiteDatabase rfidListDb = openOrCreateDatabase(database_name, MODE_PRIVATE, null);

        String query = "UPDATE "+database_name+" SET "+Item_Quantity+ "='"+updateLabel[4]
                        +"'WHERE "+ UPC_Number + " = '" + updateLabel[0] + "'";

        String query2 = "UPDATE "+database_name+" SET "+Company_Name+ "='"+updateLabel[1]
                         +"',"+Item_Name + "='"+updateLabel[2]+"',"+Item_Price +"='"+updateLabel[3]
                         +"',"+Item_Quantity + "='"+updateLabel[4]+"',"+Item_Discount +"='"+updateLabel[5]
                         +"',"+Item_Color + "='"+updateLabel[6]+"',"+Item_Style +"='"+updateLabel[7]
                         +"',"+Item_Size + "='"+updateLabel[8]
                         +"' WHERE "+ UPC_Number + " = '" + updateLabel[0] + "'";

       Log.d(DATABASE_TAG, "updating data" +  updateLabel[0]+" "+updateLabel[1]+" "+updateLabel[2]+" "+updateLabel[3]
                +" "+updateLabel[4]+" "+updateLabel[5]+" "+updateLabel[6]+" "+updateLabel[7]+" "
                +" "+updateLabel[8] );

        rfidListDb.execSQL(query2);
        rfidListDb.close();
    }


    public String [] retrieveDataUPC(String searchUPC) throws NullPointerException {

        String[] rfid_Label_captured = new String[9];

          Log.d(DATABASE_TAG, "passing UPC" + searchUPC);
        try {
            SQLiteDatabase rfidListDb = openOrCreateDatabase(database_name, MODE_PRIVATE, null);
            /*retrieve data from database */

            String query = "SELECT * FROM " + database_name + " WHERE " + UPC_Number + " = '" + searchUPC + "'";
            Cursor cur = rfidListDb.rawQuery(query, null);

            if (cur.moveToFirst()) {
                while (!cur.isAfterLast()) {

                    rfid_Label_captured[0] = cur.getString(cur.getColumnIndex(UPC_Number));
                    rfid_Label_captured[1] = cur.getString(cur.getColumnIndex(Company_Name));
                    rfid_Label_captured[2] = cur.getString(cur.getColumnIndex(Item_Name));
                    rfid_Label_captured[3] = cur.getString(cur.getColumnIndex(Item_Price));
                    rfid_Label_captured[4] = cur.getString(cur.getColumnIndex(Item_Quantity));
                    rfid_Label_captured[5] = cur.getString(cur.getColumnIndex(Item_Discount));
                    rfid_Label_captured[6] = cur.getString(cur.getColumnIndex(Item_Color));
                    rfid_Label_captured[7] = cur.getString(cur.getColumnIndex(Item_Style));
                    rfid_Label_captured[8] = cur.getString(cur.getColumnIndex(Item_Size));
                    // capturing data

                    Log.d(DATABASE_TAG, "retrieved data" + rfid_Label_captured[0] + " " + rfid_Label_captured[1] + " " + rfid_Label_captured[2] + " " + rfid_Label_captured[3]
                            + " " + rfid_Label_captured[4] + " " + rfid_Label_captured[5] + " " + rfid_Label_captured[6] + " " + rfid_Label_captured[7] + " "
                            + " " + rfid_Label_captured[8]);
                    cur.moveToNext();
                }
                cur.close();
                rfidListDb.close();
            }

            return rfid_Label_captured;
        } catch (NullPointerException e1) {
            return rfid_Label_captured;
        } catch (SQLException e2) {
            return rfid_Label_captured;
        }
    }
    public ArrayList retrieveAllData (){

      //  String[] rfid_Label_captured = new String[9];
        ArrayList<String[]> total_db = new ArrayList<String[]>();
        Log.d(DATABASE_TAG, "Retrieving all data");
        try {
            SQLiteDatabase rfidListDb = openOrCreateDatabase(database_name, MODE_PRIVATE, null);
            /*retrieve data from database */

            String query = "SELECT * FROM " + database_name ;
            long count_db = DatabaseUtils.queryNumEntries(rfidListDb,database_name);
            Cursor cur = rfidListDb.rawQuery(query, null);

            if (cur.moveToFirst()) {
                while (!cur.isAfterLast()) {
                    String[] rfid_Label_captured = new String[9];
                    rfid_Label_captured[0] = cur.getString(cur.getColumnIndex(UPC_Number));
                    rfid_Label_captured[1] = cur.getString(cur.getColumnIndex(Company_Name));
                    rfid_Label_captured[2] = cur.getString(cur.getColumnIndex(Item_Name));
                    rfid_Label_captured[3] = cur.getString(cur.getColumnIndex(Item_Price));
                    rfid_Label_captured[4] = cur.getString(cur.getColumnIndex(Item_Quantity));
                    rfid_Label_captured[5] = cur.getString(cur.getColumnIndex(Item_Discount));
                    rfid_Label_captured[6] = cur.getString(cur.getColumnIndex(Item_Color));
                    rfid_Label_captured[7] = cur.getString(cur.getColumnIndex(Item_Style));
                    rfid_Label_captured[8] = cur.getString(cur.getColumnIndex(Item_Size));
                    total_db.add(rfid_Label_captured);

                    // capturing data

                    Log.d(DATABASE_TAG, "retrieved data_record  ---> " + total_db.size()+ "count_db :>>"+ count_db+ " -- " +rfid_Label_captured[0]+" "+rfid_Label_captured[1]+" "+rfid_Label_captured[2]+" "+rfid_Label_captured[3]
                            +" "+rfid_Label_captured[4] );

                    cur.moveToNext();
                }
                cur.close();
                rfidListDb.close();
            }

            return total_db;
        } catch (NullPointerException e1) {

            return null;
        } catch (SQLException e2) {

            return null;
        }


    }

    /**
     * Deletes the existing database (if there is one) and creates a new empty database with the
     * same name. The empty database will only have the base 5 columns: EntryNumber, Date, Time,
     * BarcodeData, and Notes.
     */
    public void resetDatabase() {
        try {
            SQLiteDatabase rfidListDb = openOrCreateDatabase(database_name, MODE_PRIVATE, null);
            String clearDBQuery = "DELETE FROM " + database_name;
            rfidListDb.execSQL(clearDBQuery);
        }catch (SQLiteException e){
            String error_db = "There is not database yet : "+e;
        }
    }

    /**
     * Writes the SQLite database to a .csv file with the specified filename. Returns true if the
     * write is successful.
     *

    /**
     * Adds a column to the app's SQLite database.
     */
    public void addColumn() {

    }

    /**
     * Reads the app's SQLite database and returns the number of columns that it has.
     *
     * @return              int of the number of columns in the database
     */
    public int getColumnCount() {
       return 0;
    }

    /**
     * Returns a formatted string of the current time, including seconds.
     * @param c         The character the date should be formatted with (: or -)
     * @return          Formatted String of the current time
     */
    public String getTime(char c) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("kk" + c + "mm" + c + "ss");
        Date currentTimeZone = calendar.getTime();
        return sdf.format(currentTimeZone);
    }

    /**
     * Returns a formatted string of today's date.
     * @param c         The character the date should be formatted with (/ or -)
     * @return          Formatted String of today's date
     */
    public String getDate(char c) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM" + c + "dd" + c + "yyyy");
        Date currentTimeZone = calendar.getTime();
        return sdf.format(currentTimeZone);
    }

    /**
     * Sets all the global variables (except the custom entry fields) to what's stored in the app's
     * SharedPreferences.
     */
    public void getSharedVariables() {
        SharedPreferences pref = getSharedPreferences("database_preferences",MODE_PRIVATE);
        entryCount = pref.getInt("entryCount", 1);
        folderName = pref.getString("folderName","ZebraLeadCapture");
        timestampFileNames = pref.getBoolean("timestampFileNames",true);
        customDataEntry = pref.getBoolean("customDataEntry",false);
        parseScannedData = pref.getBoolean("parseScannedData",false);
        suggestedDelimiter = pref.getString("suggestedDelimiter","");
    }

    /**
     * Updates all the persistent variables (except the custom entry fields) for the app in its
     * SharedPreferences.
     */
    public void updateSharedVariables() {
        SharedPreferences pref = getSharedPreferences("database_preferences",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("entryCount",entryCount);
        editor.putBoolean("timestampFileNames",timestampFileNames);
        editor.putBoolean("customDataEntry",customDataEntry);
        editor.putBoolean("parseScannedData",parseScannedData);
        editor.putString("folderName",folderName);
        editor.putString("suggestedDelimiter",suggestedDelimiter);
        editor.apply();
    }

    /**
     * Sets the global customEntryFields variable to what's stored in the app's SharedPreferences.
     */
    public void getCustomEntryFields() {
        if (customEntryFields==null) {
            customEntryFields = new ArrayList<>();
        }
        SharedPreferences pref = getSharedPreferences("database_preferences",MODE_PRIVATE);
        String customEntryFieldsString = pref.getString("customEntryFieldsString","");
        if (customEntryFieldsString.length()<=0 || customEntryFieldsString.equals("__null__")) {
            customEntryFields.clear();
            return;
        }

        Collections.addAll(customEntryFields,customEntryFieldsString.split(";"));
    }

    /**
     * Update the shared custom entry fields in the app's SharedPreferences.
     */
    public void updateCustomEntryFields() {
        String customEntryFieldsString = "";
        for (String field : customEntryFields) {
            customEntryFieldsString = customEntryFieldsString + ";" + field;
        }

        if (customEntryFieldsString.length()>0) {
            customEntryFieldsString = customEntryFieldsString.substring(1,customEntryFieldsString.length());
        } else {
            Log.d(DATABASE_TAG, "No custom entry fields to store");
            customEntryFieldsString = "__null__";   //SharedPreferences won't store an empty string, so this is used to signal no column names entered
        }

        SharedPreferences pref = getSharedPreferences("database_preferences",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("customEntryFieldsString",customEntryFieldsString);
        editor.apply();
    }




}
