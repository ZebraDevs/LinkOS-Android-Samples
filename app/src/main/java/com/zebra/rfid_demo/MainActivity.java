package com.zebra.rfid_demo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends DatabaseActivity {
    private static final String DIALOG_PRINTER_CONNECT_TAG = "printer_connect";
    private static final String DIALOG_SIGN_AND_PRINT_TAG = "sign_and_print";

    private static final String NEW_ENTRY_TAG = "new_entry";
    private static final String EXPORT_DATA_TAG = "export_database";
    private static final String EMAIL_DATA_TAG = "email_database";
    private static final String RESET_DATA_TAG = "reset_database";

    private String filename = "";

    //TC-55 Price 58000/100
    private static final int PRICE_CENTS_PRODUCT_ONE = 58000;
    //Charger 2000/100
    private static final int PRICE_CENTS_PRODUCT_TWO = 2000;

    private TextView printerSelectionStatus;
    private TextView printerInfoTableName;
    private TextView printerInfoTableAddress;
    private TableLayout printerInfoTable;


    private ArrayList<HashMap<String, String>> list;
    public static final String FIRST_COLUMN="First";
    public static final String SECOND_COLUMN="Second";
    public static final String THIRD_COLUMN="Third";
    public static final String FOURTH_COLUMN="Fourth";
    public static final String FIFTH_COLUMN="Fifth";

    private static final int LIST_RFID_LABELS = 2;

    private TextView signAndPrintButton;
    private TextView resetEntryButton;
    Context context;


    //IntentFilter that matches the Intent action in the DataWedge profile
    IntentFilter filter = new IntentFilter("com.zebra.rfid_demo.RECVR");
    BroadcastReceiver receiver = new BroadcastReceiver() { //When an intent is caught, handles it
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
            Intent i = new Intent(context, NewScanLabelActivity.class);
            String data = intent.getStringExtra("com.motorolasolutions.emdk.datawedge.data_string");
            i.putExtra("barcode_data", data);
            i.putExtra("soft_scan", 1);
            context.startActivity(i);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        registerReceiver(receiver,filter);

        getPermissions(MainActivity.this);


        printerSelectionStatus = (TextView) findViewById(R.id.printerSelectionStatus);
        printerInfoTableName = (TextView) findViewById(R.id.printerInfoTableName);
        printerInfoTableAddress = (TextView) findViewById(R.id.printerInfoTableAddress);
        printerInfoTable = (TableLayout) findViewById(R.id.printerInfoTable);



        TextView discoverPrintersButton = (TextView) findViewById(R.id.discoverPrintersButton);
        discoverPrintersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                PrinterConnectionDialog printerConnectionDialog = (PrinterConnectionDialog) getFragmentManager().findFragmentByTag(DIALOG_PRINTER_CONNECT_TAG);

                if (printerConnectionDialog == null) {
                    printerConnectionDialog = new PrinterConnectionDialog();
                    printerConnectionDialog.show(ft, DIALOG_PRINTER_CONNECT_TAG);
                }
            }
        });




        final TextView ResetEntryButton = (TextView) findViewById(R.id.button_database);
        ResetEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(NEW_ENTRY_TAG, "Reset database");
                resetDatabase();
                finish();
                startActivity(getIntent());

            }
        });


        ListView listView=(ListView)findViewById(R.id.listView1);
        populateList();


        ListViewAdapter adapter=new ListViewAdapter(this, list);

        listView.setAdapter(adapter);



    }



    public void populateList() {
        // TODO Auto-generated method stub
        ArrayList<String[]> pop_db= new ArrayList<String[]>();


        list=new ArrayList<HashMap<String,String>>();

        HashMap<String,String> hashmap_title=new HashMap<String, String>();
        hashmap_title.put(FIRST_COLUMN, "UPC");
        hashmap_title.put(SECOND_COLUMN, "Product Name");
        hashmap_title.put(THIRD_COLUMN, "Provider");
        hashmap_title.put(FOURTH_COLUMN, "Price");
        hashmap_title.put(FIFTH_COLUMN, "QTY");

        list.add(hashmap_title);
        HashMap<String,String> hashmap_tempo_ref=new HashMap<String, String>();
        try {
            pop_db = retrieveAllData();
            String [] rfid_barcode = new String[5];

            DiscoveredPrinter discoveredPrinter = SelectedPrinterManager.getSelectedPrinter();
            if(discoveredPrinter!=null) {
                updatePrinterInfoTable(discoveredPrinter);
            }

                for (int i = 0; i < pop_db.size(); i++) {
                    rfid_barcode = pop_db.get(i);

                    HashMap<String,String> hashmap_tempo=new HashMap<String, String>();
                    hashmap_tempo.put(FIRST_COLUMN, rfid_barcode[0]);
                    hashmap_tempo.put(SECOND_COLUMN, rfid_barcode[1]);
                    hashmap_tempo.put(THIRD_COLUMN, rfid_barcode[2]);
                    hashmap_tempo.put(FOURTH_COLUMN, rfid_barcode[3]);
                    hashmap_tempo.put(FIFTH_COLUMN, rfid_barcode[4]);



                    Log.i(NEW_ENTRY_TAG, "Populate database:"+rfid_barcode[0]+ rfid_barcode[1]+rfid_barcode[2]+ rfid_barcode[3]+rfid_barcode[4]);
                    list.add(hashmap_tempo);


                }
             // list.clear();

        }catch(NullPointerException e){

        }

    }

    public void displayConnectingStatus() {
        printerSelectionStatus.setText(getString(R.string.connecting_to_printer));
    }

    public void updatePrinterInfoTable(DiscoveredPrinter discoveredPrinter) {
        printerInfoTableName.setText(discoveredPrinter.getDiscoveryDataMap().get("SYSTEM_NAME"));
        printerInfoTableAddress.setText(discoveredPrinter.getDiscoveryDataMap().get("HARDWARE_ADDRESS"));
        printerInfoTable.setVisibility(View.VISIBLE);
        printerSelectionStatus.setText(getString(R.string.connected_to_printer));


    }

    public void resetConnectingStatus() {
        printerInfoTable.setVisibility(View.GONE);
        printerSelectionStatus.setText(getString(R.string.no_printer_selected));


    }

    public void showSnackbar(String snackbarText) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), snackbarText, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(getResources().getColor(R.color.near_black));
        TextView snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public HashMap<String, String> newrfidLabel(String [] label){

        HashMap<String, String > rfidLabel = new HashMap<>();

        rfidLabel.put(FIRST_COLUMN, label[0]);
        rfidLabel.put(SECOND_COLUMN, label[1]);
        rfidLabel.put(THIRD_COLUMN, label[2]);
        rfidLabel.put(FOURTH_COLUMN, label[3]);
        rfidLabel.put(FIFTH_COLUMN, label[4]);
        return rfidLabel;
    }
    public HashMap<Integer, String> mergeVariables() {


        HashMap<Integer, String> variableMap = new HashMap<>();
        variableMap.put(16,"Test");

        return variableMap;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:  //When the wrench icon is clicked.
                final EditText input = new EditText(MainActivity.this);
                final Intent intent = new Intent(MainActivity.this, SettingsActivity.class);

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                /*
                                Checks password. If invalid, shows Toast. If valid, launches
                                SettingsActivity.
                                 */
                                if (input.getText().toString().equals(settingsCredentials)) {
                                    startActivity(intent);
                                } else {
                                    Toast toast = Toast.makeText(getApplicationContext(),
                                            "Password is incorrect",
                                            Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                                break;
                        }
                    }
                };

                /*
                Launches a password dialog.
                 */
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                builder .setView(input)
                        .setMessage("Please enter the administrator password to access the settings")
                        .setPositiveButton("Log In", dialogClickListener)
                        .setNegativeButton("Cancel", dialogClickListener)
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Checks whether it's supposed to timestamp new file names and returns a file name
     * accordingly.
     *
     * @return      A .csv file name String, including extension
     */
    private String getFileName() {
        if (!timestampFileNames) { return "RFID_LIST.csv"; }
        String newFileName = "RFID_LIST_" + getDate('-') + "_" + getTime('-') + ".csv";
        return newFileName;
    }
    /**
     * Launches the Andoird dialog to request the read/write permissions.
     * Required as of Android 6.0.
     * @param activity
     */
    private static void getPermissions(Activity activity) {
        final int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,



        };

        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        populateList();
        super.onPause();


    }


    @Override
    protected void onResume() {
        populateList();
        registerReceiver(receiver, filter);
        super.onResume();

    }

    @Override
    public void onBackPressed()
    {
        // Add your code here
        finish();
        // Then call the parent constructor to have the default button functionality
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Writes the app's current SQLite database to a .csv file on a new Thread. Saves this file in the device's
     * external memory in the folder specified in the settings menu.
     *
     * @param newFileName       The name of the new file
     */
    private void exportDatabase(String newFileName) {
        getSharedVariables();

    }




}
