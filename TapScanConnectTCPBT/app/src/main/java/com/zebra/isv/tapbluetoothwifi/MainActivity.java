/**
 * The Tap Scan Pair Bluetooth WIFI sample application automatically connects an Android device to a Zebra Technologies printer with a Near Field Communication (NFC) tag.
 * Printers can be connected either via Bluetooth or TCP/IP (WIFI) automatically after performing an NFC touch action.
 * The application also implements automatic pairing over Bluetooth when a MAC Address is scanned, or connection through WIFI when an IP address or WIFI Mac Address is scanned.
 * This application follows best practice  procedures and accounts for multiple error states.
 * This application was tested with a Motorola TC55 running Android Version 4.1.2 and the Zebra QLn320, iMZ320, iMZ220, and ZQ510 printers.
 *
 * Application Version: v1.1
 * Last Updated: 8/12/15
 * Recent Changes: Minor bug fixes, updated variable names, added comments
 * Updated By: Benjamin Wai, Zebra ISV Team
 */


package com.zebra.isv.tapbluetoothwifi;

/**
 * Imports packages from Android, Zebra LinkOS SDK, and Java libraries.
 * To import the Zebra SDK libraries, the ZSDK_ANDROID_API.jar must be added to the project folder
 *      and module dependency must be added via File -> Project Structure -> app -> Dependencies
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;
import com.zebra.sdk.printer.discovery.NetworkDiscoverer;

import java.util.ArrayList;
import java.util.Set;


public class MainActivity extends Activity{

    //Initialize Global Variables
    final Context context = this;
    private AlertDialog successDialog;

        //Bluetooth Adapter variables
    private BluetoothDeviceArrayAdapter adapter;
    private BroadcastReceiver broadcastReceiver;

        //UI variables
    private Button button;
    private EditText editText;
    private ListView listview;
    private NfcAdapter nAdapter;
    private RadioButton btRadioButton;

        //NFC Touch variables
    private Tag detectedTag;
    private String nfcWifiAddress;
    private String nfcMacAddress;
    private String nfcSerialName;

        //Printer data variables
    private String bluetoothAddress;
    private String ipAddress;
    private String uniqueId;
    private Integer port;
    private String bluetoothDiscoverable;
    private String wifiMacAddress;

    /**
     * onCreate() contains several processes that begin once the application is started
     * Contains EventListener/ Event procedures
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  Grab the NFC Adapter object
        nAdapter = NfcAdapter.getDefaultAdapter(this);

        //  Grab the editText object
        editText = (EditText) this.findViewById(R.id.editText);
        final TextView uniqueIdTag = (TextView) this.findViewById(R.id.uniqueIdTag);
        final TextView uniqueIdText = (TextView) this.findViewById(R.id.uniqueIdText);
        final TextView bluetoothTag = (TextView) this.findViewById(R.id.bluetoothTag);
        final TextView bluetoothText = (TextView) this.findViewById(R.id.bluetoothText);
        final TextView ipAddrTag = (TextView) this.findViewById(R.id.ipAddrTag);
        final TextView ipAddrText = (TextView) this.findViewById(R.id.ipAddrText);
        final TextView portTag = (TextView) this.findViewById(R.id.portTag);
        final TextView portText = (TextView) this.findViewById(R.id.portText);
        final TextView btMacAddrTag = (TextView) this.findViewById(R.id.btMacAddrTag);
        final TextView btMacAddrText = (TextView) this.findViewById(R.id.btMacAddrText);
        final TextView wifiMacAddrTag = (TextView) this.findViewById(R.id.wifiMacAddrTag);
        final TextView wifiMacAddrText = (TextView) this.findViewById(R.id.wifiMacAddrText);
        final TextView textView = (TextView) this.findViewById(R.id.textView);

        //  Initiate in Bluetooth mode, hide WIFI screen
        uniqueIdTag.setVisibility(View.GONE);
        uniqueIdText.setVisibility(View.GONE);
        bluetoothText.setVisibility(View.GONE);
        bluetoothTag.setVisibility(View.GONE);
        portText.setVisibility(View.GONE);
        portTag.setVisibility(View.GONE);
        ipAddrTag.setVisibility(View.GONE);
        ipAddrText.setVisibility(View.GONE);
        btMacAddrTag.setVisibility(View.GONE);
        btMacAddrText.setVisibility(View.GONE);
        wifiMacAddrTag.setVisibility(View.GONE);
        wifiMacAddrText.setVisibility(View.GONE);

        //  Initialize printButton, onClickListener, and onClick event procedures
        final Button printButton = (Button) this.findViewById(R.id.printButton);
        printButton.setVisibility(View.GONE);
        printButton.setOnClickListener (new OnClickListener() {
            @Override
            public void onClick (View v) {
                createAlert(context, "Print", "Are you sure you want to print a test label?", "Print", "Cancel", true);
            }
        });

        //  Initialize button, onClickListener, and onClick event procedures
        button = (Button) this.findViewById(R.id.button);
        button.setOnClickListener (new OnClickListener () {
            @Override
            public void onClick (View v) {
                new Thread(new Runnable() {
                    public void run() {
                        if(!btRadioButton.isChecked()){ //  Checks if TCP / IP radio button is selected
                            try{
                                if (editText.getText().toString().contains(".")){ //Checks if input text should be treated as an IP address
                                    TcpConnection conn = new TcpConnection(editText.getText().toString(),9100);
                                    startSearching();
                                    connectDevice(conn);
                                    displayInfo();
                                    stopSearching(uniqueId);
                                } else{ //Treats input as WIFI MAC address
                                    nfcWifiAddress = editText.getText().toString();
                                    Looper.prepare();
                                    createAlert(context, "Search", "Would you like to search for " + editText.getText().toString() + "?", "Search", "Cancel", false);
                                    Looper.loop();
                                    Looper.myLooper().quit();
                                }

                            } catch (Exception e){
                                displayToast("ERROR: Unable to connect to printer");
                                e.printStackTrace();
                                stopSearching(null);
                            }
                        }
                        else { //Bluetooth radio button selected
                            try {
                                BluetoothConnection conn = new BluetoothConnection(editText.getText().toString()); //Input text is considered Bluetooth MAC address
                                startSearching();
                                if(!isPrinterPaired(editText.getText().toString())) {
                                    connectDevice(conn);
                                }

                            } catch (Exception e) {
                                displayToast("ERROR: Unable to connect to printer");
                                e.printStackTrace();
                                stopSearching(null);
                            }
                        }
                    }
                }).start();
                refreshList();  // Refresh ListView to display newly paired devices
            }
        });

        //  Initialize RadioGroup, RadioButton, and CheckedChanged procedures
        btRadioButton = (RadioButton) this.findViewById(R.id.btRadioButton);
        RadioGroup radioGroup = (RadioGroup) this.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener (new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.btRadioButton) {
                    editText.setHint("Tap or Scan to Pair");
                    uniqueIdTag.setVisibility(View.GONE);   //  Hides the TextViews used in the WIFI View
                    uniqueIdText.setVisibility(View.GONE);
                    bluetoothText.setVisibility(View.GONE);
                    bluetoothTag.setVisibility(View.GONE);
                    portText.setVisibility(View.GONE);
                    portTag.setVisibility(View.GONE);
                    ipAddrTag.setVisibility(View.GONE);
                    ipAddrText.setVisibility(View.GONE);
                    btMacAddrTag.setVisibility(View.GONE);
                    btMacAddrText.setVisibility(View.GONE);
                    wifiMacAddrTag.setVisibility(View.GONE);
                    wifiMacAddrText.setVisibility(View.GONE);
                    printButton.setVisibility(View.GONE);
                    ListView listView = (ListView) findViewById(R.id.lvPairedDevices);
                    listView.setVisibility(View.VISIBLE);
                    textView.setText("Bluetooth Paired Devices");
                }
                else {
                    editText.setHint("Tap or Scan to Search");
                    uniqueIdTag.setVisibility(View.VISIBLE);    //  Hides the ListView and shows the TextViews used in the WIFI View
                    uniqueIdText.setVisibility(View.VISIBLE);
                    bluetoothText.setVisibility(View.VISIBLE);
                    bluetoothTag.setVisibility(View.VISIBLE);
                    portText.setVisibility(View.VISIBLE);
                    portTag.setVisibility(View.VISIBLE);
                    ipAddrTag.setVisibility(View.VISIBLE);
                    ipAddrText.setVisibility(View.VISIBLE);
                    btMacAddrTag.setVisibility(View.VISIBLE);
                    btMacAddrText.setVisibility(View.VISIBLE);
                    wifiMacAddrTag.setVisibility(View.VISIBLE);
                    wifiMacAddrText.setVisibility(View.VISIBLE);
                    printButton.setVisibility(View.VISIBLE);
                    textView.setText("Wifi Printer Information");
                    ListView listView = (ListView) findViewById(R.id.lvPairedDevices);
                    listView.setVisibility(View.GONE);
                }
            }
        });

        //  Initialize the list view and its adapter
        listview = (ListView) findViewById(R.id.lvPairedDevices);
        adapter = new BluetoothDeviceArrayAdapter(this,getPairedPrinters());
        listview.setAdapter(adapter);

        //  Print a configuration label when a Bluetooth printer is clicked
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final BluetoothDevice item = (BluetoothDevice) parent.getItemAtPosition(position);
                if (item != null && item.getAddress() != null && isBluetoothPrinter(item)) {
                    bluetoothAddress = item.getAddress();
                    createAlert(context, "Print", "Are you sure you want to print a test label?", "Print", "Cancel", true);
                }
            }
        });

        //  Create a BroadcastReciever to refresh the ListView when device is paired/unpaired
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                refreshList();
            }
        };

        //  Registers Bluetooth devices to ListView once paired
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcastReceiver, filter);
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refreshList();
        }
        switch (item.getItemId()) {
            case R.id.menu_clear:
                clearSearch();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //  Attempt to process NFC data onResume in case the app was started via an NFC touch
        setupForegroundDispatch();
    }

    @Override
    protected void onDestroy() {
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    /**
     * onNewIntent contains several processes that occur once an Action Tag is detected via
     *      NFC touch. NFC must be enabled for these processes to occur
     *
     * @param intent NFC touch or Tag detection
     */

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        if(getIntent().getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)){
            detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            handleIntent(getIntent()); //   Process NFC data
        }

        if (!nfcMacAddress.isEmpty() && !nfcSerialName.isEmpty() && !nfcWifiAddress.isEmpty()) {
            if(btRadioButton.isChecked()) {
                createAlert(this, "Pair", "Attempt to pair with " + nfcSerialName + "?", "Pair", "Cancel", false);
            }
            else{
                createAlert(this, "Connect or Search", "Attempt Bluetooth to TCP/IP Connection or Search for " + nfcSerialName + "?", "Connect", "Search", false);
            }
        }

        refreshList();
    }

    /**
     * onPause disables Foreground Dispatch when the application is paused, allowing NFC use with other apps
     */
    @Override
    protected void onPause() {
        super.onPause();
        stopForegroundDispatch();
    }

    /**
     * DiscoveryHandler() contains the processes used when searching for a printer over
     *      a network. Contains methods that are called in response to events during Network Discovery
     */
    DiscoveryHandler discoveryHandler = new DiscoveryHandler(){

        @Override
        public void foundPrinter(DiscoveredPrinter printer){
            try{
                TcpConnection conn = new TcpConnection (printer.address, 6101);
                connectDevice(conn);
                if((bluetoothAddress.equalsIgnoreCase(nfcMacAddress) || uniqueId.equalsIgnoreCase(nfcSerialName) || wifiMacAddress.equalsIgnoreCase(nfcWifiAddress))){
                    displayInfo();
                    stopSearching(uniqueId);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
           // printers.add(printer); //   Adds DiscoveredPrinters to an ArrayList once found
        }

        @Override
        public void discoveryFinished(){
        }

        @Override
        public void discoveryError(String error){
            Log.i("ERROR", error);
            stopSearching(null);
        }
    };

    /**
     * Shows a message informing the user that the device is already paired
     *
     * @param serialName name of the printer that was being searched for
     */

    private void showAlreadyPaired(final String serialName) {
        displayToast(String.format("%s is already paired", serialName));
    }

    /**
     * Refreshes the list of paired bluetooth devices
     */

    private void refreshList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                adapter.addAll(getPairedPrinters());
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Clears data saved from WIFI connection methods
     */
    private void clearSearch() {
        runOnUiThread(new Runnable() {
            @Override
            public void run(){

                TextView uniqueText = (TextView) findViewById(R.id.uniqueIdText);
                uniqueText.setText("");

                TextView ipAddrText = (TextView) findViewById(R.id.ipAddrText);
                ipAddrText.setText("");

                TextView bluetoothText = (TextView) findViewById(R.id.bluetoothText);
                bluetoothText.setText("");

                TextView portText = (TextView) findViewById(R.id.portText);
                portText.setText("");

                TextView macAddrText = (TextView) findViewById(R.id.btMacAddrText);
                macAddrText.setText("");

                TextView wifiMac = (TextView) findViewById(R.id.wifiMacAddrText);
                wifiMac.setText("");

            }
        });
    }

    /**
     * Checks to see if the given printer is currently paired to the Android device via bluetooth.
     *
     * @param address
     * @return true if the printer is paired
     */

    private boolean isPrinterPaired(String address) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().replaceAll("[\\p{P}\\p{S}]", "").equalsIgnoreCase(address) && btRadioButton.isChecked()) {
                showAlreadyPaired(nfcSerialName);
                stopSearching(null);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of all the printers currently paired to the Android device via bluetooth.
     *
     * @return a list of all the printers currently paired to the Android device via bluetooth.
     */

    private ArrayList<BluetoothDevice> getPairedPrinters() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> pairedDevicesList = new ArrayList<BluetoothDevice>();
        for (BluetoothDevice device : pairedDevices) {
            if (isBluetoothPrinter(device))
                pairedDevicesList.add(device);
        }
        return pairedDevicesList;
    }

    /**
     * Determines if the given bluetooth device is a printer
     *
     * @param bluetoothDevice bluetooth device
     * @return true if the bluetooth device is a printer
     */

    private boolean isBluetoothPrinter(BluetoothDevice bluetoothDevice) {
        return bluetoothDevice.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING
                || bluetoothDevice.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.UNCATEGORIZED;
    }

    /**
     * Enables foreground dispatch such that NDEF and Tag discovery occur within the app
     */

    private void setupForegroundDispatch(){
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter filter2 = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter[] filters = new IntentFilter[] {tagDetected, filter2};
        nAdapter.enableForegroundDispatch(this, pendingIntent, filters, null);
    }

    //  Disables foreground dispatch
    private void stopForegroundDispatch(){
        nAdapter.disableForegroundDispatch(this);
    }

    /**
     * handleIntent processes the intent passed from onNewIntent and pulls the Bluetooth MAC Address
     *      and/or the Serial Number from the Printer's tag
     *
     * @param intent
     */

    private void handleIntent(Intent intent){
        NdefMessage[] ndefMessages=null;
        Ndef ndef = Ndef.get(detectedTag);
        try{
            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (messages != null) {
                ndefMessages = new NdefMessage[messages.length];
                for (int i = 0; i < messages.length; i++){ //   Records NDEF message
                    ndefMessages[i] = (NdefMessage) messages[i];
                }
            }
            NdefRecord record = ndefMessages[0].getRecords()[0]; // Retrieves NDEF record
            byte[] payload = record.getPayload();   //  Retrieves byte payload from record
            String nfcData = new String(payload);   //  Converts payload into String format
            Log.i("NFC", nfcData);
            findMacAddr(nfcData);
            ndef.close();   //  Closes connection
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     * findMacAddr contains the process that pulls the Bluetooth MAC Address and Serial Number from the NFC Data
     * @param nfcData
     */

    private void findMacAddr(String nfcData){
        int index = 0;
        index = nfcData.indexOf("&mB="); // Index is set to the Bluetooth MAC Address identifier
        nfcMacAddress = nfcData.substring(index + 4, index + 16); //  Creates a substring containing only the MAC Address information
        index = nfcData.indexOf("&s");  // Index is set to the Serial Number identifier
        nfcSerialName = nfcData.substring(index + 3, index + 17);   // Creates a substring containg only the Serial Number
        index = nfcData.indexOf("&mW=");
        nfcWifiAddress = nfcData.substring(index + 3, index + 16); // Creates a substring containing only the WIFI MAC Address information

    }

    /**
     * findPrinterStatus() contains processes that check the connected printer's current status for two common error states, isHeadOpen and isPaperOut,
     *      and returns a boolean
     *
     * @param conn Established connection. Can be either BluetoothConnection or TcpConnection
     * @return True if no error is found. False if an error is found.
     */

    private boolean findPrinterStatus(final Connection conn){
        try {
            if (ZebraPrinterFactory.getInstance(conn).getCurrentStatus().isHeadOpen) {
                displayToast("ERROR: Printer Head is Open");
                return false;
            }

            else if (ZebraPrinterFactory.getInstance(conn).getCurrentStatus().isPaperOut) {
                displayToast("ERROR: No Media Detected");
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return true; // Returns if neither of the above error states is found
    }

    /**
     * displayToast creates a Toast pop up that appears in the center of the screen containing the
     * String message parameter
     * @param message String
     */

    private void displayToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast;
                toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
        });
    }

    /**
     * Creates an Alert Dialog
     * @param context
     */


    private void createAlert(Context context, String title, String message, final String positive, String negative, final boolean print){
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    startSearching();
                                    if (print) {
                                        if (btRadioButton.isChecked()) {
                                            BluetoothConnection conn = new BluetoothConnection(bluetoothAddress);
                                            connectAndPrint(conn);
                                        } else {
                                            try {
                                                TextView ipAddrText = (TextView) findViewById(R.id.ipAddrText);
                                                TextView portText = (TextView) findViewById(R.id.portText);
                                                TcpConnection conn = new TcpConnection(ipAddrText.getText().toString(), Integer.valueOf(portText.getText().toString()));
                                                connectAndPrint(conn);
                                            } catch (Exception e) {
                                                stopSearching(null);
                                                displayToast("ERROR: No printer is currently connected");
                                            }
                                        }
                                    } else if (!btRadioButton.isChecked() && positive.equalsIgnoreCase("Search")) {
                                        try {
                                            NetworkDiscoverer.findPrinters(discoveryHandler);
                                        } catch (Exception e) {
                                            stopSearching(null);
                                            e.printStackTrace();
                                        }
                                    } else if (btRadioButton.isChecked()) {
                                        if (!isPrinterPaired(nfcMacAddress)) {
                                            BluetoothConnection conn = new BluetoothConnection(nfcMacAddress);
                                            connectDevice(conn);
                                        }
                                    } else if (!btRadioButton.isChecked()) {
                                        BluetoothConnection conn = new BluetoothConnection(nfcMacAddress);
                                        Looper.prepare();
                                        connectBluetoothWifi(conn);
                                        Looper.loop();
                                        Looper.myLooper().quit();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    stopSearching(null);
                                }
                            }
                        }).start();
                    }
                })
                .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!btRadioButton.isChecked()) {
                            try {
                                startSearching();
                                NetworkDiscoverer.findPrinters(discoveryHandler);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Do nothing
                        }
                    }
                })
                .show();
    }

    /**
     * Opens a connection and prints a configuration label
     * @param conn
     */
    private void connectAndPrint (Connection conn){
        try{
            conn.open();
            String printerName = SGD.GET("device.unique_id", conn);
            if (findPrinterStatus(conn)) {
                ZebraPrinterFactory.getInstance(conn).printConfigurationLabel();
                stopSearching(printerName);
            }
            Thread.sleep(500);
            conn.close();

        } catch (Exception e){
            e.printStackTrace();
            displayToast("ERROR: Unable to connect to Printer");
            stopSearching(null);
        }
    }

    /**
     * Opens a connection to the printer and pulls information
     * @param conn
     */
    private void connectDevice (Connection conn){
        try{
            conn.open();
            ipAddress = SGD.GET("ip.addr", conn);
            bluetoothAddress = SGD.GET("bluetooth.short_address", conn);
            uniqueId = SGD.GET("device.unique_id", conn);
            port = Integer.valueOf(SGD.GET("ip.port", conn));
            bluetoothDiscoverable = SGD.GET("bluetooth.discoverable", conn);
            wifiMacAddress = SGD.GET("wlan.mac_raw", conn);
            Thread.sleep(500);
            if (btRadioButton.isChecked() && conn.isConnected())
                stopSearching(uniqueId);
            conn.close();

        } catch (ConnectionException e) {
            displayToast("ERROR: Unable to connect to Printer");
            stopSearching(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a Bluetooth connection, retrieves printer device data, then follows with a TCP connection. If a ConnectionException occurs (Bluetooth or TCP),
     * an Alert dialogue will be created that will attempt a Search / Discover method
     * @param conn
     */
    private void connectBluetoothWifi(BluetoothConnection conn){
        try {
            conn.open();
            if(conn.isConnected()){
                uniqueId = SGD.GET("device.unique_id",conn);
                bluetoothDiscoverable = SGD.GET("bluetooth.discoverable", conn);
                bluetoothAddress = SGD.GET("bluetooth.short_address", conn);
                ipAddress = SGD.GET("ip.addr", conn);
                port = Integer.valueOf(SGD.GET("ip.port", conn));
                wifiMacAddress = SGD.GET("wlan.mac_raw", conn);
                Thread.sleep(500);
                conn.close();
                TcpConnection conn2 = new TcpConnection(ipAddress, port);
                conn2.open();
                if (conn2.isConnected()) {
                    displayInfo();
                    stopSearching(uniqueId);
                }
            }
        } catch (ConnectionException e){
            stopSearching(null);
            createAlert(context, "Search", "Unable to connect. Would you like to Search the network?", "Search", "Cancel", false); //   Alert Dialogue initiates Network Discovery
            e.printStackTrace();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Displays "Searching" in the editText field, enables the progressbar, and disables other fields
     */
    private void startSearching() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EditText editText = (EditText) findViewById(R.id.editText);

                editText.setText("Searching...");
                editText.setEnabled(false);

                Button button = (Button) findViewById(R.id.button);
                button.setVisibility(View.GONE);

                ProgressBar pbSearching = (ProgressBar) findViewById(R.id.pbSearching);
                pbSearching.setVisibility(View.VISIBLE);

                RadioButton btRadioButton = (RadioButton) findViewById(R.id.btRadioButton);
                btRadioButton.setEnabled(false);
                RadioButton ipRadioButton = (RadioButton) findViewById(R.id.ipRadioButton);
                ipRadioButton.setEnabled(false);

                Button printButton = (Button) findViewById(R.id.printButton);
                printButton.setEnabled(false);

            }
        });
    }

    /**
     * Displays results of search in editText field, re-enables fields, and makes the progress bar invisible
     * @param printerName
     */
    private void stopSearching(final String printerName) {
        runOnUiThread(new Runnable () {
            @Override
            public void run() {
                if (printerName != null) {
                    if (successDialog == null || !successDialog.isShowing()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                .setTitle("Action Successful")
                                .setMessage(String.format("%s successfully completed the request", printerName))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        successDialog.dismiss();
                                        successDialog = null;
                                    }
                                });
                        successDialog = builder.create();
                        successDialog.show();
                    }
                }

                EditText editText = (EditText) findViewById(R.id.editText);
                editText.setText("");
                editText.setEnabled(true);

                Button button = (Button) findViewById(R.id.button);
                button.setVisibility(View.VISIBLE);

                ProgressBar pbSearching = (ProgressBar) findViewById(R.id.pbSearching);
                pbSearching.setVisibility(View.GONE);

                RadioButton btRadioButton = (RadioButton) findViewById(R.id.btRadioButton);
                btRadioButton.setEnabled(true);
                RadioButton ipRadioButton = (RadioButton) findViewById(R.id.ipRadioButton);
                ipRadioButton.setEnabled(true);

                Button printButton = (Button) findViewById(R.id.printButton);
                printButton.setEnabled(true);

            }
        });
    }

    /**
     * Displays data retrieved from printer on the UI. Text is selectable and can be copied
     */
    private void displayInfo(){
        runOnUiThread(new Runnable() {
            @Override
            public void run(){

                TextView uniqueText = (TextView) findViewById(R.id.uniqueIdText);
                uniqueText.setText(uniqueId);

                TextView ipAddrText = (TextView) findViewById(R.id.ipAddrText);
                ipAddrText.setText(ipAddress);
                SettingsHelper.saveIp(context, ipAddress);

                TextView bluetoothText = (TextView) findViewById(R.id.btMacAddrText);
                bluetoothText.setText(bluetoothAddress);
                SettingsHelper.saveBluetoothAddress(context, bluetoothAddress);

                TextView bluetoothEnable = (TextView) findViewById(R.id.bluetoothText);
                bluetoothEnable.setText(bluetoothDiscoverable);

                TextView portText = (TextView) findViewById(R.id.portText);
                portText.setText(Integer.toString(port));
                SettingsHelper.savePort(context, Integer.toString(port));

                TextView wifiMac = (TextView) findViewById(R.id.wifiMacAddrText);
                wifiMac.setText(wifiMacAddress);
                SettingsHelper.saveWlanAddress(context, wifiMacAddress);
            }
        });
    }


}
